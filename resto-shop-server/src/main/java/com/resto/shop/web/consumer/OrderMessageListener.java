package com.resto.shop.web.consumer;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.ProductionStatus;
import com.resto.shop.web.model.Appraise;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.resto.brand.core.util.MQSetting;
import com.resto.shop.web.constant.OrderState;
import com.resto.shop.web.datasource.DataSourceContextHolder;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Component
public class OrderMessageListener implements MessageListener{
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	OrderService orderService;

	@Resource
	WechatConfigService wechatConfigService;
	@Resource
	BrandSettingService brandSettingService;

	@Resource
	CustomerService customerService;

	@Override
	public Action consume(Message message, ConsumeContext context) {
		Logger log = LoggerFactory.getLogger(getClass());
		
		log.info("接收到队列消息:"+message.getTag()+"@"+message);
		try {
			return executeMessage(message);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error("字符编码转换错误:"+e.getMessage());
		}
		return Action.CommitMessage;
	}
	
	public Action executeMessage(Message message) throws UnsupportedEncodingException{
		String tag = message.getTag();
		if(tag.equals(MQSetting.TAG_CANCEL_ORDER)){ //取消订单消息
			return executeCancelOrder(message);
		}else if(tag.equals(MQSetting.TAG_AUTO_CONFIRM_ORDER)){
			return executeAutoConfirmOrder(message);
		}else if(tag.equals(MQSetting.TAG_NOT_PRINT_ORDER)){
			return executeChangeProductionState(message);
		}else if(tag.equals(MQSetting.TAG_NOT_ALLOW_CONTINUE)){
			return executeNotAllowContinue(message);
		}else if(tag.equals(MQSetting.TAG_SHOW_ORDER)){
			return executeShowComment(message);
		}else if(tag.equals(MQSetting.TAG_AUTO_REFUND_ORDER)){
			return executeAutoRefundOrder(message);
		}
		return Action.CommitMessage;
	}

	private Action executeAutoRefundOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		JSONObject obj =JSONObject.parseObject(msg);
		String brandId = obj.getString("brandId");
		DataSourceContextHolder.setDataSourceName(brandId);
		Order order = orderService.selectById(obj.getString("orderId"));
		if(order.getOrderState()==OrderState.PAYMENT &&
				order.getProductionStatus() == ProductionStatus.PRINTED){
			log.info("款项自动退还到相应账户:"+obj.getString("orderId"));
			orderService.autoRefundOrder(obj.getString("orderId"));
		}else{
			log.info("款项自动退还到相应账户失败，订单状态不是已付款或商品状态不是已付款未下单");
		}
		return Action.CommitMessage;


	}

	private Action executeNotAllowContinue(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		orderService.updateAllowContinue(order.getId(),false);
		return Action.CommitMessage;
	}

	private Action executeShowComment(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Appraise appraise = JSON.parseObject(msg, Appraise.class);
		DataSourceContextHolder.setDataSourceName(appraise.getBrandId());
		sendShareMsg(appraise);

		return Action.CommitMessage;
	}

	private void sendShareMsg(Appraise appraise){
		StringBuffer msg = new StringBuffer("感谢您的五星评价，分享好友\n");
		BrandSetting setting = brandSettingService.selectByBrandId(appraise.getBrandId());
		WechatConfig config = wechatConfigService.selectByBrandId(appraise.getBrandId());
		Customer customer = customerService.selectById(appraise.getCustomerId());
		msg.append("<a href='"+setting.getWechatWelcomeUrl()+"?subpage=home&dialog=share&appraiseId="+appraise.getId()+"'>再次领取红包</a>");
		log.info("异步发送分享好评微信通知ID:"+appraise.getId()+" 内容:"+msg);
		WeChatUtils.sendCustomerMsgASync(msg.toString(),customer.getWechatId(),config.getAppid(),config.getAppsecret());
	}

	private Action executeChangeProductionState(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		orderService.changePushOrder(order);
		return Action.CommitMessage;
	}

	private Action executeAutoConfirmOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		Order order = JSON.parseObject(msg, Order.class);
		DataSourceContextHolder.setDataSourceName(order.getBrandId());
		log.info("执行自动确认逻辑"+order.getId());
		orderService.confirmOrder(order);
		return Action.CommitMessage;
	}

	private Action executeCancelOrder(Message message) throws UnsupportedEncodingException {
		String 	msg = new String(message.getBody(),MQSetting.DEFAULT_CHAT_SET);
		JSONObject obj =JSONObject.parseObject(msg);
		String brandId = obj.getString("brandId");
		DataSourceContextHolder.setDataSourceName(brandId);
		Order order = orderService.selectById(obj.getString("orderId"));
		if(order.getOrderState()==OrderState.SUBMIT){
			log.info("自动取消订单:"+obj.getString("orderId"));
			orderService.cancelOrder(obj.getString("orderId"));
		}else{
			log.info("自动取消订单失败，订单状态不是已提交");
		}
		return Action.CommitMessage;
	}
}
