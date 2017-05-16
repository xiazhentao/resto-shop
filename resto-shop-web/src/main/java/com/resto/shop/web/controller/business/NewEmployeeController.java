 package com.resto.shop.web.controller.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.resto.brand.core.util.ApplicationUtils;
import com.resto.brand.web.model.ShopDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.NewEmployee;
import com.resto.shop.web.service.NewEmployeeService;

@Controller
@RequestMapping("newEmployee")
public class NewEmployeeController extends GenericController{

	@Resource
	NewEmployeeService newEmployeeService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public Result listData(){
	    try{
            Map<String, Object> map = new HashMap<>();
            List<NewEmployee> newEmployees = newEmployeeService.selectList();
            List<ShopDetail> shopDetails = getCurrentShopDetails();
            for (NewEmployee newEmployee : newEmployees){
                for (ShopDetail shopDetail : shopDetails){
                    if (newEmployee.getShopDetailId().equalsIgnoreCase(shopDetail.getId())){
                        newEmployee.setShopName(shopDetail.getName());
                        break;
                    }
                }
                if (newEmployee.getRoleType().equals(1)){
                    newEmployee.setRoleValue("员工");
                }else{
                    newEmployee.setRoleValue("店长");
                }
                if (newEmployee.getSex().equals(1)){
                    newEmployee.setSexValue("男");
                }else{
                    newEmployee.setSexValue("女");
                }
            }
            map.put("newEmployees",newEmployees);
            map.put("shopDetails",shopDetails);
            return getSuccessResult(map);
        }catch (Exception e){
            e.printStackTrace();
            log.error("查看所有员工出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid NewEmployee newemployee){
	    try{
	        newemployee.setId(ApplicationUtils.randomUUID());
            newemployee.setBrandId(getCurrentBrandId());
            newemployee.setCreateTime(new Date());
            newEmployeeService.insert(newemployee);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("添加员工出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid NewEmployee newemployee){
	    try{
            newEmployeeService.update(newemployee);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("编辑员工出错！");
            return new Result(false);
        }
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
	    try{
            newEmployeeService.delete(id);
            return getSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            log.error("删除员工出错！");
            return new Result(false);
        }
	}
}
