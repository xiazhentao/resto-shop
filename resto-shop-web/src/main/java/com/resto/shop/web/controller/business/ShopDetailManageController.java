 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.core.util.MemcachedUtils;
 import com.resto.brand.web.model.Brand;
 import com.resto.brand.web.model.ShopDetail;
 import com.resto.brand.web.service.BrandService;
 import com.resto.brand.web.service.ShopDetailService;
 import com.resto.shop.web.controller.GenericController;
 import com.resto.shop.web.util.LogTemplateUtils;
 import com.resto.shop.web.util.RedisUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import java.util.List;

 @Controller
 @RequestMapping("shopDetailManage")
 public class ShopDetailManageController extends GenericController{

     @Resource
     ShopDetailService shopDetailService;

     @Resource
     BrandService brandService;

     @RequestMapping("/list")
         public void list(){

     }


     @RequestMapping("list_one")
     @ResponseBody
     public Result list_one(){
         ShopDetail  shopDetail = shopDetailService.selectById(getCurrentShopId());
         return getSuccessResult(shopDetail);
     }

     @RequestMapping("modify")
     @ResponseBody
     public Result modify(ShopDetail shopDetail){
         shopDetail.setId(getCurrentShopId());
         switch (shopDetail.getWaitUnit()){
             case 1 :
                 shopDetail.setTimeOut(shopDetail.getWaitTime());
                 break;
             case 2 :
                 shopDetail.setTimeOut(shopDetail.getWaitTime()  * 24);
                 break;
             case 3 :
                 shopDetail.setTimeOut(Integer.MAX_VALUE);
                 break;
         }
         shopDetailService.update(shopDetail);
         if(RedisUtil.get(getCurrentShopId()+"info") != null){
             RedisUtil.remove(getCurrentShopId()+"info");
         }
         Brand brand = brandService.selectByPrimaryKey(getCurrentBrandId());
         shopDetail = shopDetailService.selectByPrimaryKey(getCurrentShopId());
         LogTemplateUtils.shopDeatilEdit(brand.getBrandName(), shopDetail.getName(), getCurrentBrandUser().getUsername());
         return Result.getSuccess();
     }

     @RequestMapping("list_all")
     @ResponseBody
     public Result listAll(){
         List<ShopDetail> lists = shopDetailService.selectByBrandId(getCurrentBrandId());
         return getSuccessResult(lists);
     }

 }
