 package com.resto.shop.web.controller.business;

 import com.resto.brand.core.entity.Result;
 import com.resto.brand.web.model.BrandAccountLog;
 import com.resto.brand.web.service.BrandAccountLogService;
 import com.resto.shop.web.controller.GenericController;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;

 import javax.annotation.Resource;
 import javax.validation.Valid;
 import java.util.List;

 @Controller
 @RequestMapping("brandaccountlog")
 public class BrandAccountLogController extends GenericController {

	 @Resource
	 BrandAccountLogService brandaccountlogService;

	 @RequestMapping("/list")
	 public void list(){
	 }

	 @RequestMapping("/list_all")
	 @ResponseBody
	 public Result listData(@RequestParam("beginDate")String beginDate,@RequestParam("endDate")String endDate){
	 	 List<BrandAccountLog> list = brandaccountlogService.selectListByBrandIdAndTime(beginDate,endDate,getCurrentBrandId());

	 	 return getSuccessResult(list);

	 }

	 @RequestMapping("list_one")
	 @ResponseBody
	 public Result list_one(Long id){
		 BrandAccountLog brandaccountlog = brandaccountlogService.selectById(id);
		 return getSuccessResult(brandaccountlog);
	 }

	 @RequestMapping("create")
	 @ResponseBody
	 public Result create(@Valid BrandAccountLog brand){
		 brandaccountlogService.insert(brand);
		 return Result.getSuccess();
	 }

	 @RequestMapping("modify")
	 @ResponseBody
	 public Result modify(@Valid BrandAccountLog brand){
		 brandaccountlogService.update(brand);
		 return Result.getSuccess();
	 }

	 @RequestMapping("delete")
	 @ResponseBody
	 public Result delete(Long id){
		 brandaccountlogService.delete(id);
		 return Result.getSuccess();
	 }
 }
