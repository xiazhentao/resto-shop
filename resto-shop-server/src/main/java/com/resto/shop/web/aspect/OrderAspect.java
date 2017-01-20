package com.resto.shop.web.aspect;

import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.*;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShareSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.*;
import com.resto.shop.web.container.OrderProductionStateContainer;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.*;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Aspect
public class OrderAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    ShopCartService shopCartService;
    @Resource
    CustomerService customerService;
    @Resource
    WechatConfigService wechatConfigService;
    @Resource
    BrandSettingService brandSettingService;
    @Resource
    OrderProductionStateContainer orderProductionStateContainer;
    @Resource
    OrderItemService orderItemService;
    @Resource
    ShopDetailService shopDetailService;
    @Resource
    ShareSettingService shareSettingService;
    @Resource
    RedConfigService redConfigService;
    @Resource
    OrderService orderService;
    @Resource
    OrderPaymentItemService orderPaymentItemService;
    @Resource
    LogBaseService logBaseService;


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrder(..))")
    public void createOrder() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.createOrderByEmployee(..))")
    public void createOrderByEmployee() {
    }

    ;

    @AfterReturning(value = "createOrderByEmployee()", returning = "jsonResult")
    public void createOrderByEmployeeAround(JSONResult jsonResult) throws Throwable {
        if (jsonResult.isSuccess() == true) {
            Order order = (Order) jsonResult.getData();
            shopCartService.clearShopCartGeekPos(String.valueOf(order.getEmployeeId()), order.getShopDetailId());
            //出单时减少库存
            Boolean updateStockSuccess = false;
            updateStockSuccess = orderService.updateStock(orderService.getOrderInfo(order.getId()));
            if (!updateStockSuccess) {
                log.info("库存变更失败:" + order.getId());
            }
        }
    }

    @AfterReturning(value = "createOrder()", returning = "jsonResult")
    public void createOrderAround(JSONResult jsonResult) throws Throwable {
        if (jsonResult.isSuccess() == true) {
            Order order = (Order) jsonResult.getData();
            if(order.getOrderState() == OrderState.PAYMENT && order.getOrderMode() != ShopMode.HOUFU_ORDER){
                shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
            }
            if(order.getOrderState() == OrderState.SUBMIT && order.getOrderMode() == ShopMode.HOUFU_ORDER){
                shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
            }
            //订单在每天0点未被消费系统自动取消订单（款项自动退还到相应账户）
            log.info("当天24小时开启自动退款:" + order.getId());
            if (order.getOrderMode() != ShopMode.HOUFU_ORDER) {
                MQMessageProducer.sendAutoRefundMsg(order.getBrandId(), order.getId(), order.getCustomerId());
            }
            //自动取消订单，不包含后付款模式
            if (order.getOrderState().equals(OrderState.SUBMIT)&&(order.getOrderMode()!=ShopMode.HOUFU_ORDER||order.getOrderMode()!=ShopMode.BOSS_ORDER)) {//未支付和未完全支付的订单，不包括后付款模式
            	long delay = 1000*60*60*2;//两个小时后自动取消订单
                MQMessageProducer.sendAutoCloseMsg(order.getId(),order.getBrandId(),delay);
            } else if (order.getOrderState().equals((OrderState.PAYMENT))&&(order.getOrderMode()!=ShopMode.TABLE_MODE||order.getOrderMode()!=ShopMode.BOSS_ORDER)) { //坐下点餐模式不发送
                sendPaySuccessMsg(order);
            }
            if(order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY){
                shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
                MQMessageProducer.sendPlaceOrderMessage(order);
            }
            //出单时减少库存
            Boolean updateStockSuccess = false;
            updateStockSuccess = orderService.updateStock(orderService.getOrderInfo(order.getId()));
            if (!updateStockSuccess) {
                log.info("库存变更失败:" + order.getId());
            }
        }
    }

    private void sendPaySuccessMsg(Order order) {
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        StringBuffer msg = new StringBuffer();
        msg.append("订单编号:\n" + order.getSerialNumber() + "\n");
        if (order.getOrderMode() != null) {
            switch (order.getOrderMode()) {
                case ShopMode.TABLE_MODE:
                    msg.append("桌号:" + order.getTableNumber() + "\n");
                    break;
                default:
                    msg.append("取餐码：" + order.getVerCode() + "\n");
                    break;
            }
        }
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
        if (order.getShopName() == null || "".equals(order.getShopName())) {
            order.setShopName(shopDetail.getName());
        }
        msg.append("就餐店铺：" + order.getShopName() + "\n");
        msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");

        BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
        if(setting.getIsUseServicePrice() == 1 && order.getServicePrice().compareTo(BigDecimal.ZERO) != 0 && order.getDistributionModeId() == 1){
            msg.append(setting.getServiceName()+"：" + order.getServicePrice() + "\n");
        }
        if(setting.getIsMealFee() == 1 && order.getMealFeePrice().compareTo(BigDecimal.ZERO) != 0 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1){
            msg.append(shopDetail.getMealFeeName()+"：333" + order.getMealFeePrice() + "\n");
        }
        msg.append("订单明细：\n");
        List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
        for (OrderItem item : orderItem) {
            msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
        }
        msg.append("订单金额：" + order.getOrderMoney() + "\n");
        if(order.getOrderMode() == ShopMode.BOSS_ORDER){
            String url = "";
            if(order.getParentOrderId() == null){
                url = setting.getWechatWelcomeUrl()+"?orderBossId=" + order.getId() + "&dialog=closeRedPacket&shopId=" +order.getShopDetailId();
            }else{
                Order o = orderService.selectById(order.getParentOrderId());
                url = setting.getWechatWelcomeUrl()+"?orderBossId=" + o.getId() + "&dialog=closeRedPacket&shopId=" +order.getShopDetailId();
            }
            msg.append("<a href='" + url+ "'>点击这里进行\"加菜\"或\"买单\"</a> \n");
        }
        try {
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            log.info("订单支付完成后，发送客服消息:" + order.getId() + " -- " + result);
        } catch (Exception e) {
            log.error("发送客服消息失败:" + e.getMessage());
        }

//        Customer customer = customerService.selectById(order.getCustomerId());
//        WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
//        ShopDetail shop = shopDetailService.selectById(order.getShopDetailId());
//
//        String res = WeChatUtils.getTemplate("OPENTM408705883", config.getAppid(), config.getAppsecret());
//        JSONObject access = new JSONObject(res);
//        String templateId = access.optString("template_id");
//
//        String jumpUrl = "http://www.baidu.com";
//        Map<String, Map<String, Object>> content = new HashMap<String, Map<String, Object>>();
//        Map<String, Object> first = new HashMap<String, Object>();
//
//        if(order.getParentOrderId() == null){
//            first.put("value", "下单成功！\n您于"+DateUtil.formatDate(order.getCreateTime(),"yyyy-MM-dd HH:mm:ss")+"的订单已下厨，请稍候~");
//        }else{
//            first.put("value", "加菜成功！\n您于"+DateUtil.formatDate(order.getCreateTime(),"yyyy-MM-dd HH:mm:ss")+"的订单已下厨，请稍候~");
//        }
//        first.put("color", "#00DB00");
//        Map<String, Object> keyword1 = new HashMap<String, Object>();
//        keyword1.put("value", order.getSerialNumber());
//        keyword1.put("color", "#000000");
//        Map<String, Object> keyword2 = new HashMap<String, Object>();
//        keyword2.put("value", shop.getName());
//        keyword2.put("color", "#000000");
//        Map<String, Object> keyword3 = new HashMap<String, Object>();
//        if(order.getOrderMode() == 2){
//            keyword3.put("value", order.getVerCode());
//        }else{
//            keyword3.put("value", order.getTableNumber());
//        }
//        keyword3.put("color", "#000000");
//        Map<String, Object> keyword4 = new HashMap<String, Object>();
//        keyword4.put("value", "￥" + order.getOrderState());
//        keyword4.put("color", "#000000");
//        Map<String, Object> keyword5 = new HashMap<String, Object>();
//        List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
//        StringBuffer msg = new StringBuffer();
//        for (int i=0; i< orderItem.size(); i++) {
//            OrderItem item = orderItem.get(i);
//            if(i == 0){
//                msg.append(" " + item.getArticleName() + "x" + item.getCount() + "\n");
//            }else{
//                msg.append("\t\t\t" + item.getArticleName() + "x" + item.getCount() + "\n");
//            }
//        }
//        keyword5.put("value", msg.toString());
//        keyword5.put("color", "#000000");
//        Map<String, Object> remark = new HashMap<String, Object>();
//        remark.put("value", "点击结果进行\"加菜\"或\"买单\"");
//        remark.put("color", "#173177");
//        content.put("first", first);
//        content.put("keyword1", keyword1);
//        content.put("keyword2", keyword2);
//        content.put("keyword3", keyword3);
//        content.put("keyword4", keyword4);
//        content.put("keyword5", keyword5);
//        content.put("remark", remark);
//        String result = WeChatUtils.sendTemplate(customer.getWechatId(), templateId, jumpUrl, content, config.getAppid(), config.getAppsecret());
//
//        String data = WeChatUtils.delTemplate(templateId, config.getAppid(), config.getAppsecret());
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.orderWxPaySuccess(..))")
    public void orderWxPaySuccess() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.afterPay(..))")
    public void afterPay() {

    };


    @AfterReturning(value = "afterPay()", returning = "order")
    public void afterPay(Order order) {
        if(order.getOrderState() == OrderState.PAYMENT){ //已支付
            MQMessageProducer.sendPlaceOrderMessage(order);
        }
    }




    @AfterReturning(value = "orderWxPaySuccess()", returning = "order")
    public void orderPayAfter(Order order) {
        if (order != null && order.getOrderState().equals(OrderState.PAYMENT) &&
                (ShopMode.TABLE_MODE != order.getOrderMode() || ShopMode.BOSS_ORDER != order.getOrderMode())) {//坐下点餐模式不发送该消息
            sendPaySuccessMsg(order);
        }
        if(order != null && order.getPayMode() != null && order.getPayMode() == OrderPayMode.ALI_PAY &&
                order.getOrderState().equals(OrderState.PAYMENT)
                && order.getProductionStatus().equals(ProductionStatus.HAS_ORDER)){
            MQMessageProducer.sendPlaceOrderMessage(order);
        }

        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(order.getShopDetailId());
        if(order != null  && order.getOrderState() == OrderState.PAYMENT
                && (order.getTableNumber() != null || (order.getDistributionModeId() == DistributionType.TAKE_IT_SELF && shopDetail.getContinueOrderScan() == Common.NO))
                && (order.getOrderMode() == ShopMode.TABLE_MODE || order.getOrderMode() == ShopMode.BOSS_ORDER )){
            MQMessageProducer.sendPlaceOrderMessage(order);
        }

        if(order.getOrderMode() != ShopMode.HOUFU_ORDER){
            shopCartService.clearShopCart(order.getCustomerId(), order.getShopDetailId());
        }

        if(order != null  && order.getOrderState() == OrderState.PAYMENT
                && order.getOrderMode() == ShopMode.CALL_NUMBER){
            MQMessageProducer.sendPlaceOrderMessage(order);
        }

        if (order.getOrderMode() == ShopMode.HOUFU_ORDER) {
//            MQMessageProducer.sendPlaceOrderMessage(order);
            orderService.payOrderWXModeFive(order.getId());
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.pushOrder(..))")
    public void pushOrder() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.callNumber(..))")
    public void callNumber() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.printSuccess(..))")
    public void printSuccess() {
    }

    ;


    @Pointcut("execution(* com.resto.shop.web.service.AccountService.houFuPayOrder(..))")
    public void houFuPayOrder() {
    }

    ;


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payOrderModeFive(..))")
    public void payOrderModeFive() {
    }

    ;

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payOrderWXModeFive(..))")
    public void payOrderWXModeFive() {
    }

    ;


    @Pointcut("execution(* com.resto.shop.web.service.OrderService.payPrice(..))")
    public void payPrice() {
    }

    ;




    @AfterReturning(value = "callNumber()", returning = "order")
    public void createCallMessage(Order order) throws Throwable {
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(order.getBrandId());
        WeChatUtils.sendCustomerMsgASync("您的餐品已经准备好了，请尽快到吧台取餐！", customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        WeChatUtils.sendCustomerWaitNumberMsg("您的餐品已经准备好了，请尽快到吧台取餐！", customer.getWechatId(), config.getAppid(), config.getAppsecret());
//		MQMessageProducer.sendCallMessage(order.getBrandId(),order.getId(),order.getCustomerId());
        ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId()); //根据订单找到对应的店铺
        if(shopDetail.getIsPush() == Common.YES){ //开启就餐提醒
        	MQMessageProducer.sendRemindMsg(order,shopDetail.getPushTime() * 1000);	
        }
        
    }

    @AfterReturning(value = "pushOrder()||callNumber()||printSuccess()||payOrderModeFive()||payPrice()|| createOrderByEmployee()||payOrderWXModeFive()", returning = "order")
    public void pushOrderAfter(Order order) throws Throwable {
        if (order != null) {
            if (ProductionStatus.HAS_ORDER == order.getProductionStatus()) {
                if(order.getPayMode() != null && order.getPayMode() == OrderPayMode.ALI_PAY && order.getOrderState().equals(OrderState.SUBMIT)){
                    return;
                }
//                BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
                log.info("客户下单,发送成功下单通知" + order.getId());

				if (order.getEmployeeId() == null) {
                    if(order.getPrintOrderTime() == null){
                        MQMessageProducer.sendPlaceOrderMessage(order);
                    }
				} else {
					if (order.getOrderState().equals(OrderState.PAYMENT)) {
						MQMessageProducer.sendPlaceOrderMessage(order);
					}
				}
//				log.info("客户下单，添加自动拒绝5分钟未打印的订单");
//				MQMessageProducer.sendNotPrintedMessage(order,1000*60*5); //延迟五分钟，检测订单是否已经打印
                if ((order.getOrderMode() == ShopMode.TABLE_MODE || order.getOrderMode() == ShopMode.BOSS_ORDER) && order.getEmployeeId() == null) {  //坐下点餐在立即下单的时候，发送支付成功消息通知
                    log.info("坐下点餐在立即下单的时候，发送支付成功消息通知:" + order.getId());
                    sendPaySuccessMsg(order);
                }
                log.info("检查打印异常");
//                int times = setting.getReconnectTimes();
//                int seconds = setting.getReconnectSecond();
//                for (int i = 0; i < times; i++) {
//                    MQMessageProducer.checkPlaceOrderMessage(order, (i + 1) * seconds * 1000L, seconds * times * 1000L);
//                }
            } else if (ProductionStatus.PRINTED == order.getProductionStatus()) {
                BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
                log.info("发送禁止加菜:" + setting.getCloseContinueTime() + "s 后发送");
                if (order.getOrderMode() != ShopMode.HOUFU_ORDER && order.getPayType() != PayType.NOPAY) {
                    MQMessageProducer.sendNotAllowContinueMessage(order, 1000 * setting.getCloseContinueTime()); //延迟两小时，禁止继续加菜
                    MQMessageProducer.sendPlaceOrderMessage(order);
                    MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
                } else {
                    if (order.getOrderState() == OrderState.PAYMENT) {
                        MQMessageProducer.sendAutoConfirmOrder(order, setting.getAutoConfirmTime() * 1000);
                        MQMessageProducer.sendModelFivePaySuccess(order);
                        if(order.getPrintTimes() == 0){
                            order.setPrintTimes(order.getPrintTimes()+1);
                            orderService.update(order);
                            MQMessageProducer.sendPlaceOrderMessageAgain(order,6000);
                        }
                    }
                }
//                if(order.getOrderMode() == ShopMode.TABLE_MODE && order.getEmployeeId() == null && order.getParentOrderId() != null){
//                    sendPaySuccessMsg(order);
//                }
                if (order.getOrderMode() != null) {
                    switch (order.getOrderMode()) {
                        case ShopMode.CALL_NUMBER:
                            log.info("叫号模式,发送取餐码信息:" + order.getId());
                            sendVerCodeMsg(order);
                            break;
                        default:
                            break;
                    }
                }
                log.info("发送打印信息");
                log.info("打印成功后，发送自动确认订单通知！" + setting.getAutoConfirmTime() + "s 后发送" + ",orderId:" + order.getId());
            } else if (ProductionStatus.HAS_CALL == order.getProductionStatus()) {
                log.info("发送叫号信息");
                MQMessageProducer.sendPlaceOrderMessage(order);
            }
        }
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.confirmOrder(..))")
    public void confirmOrder() {
    }

    ;

    @AfterReturning(value = "printSuccess()", returning = "order")
    public void pushContent(Order order) {
        if (order != null
                && (order.getOrderMode() == ShopMode.HOUFU_ORDER || (order.getOrderMode() == ShopMode.BOSS_ORDER && order.getPayType() == PayType.NOPAY))
                && order.getOrderState() == OrderState.SUBMIT
                && order.getProductionStatus() == ProductionStatus.PRINTED) {
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            StringBuffer msg = new StringBuffer();
            if (order.getParentOrderId() == null) {
                msg.append("下单成功!" + "\n");
            } else {
                msg.append("加菜成功!" + "\n");
            }
            msg.append("订单编号:" + order.getSerialNumber() + "\n");
            msg.append("桌号：" + order.getTableNumber() + "\n");
            msg.append("就餐店铺：" + shopDetail.getName() + "\n");
            msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
            if(setting.getIsUseServicePrice() == 1 && order.getDistributionModeId() == 1){
                msg.append(setting.getServiceName()+"：" + order.getServicePrice() + "\n");
            }
            if(setting.getIsMealFee() == 1 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1){
                msg.append(shopDetail.getMealFeeName()+"：222" + order.getMealFeePrice() + "\n");
            }
            BigDecimal sum = order.getOrderMoney();
            List<Order> orders = orderService.selectByParentId(order.getId()); //得到子订单
            for (Order child : orders) { //遍历子订单
                sum = sum.add(child.getOrderMoney());
            }
            msg.append("订单明细：\n");
            List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : orderItem) {
                msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
            }
            msg.append("订单金额：" + sum + "\n");
            if(order.getOrderMode() == ShopMode.BOSS_ORDER){
                String url = "";
                if(order.getParentOrderId() == null){
                    url = setting.getWechatWelcomeUrl()+"?orderBossId=" + order.getId() + "&dialog=closeRedPacket&shopId=" +order.getShopDetailId();
                }else{
                    Order o = orderService.selectById(order.getParentOrderId());
                    url = setting.getWechatWelcomeUrl()+"?orderBossId=" + o.getId() + "&dialog=closeRedPacket&shopId=" +order.getShopDetailId();
                }
                msg.append("<a href='" + url+ "'>点击这里进行\"加菜\"或\"买单\"</a> \n");
            }
            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        }
    }

    //推送分享领红包，跳转到我的二维码界面
    public void scanaQRcode(WechatConfig config, Customer customer, BrandSetting setting, Order order){
        StringBuffer str=new StringBuffer();
        str.append("邀请好友扫一扫,");
        String jumpurl = setting.getWechatWelcomeUrl()+"?dialog=scanAqrCode&subpage=my&shopId=" + order.getShopDetailId();
        str.append("<a href='"+jumpurl+"'>领取奖励红包</a>");
        String result = WeChatUtils.sendCustomerMsg(str.toString(),customer.getWechatId(), config.getAppid(), config.getAppsecret());
    }

//    @AfterReturning(value = "payOrderModeFive()||payPrice()", returning = "order")
//    public void payContent(Order order) {
//        if (order != null && order.getOrderMode() == ShopMode.HOUFU_ORDER && order.getOrderState() == OrderState.PAYMENT
//                && order.getProductionStatus() == ProductionStatus.PRINTED) {
//            Customer customer = customerService.selectById(order.getCustomerId());
//            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
//            List<OrderPaymentItem> paymentItems = orderPaymentItemService.selectByOrderId(order.getId());
//            String money = "(";
//            for (OrderPaymentItem orderPaymentItem : paymentItems) {
//                money += PayMode.getPayModeName(orderPaymentItem.getPaymentModeId()) + "： " + orderPaymentItem.getPayValue() + " ";
//
//            }
//            StringBuffer msg = new StringBuffer();
//            BigDecimal sum = order.getOrderMoney();
//            List<Order> orders = orderService.selectByParentId(order.getId()); //得到子订单
//            for (Order child : orders) { //遍历子订单
//                sum = sum.add(child.getOrderMoney());
//            }
//
//            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());
//            if(setting.getIsUseServicePrice() == 1){
//                sum = sum.add(order.getServicePrice());
//            }
//
//            msg.append("您的订单").append(order.getSerialNumber()).append("已于").append(DateFormatUtils.format(paymentItems.get(0).getPayTime(), "yyyy-MM-dd HH:mm"));
//            msg.append("支付成功。订单金额：").append(sum).append(money).append(") ");
//            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
//        }
//
//    }

    @AfterReturning(value = "confirmOrder()", returning = "order")
    public void confirmOrderAfter(Order order) {
        log.info("确认订单成功后回调:" + order.getId());
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
//		RedConfig redConfig = redConfigService.selectListByShopId(order.getShopDetailId());
        if (order.getAllowAppraise()) {
            StringBuffer msg = new StringBuffer();
            msg.append("您有一个红包未领取\n");
            msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=my&dialog=redpackage&orderId=" + order.getId() + "&shopId=" + order.getShopDetailId() + "'>点击领取</a>");

            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            log.info("发送评论通知成功:" + msg + result);
            scanaQRcode(config, customer, setting, order);
        }
        try {
            if (customer.getFirstOrderTime() == null) { //分享判定
                customerService.updateFirstOrderTime(customer.getId());
                if (customer.getShareCustomer() != null) {
                    Customer shareCustomer = customerService.selectById(customer.getShareCustomer());
                    if (shareCustomer != null) {
                        ShareSetting shareSetting = shareSettingService.selectValidSettingByBrandId(customer.getBrandId());
                        if (shareSetting != null) {
                            log.info("是被分享用户，并且分享设置已启用:" + customer.getId() + " oid:" + order.getId() + " setting:" + shareSetting.getId());
                            BigDecimal rewardMoney = customerService.rewareShareCustomer(shareSetting, order, shareCustomer, customer);
                            log.info("准备发送返利通知");
                            sendRewardShareMsg(shareCustomer, customer, config, setting, rewardMoney,order);
                        } else{
                            log.info("准备发送返利通知  but品牌没有设置返利  so返利0元");
                            sendRewardShareMsg(shareCustomer, customer, config, setting, BigDecimal.ZERO,order);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("分享功能出错:" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void sendRewardShareMsg(Customer shareCustomer, Customer customer, WechatConfig config,
                                    BrandSetting setting, BigDecimal rewardMoney, Order order) {
        StringBuffer msg = new StringBuffer();
        if(rewardMoney.compareTo(BigDecimal.ZERO) != 0){
            rewardMoney = rewardMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        msg.append("<a href='" + setting.getWechatWelcomeUrl() + "?subpage=my&dialog=myYue'>")
                .append("您邀请的好友").append(customer.getNickname()).append("已到店消费，您已获得")
                .append(rewardMoney).append("元红包返利").append("</a>");
        String result = WeChatUtils.sendCustomerMsg(msg.toString(), shareCustomer.getWechatId(), config.getAppid(), config.getAppsecret());
        logBaseService.insertLogBaseInfoState(shopDetailService.selectById(order.getShopDetailId()),customer,shareCustomer.getId(),LogBaseState.FIRST_SHARE_PAY);
        log.info("发送返利通知成功:" + shareCustomer.getId() + " MSG: " + msg + result);
    }

    @Pointcut("execution(* com.resto.shop.web.service.OrderService.cancelOrderPos(..))")
    public void cancelOrderPos() {
    }

    ;

    @AfterReturning(value = "cancelOrderPos()", returning = "order")
    public void cancelOrderPosAfter(Order order) throws Throwable {
        if (order != null) {
            Customer customer = customerService.selectById(order.getCustomerId());
            WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
            ShopDetail shopDetail = shopDetailService.selectById(order.getShopDetailId());
            StringBuffer msg = new StringBuffer();
            msg.append("您好，您 " + DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm") + " 的订单" + "已被商家取消\n");
            msg.append("订单编号:\n" + order.getSerialNumber() + "\n");
            if (order.getOrderMode() != null) {
                switch (order.getOrderMode()) {
                    case ShopMode.TABLE_MODE:
                        msg.append("桌号:" + order.getTableNumber() + "\n");
                        break;
                    default:
                        msg.append("取餐码：" + order.getVerCode() + "\n");
                        break;
                }
            }
            if (order.getShopName() == null || "".equals(order.getShopName())) {
                order.setShopName(shopDetail.getName());
            }
            msg.append("就餐店铺：" + order.getShopName() + "\n");
            msg.append("订单时间：" + DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm") + "\n");
            BrandSetting setting = brandSettingService.selectByBrandId(order.getBrandId());

            if(setting.getIsUseServicePrice() == 1 && order.getDistributionModeId() == 1){
                msg.append(setting.getServiceName()+"：" + order.getServicePrice() + "\n");
            }
            if(setting.getIsMealFee() == 1 && order.getDistributionModeId() == 3 && shopDetail.getIsMealFee() == 1){
                msg.append(shopDetail.getMealFeeName()+"：111" + order.getMealFeePrice() + "\n");
            }
            msg.append("订单明细：\n");
            List<OrderItem> orderItem = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : orderItem) {
                if(item.getCount() > 0){
                    msg.append("  " + item.getArticleName() + "x" + item.getCount() + "\n");
                }
            }
            msg.append("订单金额：" + order.getOrderMoney() + "\n");

            String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
            log.info("发送订单取消通知成功:" + msg + result);

            MQMessageProducer.sendNoticeOrderMessage(order);

            if (order.getParentOrderId() != null) {  //子订单
                orderService.updateOrderChild(order.getId());
            }
//			//拒绝订单后还原库存
//			Boolean addStockSuccess  = false;
//			addStockSuccess	= orderService.addStock(orderService.getOrderInfo(order.getId()));
//			if(!addStockSuccess){
//				log.info("库存还原失败:"+order.getId());
//			}
        }
    }

    private void sendVerCodeMsg(Order order) {
        Customer customer = customerService.selectById(order.getCustomerId());
        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
        StringBuffer msg = new StringBuffer();
        msg.append("交易码:" + order.getVerCode() + "\n");
        msg.append("请留意餐厅叫号信息");
        String result = WeChatUtils.sendCustomerMsg(msg.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());
        log.info("发送取餐信息成功:" + result);
    }
}
