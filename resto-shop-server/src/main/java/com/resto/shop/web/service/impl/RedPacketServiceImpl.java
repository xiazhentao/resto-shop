package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.shop.web.constant.PayMode;
import com.resto.shop.web.constant.RedType;
import com.resto.shop.web.dao.RedPacketMapper;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;
import com.resto.shop.web.model.RedPacket;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.RedPacketService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RpcService
public class RedPacketServiceImpl extends GenericServiceImpl<RedPacket, String> implements RedPacketService {

    @Resource
    private RedPacketMapper redPacketMapper;

    @Resource
    private OrderPaymentItemService orderPaymentItemService;

    @Override
    public GenericDao<RedPacket, String> getDao() {
        return redPacketMapper;
    }

    @Override
    public void useRedPacketPay(BigDecimal redPay, String customerId, Order order) {
        //扣除红包，扣除顺序 评论红包-->分享红包-->退菜红包
        Integer[] redType = {0,1,2};
        for(Integer type : redType){
            boolean flg = useRedPacket(type,redPay,customerId,order);
            //如果已扣完则不再扣除
            if(flg){
                break;
            }
        }
    }

    private boolean useRedPacket(Integer redType, BigDecimal redPay, String customerId, Order order){
        RedPacket redPacket = redPacketMapper.selectFirstRedPacket(customerId,redType);
        if (redPacket == null && redPay.compareTo(BigDecimal.ZERO) > 0){
            return false;
        }
        Map<String, Object> param = new HashMap<>();
        if(redPay.compareTo(redPacket.getRedRemainderMoney()) >= 0){
            param.put("id",redPacket.getId());
            param.put("redRemainderMoney",BigDecimal.ZERO);
            param.put("finishTime",new Date());
            redPacketMapper.updateRedRemainderMoney(param);
            OrderPaymentItem item = new OrderPaymentItem();
			item.setId(ApplicationUtils.randomUUID());
			item.setOrderId(order.getId());
            switch (redType){
                case RedType.APPRAISE_RED:
                    item.setPaymentModeId(PayMode.APPRAISE_RED_PAY);
                    break;
                case RedType.SHARE_RED:
                    item.setPaymentModeId(PayMode.SHARE_RED_PAY);
                    break;
                case RedType.REFUND_ARTICLE_RED:
                    item.setPaymentModeId(PayMode.REFUND_ARTICLE_RED_PAY);
                    break;
            }
			item.setPayTime(new Date());
			item.setPayValue(redPacket.getRedRemainderMoney());
			item.setRemark(""+ RedType.GETREDTYPE.get(redType)+"支付:" + item.getPayValue());
			item.setResultData(redPacket.getId());
			orderPaymentItemService.insert(item);
            redPay = redPay.subtract(redPacket.getRedRemainderMoney());
            if(redPay.compareTo(BigDecimal.ZERO) > 0) {
                return useRedPacket(redType, redPay, customerId, order);
            }
        }else{
            param.put("id",redPacket.getId());
            param.put("redRemainderMoney",redPacket.getRedRemainderMoney().subtract(redPay));
            redPacketMapper.updateRedRemainderMoney(param);
            OrderPaymentItem item = new OrderPaymentItem();
            item.setId(ApplicationUtils.randomUUID());
            item.setOrderId(order.getId());
            switch (redType){
                case RedType.APPRAISE_RED:
                    item.setPaymentModeId(PayMode.APPRAISE_RED_PAY);
                    break;
                case RedType.SHARE_RED:
                    item.setPaymentModeId(PayMode.SHARE_RED_PAY);
                    break;
                case RedType.REFUND_ARTICLE_RED:
                    item.setPaymentModeId(PayMode.REFUND_ARTICLE_RED_PAY);
                    break;
            }
            item.setPayTime(new Date());
            item.setPayValue(redPay);
            item.setRemark(""+ RedType.GETREDTYPE.get(redType)+"支付:" + item.getPayValue());
            item.setResultData(redPacket.getId());
            orderPaymentItemService.insert(item);
        }
        return true;
    }

    @Override
    public List<RedPacketDto> selectRedPacketLog(Map<String, Object> selectMap) {
        return redPacketMapper.selectRedPacketLog(selectMap);
    }

    @Override
    public Map<String, Object> selectUseRedOrder(Map<String, Object> selectMap) {
        return redPacketMapper.selectUseRedOrder(selectMap);
    }
}