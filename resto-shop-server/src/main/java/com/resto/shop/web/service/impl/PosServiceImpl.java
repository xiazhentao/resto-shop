package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.alibaba.fastjson.JSON;
import com.resto.brand.web.model.AccountSetting;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.AccountSettingService;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.constant.OrderPayMode;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.*;
import com.resto.shop.web.posDto.*;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import com.resto.shop.web.util.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.resto.shop.web.service.impl.OrderServiceImpl.generateString;

/**
 * Created by KONATA on 2017/8/9.
 */
@RpcService
public class PosServiceImpl implements PosService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderPaymentItemService orderPaymentItemService;

    @Autowired
    private PlatformOrderService platformOrderService;

    @Autowired
    private PlatformOrderDetailService platformOrderDetailService;

    @Autowired
    private PlatformOrderExtraService platformOrderExtraService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandSettingService brandSettingService;

    @Autowired
    private AccountSettingService accountSettingService;

    @Autowired
    private CustomerAddressService customerAddressService;

    @Autowired
    private ShopDetailService shopDetailService;

    @Autowired
    private CustomerService customerService;

    @Override
    public String syncArticleStock(String shopId) {
        Map<String, Object> result = new HashMap<>();
        result.put("dataType", "article");

        List<Article> articleList = articleService.selectList(shopId);
        List<ArticleStockDto> articleStockDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Integer count = (Integer) RedisUtil.get(article.getId() + Common.KUCUN);
            if (count != null) {
                article.setCurrentWorkingStock(count);
            }
            ArticleStockDto articleStockDto = new ArticleStockDto(article.getId(), article.getCurrentWorkingStock());
            articleStockDtoList.add(articleStockDto);
        }
        result.put("articleList", articleStockDtoList);
        return new JSONObject(result).toString();
    }

    @Override
    public String shopMsgChange(String shopId) {
        return shopId;
    }

    @Override
    public String syncOrderCreated(String orderId) {
        Order order = orderService.selectById(orderId);
        OrderDto orderDto = new OrderDto(order);
        JSONObject jsonObject = new JSONObject(orderDto);
        jsonObject.put("dataType", "orderCreated");
        List<OrderItem> orderItems = orderItemService.listByOrderId(orderId);
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for(OrderItem orderItem : orderItems){
            OrderItemDto orderItemDto = new OrderItemDto(orderItem);
            orderItemDtos.add(orderItemDto);
        }
        Customer customer = customerService.selectById(order.getCustomerId());
        jsonObject.put("customer", new CustomerDto(customer));
        jsonObject.put("orderItem", orderItemDtos);
        if(order.getPayMode() == OrderPayMode.YUE_PAY || order.getPayMode() == OrderPayMode.XJ_PAY
                || order.getPayMode() == OrderPayMode.YL_PAY){
            List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
            if(!CollectionUtils.isEmpty(payItemsList)){
                List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
                for(OrderPaymentItem orderPaymentItem : payItemsList){
                    OrderPaymentDto orderPaymentDto = new OrderPaymentDto(orderPaymentItem);
                    orderPaymentDtos.add(orderPaymentDto);
                }
                jsonObject.put("orderPayment", orderPaymentDtos);
            }
        }


        CustomerAddress customerAddress = customerAddressService.selectByPrimaryKey(order.getCustomerAddressId());

        if(customerAddress != null){
            CustomerAddressDto customerAddressDto = new CustomerAddressDto(customerAddress);
            jsonObject.put("customerAddress",new JSONObject(customerAddressDto));
        }
        return jsonObject.toString();
    }

    @Override
    public String syncOrderPay(String orderId) {
        Order order = orderService.selectById(orderId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dataType", "orderPay");
        jsonObject.put("orderId",order.getId());
        jsonObject.put("payMode", order.getPayMode());
        List<OrderPaymentItem> payItemsList = orderPaymentItemService.selectByOrderId(order.getId());
        List<OrderPaymentDto> orderPaymentDtos = new ArrayList<>();
        for(OrderPaymentItem  paymentItem: payItemsList){
            OrderPaymentDto orderPaymentDto = new OrderPaymentDto(paymentItem);
            orderPaymentDtos.add(orderPaymentDto);
        }
        jsonObject.put("orderPayment", orderPaymentDtos);
        return jsonObject.toString();
    }

    @Override
    public String syncPlatform(String orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("dataType", "platform");
        PlatformOrder platformOrder = platformOrderService.selectByPlatformOrderId(orderId, null);
        List<PlatformOrderDetail> platformOrderDetails = platformOrderDetailService.selectByPlatformOrderId(orderId);
        List<PlatformOrderDetailDto> platformOrderDetailDtos = new ArrayList<>();
        for(PlatformOrderDetail platformOrderDetail : platformOrderDetails){
            PlatformOrderDetailDto detailDto = new PlatformOrderDetailDto(platformOrderDetail);
            platformOrderDetailDtos.add(detailDto);
        }
        List<PlatformOrderExtra> platformOrderExtras = platformOrderExtraService.selectByPlatformOrderId(orderId);
        List<PlatformOrderExtraDto> extraDtos = new ArrayList<>();
        for(PlatformOrderExtra platformOrderExtra : platformOrderExtras){
            PlatformOrderExtraDto extraDto = new PlatformOrderExtraDto(platformOrderExtra);
            extraDtos.add(extraDto);
        }
        result.put("order", new PlatformOrderDto(platformOrder));
        result.put("orderDetail", platformOrderDetailDtos);
        result.put("orderExtra", extraDtos);
        return new JSONObject(result).toString();
    }

    @Override
    public void articleActived(String articleId, Integer actived) {
        articleService.setActivated(articleId,actived);
    }

    @Override
    public void articleEmpty(String articleId) {
        Article article = articleService.selectById(articleId);
        articleService.clearStock(articleId,article.getShopDetailId());
    }

    @Override
    public void articleEdit(String articleId, Integer count) {
        Article article = articleService.selectById(articleId);
        articleService.editStock(articleId,count,article.getShopDetailId());
    }

    @Override
    public void printSuccess(String orderId) {
        Order order = orderService.selectById(orderId);
        Brand brand = brandService.selectById(order.getBrandId());
        BrandSetting brandSetting = brandSettingService.selectByBrandId(brand.getId());
        AccountSetting accountSetting = accountSettingService.selectByBrandSettingId(brandSetting.getId());
        try {
            orderService.printSuccess(orderId,brandSetting.getOpenBrandAccount() == 1,accountSetting);
        } catch (AppException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void syncPosOrder(String data) {
        JSONObject json = new JSONObject(data);
        OrderDto orderDto = JSON.parseObject(json.get("order").toString(), OrderDto.class);
        Order order = new Order(orderDto);
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        order.setOrderMode(shopDetail.getShopMode());
        order.setReductionAmount(BigDecimal.valueOf(0));
        order.setBrandId(json.getString("brandId"));
        List<OrderItemDto> orderItemDtos =  orderDto.getOrderItem();
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemDto orderItemDto : orderItemDtos){
            OrderItem orderItem = new OrderItem(orderItemDto);
            orderItems.add(orderItem);
        }
        if(!StringUtils.isEmpty(orderDto.getParentOrderId())){
            //子订单
            Order parent = orderService.selectById(order.getParentOrderId());
            order.setVerCode(parent.getVerCode());
            orderService.insert(order);
            orderItemService.insertItems(orderItems);
            updateParent(order);

        }else{
            //主订单
            if(StringUtils.isEmpty(order.getVerCode())){
                order.setVerCode(generateString(5));
            }
            orderService.insert(order);
            orderItemService.insertItems(orderItems);
        }
    }

    @Override
    public void syncPosOrderPay(String data) {
        JSONObject json = new JSONObject(data);
        Order order = orderService.selectById(json.getString("orderId"));
        if(order != null && order.getOrderState() == OrderState.SUBMIT){
            List<OrderPaymentDto> orderPaymentDtos = JSON.parseArray(json.get("orderPayment").toString(), OrderPaymentDto.class);
            for(OrderPaymentDto orderPaymentDto : orderPaymentDtos){
                OrderPaymentItem orderPaymentItem = new OrderPaymentItem(orderPaymentDto);
                orderPaymentItemService.insert(orderPaymentItem);
            }
            order.setOrderState(OrderState.PAYMENT);
            order.setPaymentAmount(BigDecimal.valueOf(0));
            order.setAllowCancel(false);
            order.setAllowContinueOrder(false);
            orderService.update(order);
            if(!StringUtils.isEmpty(order.getParentOrderId())){
                updateParent(order);
            }
            updateChild(order);


        }
    }

    private void updateParent(Order order){
        Order parent = orderService.selectById(order.getParentOrderId());
        parent.setAmountWithChildren(parent.getAmountWithChildren().subtract(order.getRefundMoney()));
        parent.setCountWithChild(parent.getCountWithChild() - order.getOrderItems().size());
        orderService.update(parent);
    }


    private void updateChild(Order order) {
        List<Order> orders = orderService.selectByParentId(order.getId(), order.getPayType());
        if(!CollectionUtils.isEmpty(orders)){
            for (Order child : orders) {
                if (child.getOrderState() < OrderState.PAYMENT) {
                    child.setOrderState(OrderState.PAYMENT);
                    child.setPaymentAmount(BigDecimal.valueOf(0));
                    child.setAllowCancel(false);
                    child.setAllowContinueOrder(false);
                    orderService.update(child);
                }
            }
        }

    }

    @Override
    public void syncPosRefundOrder(String data) {

    }

    @Override
    public void syncPosConfirmOrder(String orderId) {
        Order order = orderService.selectById(orderId);
        if(order != null && order.getOrderState() == OrderState.SUBMIT && order.getIsConfirm() == Common.NO){
            orderService.confirmOrderPos(orderId);
        }
    }

    @Override
    public void test() {
        Order order = new Order();
        order.setId("00b8a27437cf460c93910bdc2489d061");
        order.setBrandId("31946c940e194311b117e3fff5327215");
        order.setShopDetailId("31164cebcc4b422685e8d9a32db12ab8");
        MQMessageProducer.sendCreateOrderMessage(order);
    }

}
