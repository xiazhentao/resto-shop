package com.resto.shop.web.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.DateUtil;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.core.util.WeChatUtils;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.BrandSetting;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.WechatConfig;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.BrandSettingService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.WechatConfigService;
import com.resto.shop.web.constant.CouponSource;
import com.resto.shop.web.constant.TimeCons;
import com.resto.shop.web.dao.NewCustomCouponMapper;
import com.resto.shop.web.model.Coupon;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.NewCustomCoupon;
import com.resto.shop.web.producer.MQMessageProducer;
import com.resto.shop.web.service.CouponService;
import com.resto.shop.web.service.CustomerService;
import com.resto.shop.web.service.NewCustomCouponService;

import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class NewCustomCouponServiceImpl extends GenericServiceImpl<NewCustomCoupon, Long> implements NewCustomCouponService {

    @Resource
    private NewCustomCouponMapper newcustomcouponMapper;
    
    @Resource
    private CouponService couponService;

    @Resource
    private ShopDetailService shopDetailService;

    @Resource
	private CustomerService customerService;
    
    @Resource
	private WechatConfigService wechatConfigService;
    
    @Resource
    private BrandSettingService brandSettingService;
    
    @Value("#{configProperties['orderMsg']}")
	public static String orderMsg;
    
    @Override
    public GenericDao<NewCustomCoupon, Long> getDao() {
        return newcustomcouponMapper;
    }

    @Override
    public int insertNewCustomCoupon(NewCustomCoupon newCustomCoupon) {

        return newcustomcouponMapper.insertSelective(newCustomCoupon);
    }
    
    @Override
    public List<NewCustomCoupon> selectListByBrandId(String currentBrandId) {
        List<NewCustomCoupon> list = newcustomcouponMapper.selectListByBrandId(currentBrandId);
        //查询品牌下所有的店铺
        List<ShopDetail> shopDetailList = shopDetailService.selectByBrandId(currentBrandId);

        if(!list.isEmpty()){
            for(NewCustomCoupon  newcustomcoupon : list){
                for(ShopDetail shopDetail:shopDetailList){
                    if(shopDetail.getId().equals(newcustomcoupon.getShopDetailId())){
                        newcustomcoupon.setShopName(shopDetail.getName());
                    }
                }
            }
        }

        return list;
    }




//	@Override
//	public void giftCoupon(Customer cus,Integer couponType) {
//		//根据 品牌id 查询该品牌的优惠卷配置 查询已经启用的优惠券
//	    List<NewCustomCoupon> couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
//	    //如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
//	    if(couponConfigs == null || couponConfigs.size()== 0 ){
//	    	couponType = -1;
//	    	couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
//	    }
//		//根据优惠卷配置，添加对应数量的优惠卷
//	    Date beginDate  = new Date();
//	    for(NewCustomCoupon cfg: couponConfigs){
//	        Coupon coupon = new Coupon();
//	        coupon.setName(cfg.getCouponName());
//	        coupon.setValue(cfg.getCouponValue());
//	        coupon.setMinAmount(cfg.getCouponMinMoney());
//	        coupon.setCouponType(couponType);
//	        coupon.setBeginTime(cfg.getBeginTime());
//	        coupon.setEndTime(cfg.getEndTime());
//	        coupon.setUseWithAccount(cfg.getUseWithAccount());
//	        coupon.setDistributionModeId(cfg.getDistributionModeId());
//	        coupon.setCouponSource(CouponSource.NEW_CUSTOMER_COUPON);
//	        coupon.setCustomerId(cus.getId());
//	        //优惠券时间选择的类型分配时间
//	        if(cfg.getTimeConsType()==TimeCons.MODELA){
//	        	coupon.setBeginDate(beginDate);
//		        coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,cfg.getCouponValiday()));
//	        }else if(cfg.getTimeConsType()==TimeCons.MODELB){
//	        	coupon.setBeginDate(cfg.getBeginDateTime());
//	        	coupon.setEndDate(cfg.getEndDateTime());
//	        }
//
//	        for(int i=0;i<cfg.getCouponNumber();i++){
//	            couponService.insertCoupon(coupon);
//	        }
//
//	    }
//
//	}

    public void giftCoupon(Customer cus,Integer couponType,String shopId) {
        //根据 店铺id 查询该店铺的优惠卷配置 查询已经启用的优惠券
        List<NewCustomCoupon> couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
        //如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
        if(couponConfigs == null || couponConfigs.size()== 0 ){
            couponType = -1;
            couponConfigs = newcustomcouponMapper.selectListByBrandIdAndIsActive(cus.getBrandId(),couponType);
        }
        //根据优惠卷配置，添加对应数量的优惠卷
        Date beginDate  = new Date();
        for(NewCustomCoupon cfg: couponConfigs){
            //如果是品牌优惠券设置或者是当前店铺的优惠券设置
            if(cfg.getIsBrand()==1||shopId.equals(cfg.getShopDetailId())){
                Coupon coupon = new Coupon();
                coupon.setName(cfg.getCouponName());
                coupon.setValue(cfg.getCouponValue());
                coupon.setMinAmount(cfg.getCouponMinMoney());
                coupon.setCouponType(couponType);
                coupon.setBeginTime(cfg.getBeginTime());
                coupon.setEndTime(cfg.getEndTime());
                coupon.setUseWithAccount(cfg.getUseWithAccount());
                coupon.setDistributionModeId(cfg.getDistributionModeId());
                coupon.setCouponSource(CouponSource.NEW_CUSTOMER_COUPON);
                coupon.setCustomerId(cus.getId());
                coupon.setPushDay(cfg.getPushDay());
                //如果是店铺专有的优惠券设置 设置该优惠券的shopId表示只有这个店铺可以用
                if(cfg.getShopDetailId()!=null&&shopId.equals(cfg.getShopDetailId())){
                    coupon.setShopDetailId(cfg.getShopDetailId());
                }
                //如果是品牌的专有优惠券
                if(cfg.getIsBrand()==1&&cfg.getBrandId()!=null){
                    coupon.setBrandId(cfg.getBrandId());
                }
                //优惠券时间选择的类型分配时间
                if(cfg.getTimeConsType()==TimeCons.MODELA){
                    coupon.setBeginDate(beginDate);
                    coupon.setEndDate(DateUtil.getAfterDayDate(beginDate,cfg.getCouponValiday()));
                }else if(cfg.getTimeConsType()==TimeCons.MODELB){
                    coupon.setBeginDate(cfg.getBeginDateTime());
                    coupon.setEndDate(cfg.getEndDateTime());
                }
                for(int i=0;i<cfg.getCouponNumber();i++){
                    couponService.insertCoupon(coupon);
                }
                long begin=coupon.getBeginDate().getTime()+cfg.getBeginDateTime().getTime();
                long end=coupon.getEndDate().getTime()+cfg.getEndDateTime().getTime();
                timedPush(begin,end,coupon.getCustomerId(),coupon.getName(),coupon.getValue(),coupon.getPushDay());
            }
        }
    }

	  //得到优惠券的时间，然后做定时任务
	    public void timedPush(long BeginDate,long EndDate,String customerId,String name,BigDecimal price,Integer pushDay){
	    	Customer customer=customerService.selectById(customerId);
	        WechatConfig config = wechatConfigService.selectByBrandId(customer.getBrandId());
	        BrandSetting setting = brandSettingService.selectByBrandId(customer.getBrandId());
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	    	if(BeginDate !=0 && EndDate !=0){
	    		if((EndDate-BeginDate)<=(1000*60*60*24*pushDay)){
	    			StringBuffer str=new StringBuffer();
	                String jumpurl = setting.getWechatWelcomeUrl()+"?subpage=tangshi";
	            	str.append("到期提醒通知"+"\n");
	            	str.append(sdf.format(new Date())+"\n");
	            	str.append("<a href='"+jumpurl+"'>您的价值的"+price+"元的"+name+""+pushDay+"天后即将到期，快来享受优惠吧</a>");
	                String result = WeChatUtils.sendCustomerMsg(str.toString(), customer.getWechatId(), config.getAppid(), config.getAppsecret());//提交推送
	                String pr=price+"";//将BigDecimal类型转换成String
	                sendNote(pr,name,pushDay,customerId);//发送短信
	    		}else{
	    			Calendar calendar = Calendar.getInstance();
	    			calendar.setTime(new Date(new Date().getTime()+(1000*60*2)));
	    			calendar.set(Calendar.HOUR, new Date().getHours());
	    			calendar.set(Calendar.SECOND,new Date().getSeconds());
	    			String pr=price+"";
	    			MQMessageProducer.autoSendRemmend(customer.getBrandId(), calendar, customer.getId(),pr,name,pushDay);
	    		}
	    	}
	    }
     
	  //发送短信
	    private void sendNote(String price,String name,Integer pushDay,String customerId){
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	        Customer customer=customerService.selectById(customerId);
	        System.out.println("电话"+customer.getTelephone());
	        System.out.println("orderMsg"+orderMsg);
	        String day=pushDay+"";//将得到的int转换成String
	    	Map param = new HashMap();
			param.put("price", price);
			param.put("name", name);
			param.put("day", day);
			System.out.println(SMSUtils.sendMessage(customer.getTelephone(), new JSONObject(param).toString(), "餐加", "SMS_40860094"));
	    }
	    
	    
	    

    @Override
	public List<NewCustomCoupon> selectListByCouponType(String brandId, Integer couponType,String shopId) {
        List<NewCustomCoupon> list = new ArrayList<>();
        //查询品牌设置的优惠券
        List<NewCustomCoupon> brandList = newcustomcouponMapper.selectListByCouponTypeAndBrandId(brandId,couponType);
        //查询店铺设置的优惠券
        List<NewCustomCoupon> shopList = newcustomcouponMapper.selectListByCouponTypeAndShopId(shopId,couponType);
		list.addAll(brandList);
        list.addAll(shopList);
		//如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
		if(list==null || list.size()==0){
			list = newcustomcouponMapper.selectListByCouponType(brandId, -1);
		}
		return list;
	}

    @Override
    public List<NewCustomCoupon> selectListByCouponTypeAndShopId(String shopId, Integer couponType) {
        List<NewCustomCoupon> list = newcustomcouponMapper.selectListByCouponTypeAndShopId(shopId, couponType);
        //如果没有找到 对应类型的优惠券，则显示通用的优惠券。用于兼容老版本红包没有设置 优惠券类型问题
        if(list==null || list.size()==0){
            list = newcustomcouponMapper.selectListByCouponTypeAndShopId(shopId, -1);
        }
        return list;
    }

    @Override
    public List<NewCustomCoupon> selectListShopId(String shopId) {
        return newcustomcouponMapper.selectListByShopId(shopId);
    }


}
