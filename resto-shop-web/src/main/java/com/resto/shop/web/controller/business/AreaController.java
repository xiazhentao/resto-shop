package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.config.SessionKey;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Area;
import com.resto.shop.web.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by KONATA on 2017/4/5.
 */
@Controller
@RequestMapping("area")
public class AreaController extends GenericController {

    @Autowired
    private AreaService areaService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<Area> listData(){
        return areaService.getAreaList(getCurrentShopId());
    }



    @RequestMapping("list_one")
    @ResponseBody
    public Result list_one(Long id){
        Area area = areaService.selectById(id);
        return getSuccessResult(area);
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(@Valid Area area){
        area.setShopDetailId(getCurrentShopId());
        areaService.insert(area);
        return Result.getSuccess();
    }

    @RequestMapping("modify")
    @ResponseBody
    public Result modify(@Valid Area area){
        areaService.update(area);
        return Result.getSuccess();
    }

    @RequestMapping("delete")
    @ResponseBody
    public Result delete(Long id){
        areaService.delete(id);
        return Result.getSuccess();
    }
}
