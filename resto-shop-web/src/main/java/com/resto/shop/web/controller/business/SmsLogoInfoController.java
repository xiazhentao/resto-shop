 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.model.SmsAcount;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.brand.web.service.SmsAcountService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.SmsLog;
import com.resto.shop.web.service.OrderItemService;
import com.resto.shop.web.service.OrderPaymentItemService;
import com.resto.shop.web.service.SmsLogService;

@Controller
@RequestMapping("smsloginfo")
public class SmsLogoInfoController extends GenericController{
	
	@Resource
	OrderPaymentItemService orderPaymentItemService;
	
	@Resource
	OrderItemService orderItemService;
	
	@Resource
	ShopDetailService shopDetailService;
	
	@Resource
	SmsLogService smsLogService;
	
	@Resource
	SmsAcountService smsAcountService;
	
	
//	@RequestMapping("/brand/list")
//    public ModelAndView list(){
//		ModelAndView mv = new ModelAndView();
//		List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
//		SmsAcount smsAcount = smsAcountService.selectByBrandId(getCurrentBrandId());
//		mv.setViewName("smsloginfo/brand/list");;
//		mv.addObject("shopDetails", shopDetails);
//		mv.addObject("smsAcount", smsAcount);
//		return mv;
//    }
	
	@RequestMapping("brand/list")
    public void listBrand(){
    }
	
	@RequestMapping("shop/list")
	public void listShop(){
	}
	
	
	/**
	 * 查询店铺的名字
	 */
	@ResponseBody
	@RequestMapping("/shopName")
	public List<ShopDetail> queryList(){
		return shopDetailService.selectByBrandId(getCurrentBrandId());	
	}
	
	@ResponseBody
	@RequestMapping("/list_all")
	public List<SmsLog> list_all(){
		return smsLogService.selecByBrandId(getCurrentBrandId());
	}
	
	/**
	 * 根据时间和页面传过来的店铺id查询短信
	 * @param begin
	 * @param end
	 * @param shopIds
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listByShopsAndDate")
	public List<SmsLog> listByWhere(@RequestParam("begin")String begin,@RequestParam("end")String end,@RequestParam("shopIds")String shopIds){
		return smsLogService.selectListWhere(begin,end,shopIds) ;
	}
	
	/**
	 * 根据时间和当前店铺id查询短信
	 * @param begin
	 * @param end
	 * @param shopIds
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/listByShopAndDate")
	public List<SmsLog> listByWhere(@RequestParam("begin")String begin,@RequestParam("end")String end){
		return smsLogService.selectListWhere(begin,end,getCurrentShopId()) ;
	}
	
	@ResponseBody
	@RequestMapping("/listByShopId")
	public List<SmsLog> listByShop(@RequestParam("shopId")String shopId){
		return smsLogService.selectListByShopId(shopId) ;
	}
	
	
	@ResponseBody
	@RequestMapping("/querySmsNum")
	public String querySmsNumByBrand(){
		return smsAcountService.selectSmsUnitPriceByBrandId(getCurrentBrandId()).toString();
	}
	
	/**
	 * 测试发短信功能
	 * @return
	 */
	@RequestMapping("/sendCode")
	public String sendCode(@RequestParam("phone")String phone, @RequestParam("code")String code, @RequestParam("brandId")String brandId, @RequestParam("shopId")String shopId){
		
//		return smsLogService.sendCode(phone, code, brandId, shopId);
		return smsLogService.sendCode(phone, code, brandId, shopId);		
	}
	
}
