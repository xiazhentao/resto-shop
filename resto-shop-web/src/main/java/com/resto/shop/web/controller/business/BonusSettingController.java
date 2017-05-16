 package com.resto.shop.web.controller.business;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.alibaba.fastjson.JSONArray;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.model.ChargeSetting;
import com.resto.shop.web.service.ChargeSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.BonusSetting;
import com.resto.shop.web.service.BonusSettingService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 @Controller
@RequestMapping("bonusSetting")
public class BonusSettingController extends GenericController{

	@Resource
	BonusSettingService bonussettingService;

    @Resource
    ChargeSettingService chargeSettingService;

	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try{
            List<BonusSetting> bonusSettings = bonussettingService.selectList();
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            List<ChargeSetting> chargeSettings = chargeSettingService.selectList();
            BonusSetting bonusSetting;
            if (bonusSettings.isEmpty()){
                for (ChargeSetting chargeSetting : chargeSettings){
                    bonusSetting = new BonusSetting();
                    bonusSetting.setBrandId(getCurrentBrandId());
                    bonusSetting.setShopDetailId(chargeSetting.getShopDetailId());
                    bonusSetting.setChargeSettingId(chargeSetting.getId());
                    bonusSetting.setChargeBonusRatio(0);
                    bonusSetting.setShopownerBonusRatio(0);
                    bonusSetting.setEmployeeBonusRatio(100);
                    bonusSetting.setState(false);
                    bonusSetting.setChargeName(chargeSetting.getLabelText());
                    bonusSetting.setCreateTime(new Date());
                    for (ShopDetail shopDetail : shopDetails){
                        if (shopDetail.getId().equalsIgnoreCase(chargeSetting.getShopDetailId())){
                            bonusSetting.setShopName(shopDetail.getName());
                            break;
                        }
                    }
                    bonusSettings.add(bonusSetting);
                }
            }else {
                for (BonusSetting setting : bonusSettings) {
                    for (ShopDetail shopDetail : shopDetails) {
                        if (setting.getShopDetailId().equalsIgnoreCase(shopDetail.getId())) {
                            setting.setShopName(shopDetail.getName());
                            break;
                        }
                    }
                    for (ChargeSetting chargeSetting : chargeSettings) {
                        if (setting.getChargeSettingId().equalsIgnoreCase(chargeSetting.getId())) {
                            setting.setChargeName(chargeSetting.getLabelText());
                            chargeSettings.remove(chargeSetting);
                            break;
                        }
                    }
                }
                for (ChargeSetting chargeSetting : chargeSettings){
                    bonusSetting = new BonusSetting();
                    bonusSetting.setBrandId(getCurrentBrandId());
                    bonusSetting.setShopDetailId(chargeSetting.getShopDetailId());
                    bonusSetting.setChargeSettingId(chargeSetting.getId());
                    bonusSetting.setChargeBonusRatio(0);
                    bonusSetting.setShopownerBonusRatio(0);
                    bonusSetting.setEmployeeBonusRatio(100);
                    bonusSetting.setState(false);
                    bonusSetting.setChargeName(chargeSetting.getLabelText());
                    bonusSetting.setCreateTime(new Date());
                    for (ShopDetail shopDetail : shopDetails){
                        if (shopDetail.getId().equalsIgnoreCase(chargeSetting.getShopDetailId())){
                            bonusSetting.setShopName(shopDetail.getName());
                            break;
                        }
                    }
                    bonusSettings.add(bonusSetting);
                }
            }
	        return getSuccessResult(bonusSettings);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有分红设置出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid BonusSetting bonussetting){
	    try{
            bonussetting.setId(ApplicationUtils.randomUUID());
            bonussetting.setCreateTime(new Date());
            bonussettingService.insert(bonussetting);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("新建分红设置出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid BonusSetting bonussetting){
        try{
            bonussettingService.update(bonussetting);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("修改分红设置出错！");
            return new Result(false);
        }
	}
}
