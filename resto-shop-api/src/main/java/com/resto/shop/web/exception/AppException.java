package com.resto.shop.web.exception;

import com.resto.shop.web.exception.AppException.ErrorMsg;

public class AppException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorMsg msg=new ErrorMsg(0, "");
	public static final ErrorMsg CUSTOMER_NOT_EXISTS= new ErrorMsg(1,"用户不存在！");
	public static final ErrorMsg NOT_BIND_PHONE = new ErrorMsg(2,"未绑定手机");
	public static final ErrorMsg ORDER_ITEMS_EMPTY = new ErrorMsg(3,"没有订单项");
	public static final ErrorMsg COUPON_IS_USED = new ErrorMsg(4,"优惠卷已使用");
	public static final ErrorMsg COUPON_MODE_ERR = new ErrorMsg(5,"优惠卷使用模式错误");
	public static final ErrorMsg COUPON_MIN_AMOUNT_ERR = new ErrorMsg(6,"订单金额不足以使用优惠卷");
	public static final ErrorMsg COUPON_NOT_USEACCOUNT = new ErrorMsg(7,"不可以和余额一起使用");
	public static final ErrorMsg COUPON_IS_EXPIRE = new ErrorMsg(8,"优惠卷已过期");
	public static final ErrorMsg COUPON_TIME_ERR = new ErrorMsg(9,"优惠卷使用时间段错误");
	public static final ErrorMsg UNSUPPORT_ITEM_TYPE = new ErrorMsg(10,"不支持的餐品类型！");
	public static final ErrorMsg ORDER_STATE_ERR = new ErrorMsg(11, "订单状态异常");
	public static final ErrorMsg ORDER_MODE_CHECK = new ErrorMsg(12, "");
	public static final ErrorMsg ORDER_NOT_ALL_APPRAISE = new ErrorMsg(13, "该订单不允许评论");;
	
	
	public AppException(ErrorMsg msg) {
		this.msg=msg;
	}
	
	public AppException(ErrorMsg msg, String string) {
		this.msg = new ErrorMsg(msg.getCode(), string);
	}

	@Override
	public String getMessage() {
		return this.msg.toString();
	}
	
	
	static class ErrorMsg{
		private final int code;
		private final String msg;
		ErrorMsg(int code, String msg){
			this.code=code;
			this.msg=msg;
		}
		public int getCode() {
			return code;
		}
		public String getMsg() {
			return msg;
		}
		@Override
		public String toString() {
			return "{code:"+code+",msg:\""+msg+"\"}";
		}
	}
}
