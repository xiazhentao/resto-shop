package com.resto.shop.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONObject;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.BrandUser;
import com.resto.brand.web.model.SmsAcount;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.BrandUserService;
import com.resto.brand.web.service.SmsAcountService;
import com.resto.shop.web.constant.SmsLogType;
import com.resto.shop.web.constant.SmsNumNotice;
import com.resto.shop.web.dao.SmsLogMapper;
import com.resto.shop.web.model.SmsLog;
import com.resto.shop.web.service.SmsLogService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class SmsLogServiceImpl extends GenericServiceImpl<SmsLog, Long> implements SmsLogService {

    @Resource
    private SmsLogMapper smslogMapper;
    
    @Resource
    BrandService brandService;
    
    @Resource
    BrandUserService brandUserService;
    
    @Resource
    BrandSettingService brandSettingService;
    
    @Resource
    SmsAcountService smsAcountService;
    
    
    
    @Override
    public GenericDao<SmsLog, Long> getDao() {
        return smslogMapper;
    }

    
    
	@Override
	public String sendCode(String phone, String code, String brandId,String shopId) {
		Brand b = brandService.selectById(brandId);
		BrandSetting brandSetting = brandSettingService.selectByBrandId(b.getId());
		//查询
		BrandUser brandUser = brandUserService.selectById(b.getBrandUserId());
		//商家给客户发短信
		String string = sendMsg(brandSetting.getSmsSign(), b.getBrandName(), code, phone,brandUser);
		SmsLog smsLog = new SmsLog();
		smsLog.setBrandId(brandId);
		smsLog.setShopDetailId(shopId);
		smsLog.setContent(code);
		smsLog.setSmsType(SmsLog.CODE);
		smsLog.setCreateTime(new Date());
		smsLog.setPhone(phone);
		smsLog.setSmsResult(string);
		JSONObject obj = new JSONObject(string);
		if(obj.optBoolean("success",false)){
			
		}
		log.info("短信发送结果:"+string);
		try{
			insert(smsLog);
			//更新短信账户的信息
			smsAcountService.updateByBrandId(brandId);
			//判断是否要提醒商家充值短信账户
			sendNotice(brandId,brandUser);
		}catch(Exception e){
			log.error("发送短信失败:"+e.getMessage());
		}
		return string;
	}

	private void sendNotice(String brandId,BrandUser brandUser) {
		SmsAcount smsAcount = smsAcountService.selectByBrandId(brandId);
		switch (smsAcount.getRemainderNum()) {
		case SmsNumNotice.NOTICE_FIRST:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你的余额不足50条", brandUser.getPhone());
			break;
		case SmsNumNotice.NOTICE_SECOND:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你的余额不足20条", brandUser.getPhone());
			break;
		case SmsNumNotice.NOTICE_LAST:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你的余额不足10条", brandUser.getPhone());
			break;
		default:
			break;
		}
		
	}

	/**
	 * 这个方法做增强
	 */
	public String sendMsg(String sign,String serviceName,String code,String phone,BrandUser brandUser){
		//判断该品牌账户的余额是否充足
		SmsAcount smsAcount = smsAcountService.selectByBrandId(brandUser.getBrandId());
		switch (smsAcount.getRemainderNum()) {
		case SmsNumNotice.NOTICE_OWING_FIRST:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你已欠费10条", brandUser.getPhone());
			break;
		case SmsNumNotice.NOTICE_OWING_SECOND:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你已欠费20条", brandUser.getPhone());
			break;
		case SmsNumNotice.NOTICE_OWING_LAST:
			SMSUtils.sendNoticeToBrand("餐加", "餐加咨询管理", "你已欠费50条", brandUser.getPhone());
			return "";
		default:
			break;
		}
		
		return SMSUtils.sendCode(sign, serviceName, code, phone);
	}
	
	@Override
	public List<SmsLog> selectListByShopId(String shopId) {
		
		return smslogMapper.selectListByShopId(shopId);
	}

	@Override
	public List<SmsLog> selectListByShopIdAndDate(String ShopId) {
		Date begin = DateUtil.getDateBegin(DateUtil.getAfterDayDate(new Date(), -2));
		return smslogMapper.selectListByShopIdAndDate(ShopId,begin);
	}

	@Override
	public List<SmsLog> selectListWhere(String begin,String end,String shopIds) {
		Date beginDate = DateUtil.getformatBeginDate(begin);
		Date endDate = DateUtil.getformatEndDate(end);
		String[] temp = shopIds.split(","); 
		//查询短信记录
		List<SmsLog> list =  smslogMapper.selectListByWhere(beginDate, endDate, temp);
		for (SmsLog smsLog : list) {
			smsLog.setSmsLogTyPeName(SmsLogType.getSmsLogTypeName(smsLog.getSmsType()));
		}
		
		return list;
		
	}

	@Override
	public List<SmsLog> selecByBrandId(String brandId) {
		List<SmsLog> list = smslogMapper.selectListByBrandId(brandId);
		for (SmsLog smsLog : list) {
			smsLog.setSmsLogTyPeName(SmsLogType.getSmsLogTypeName(smsLog.getSmsType()));
		}
		return list;
	}
}
