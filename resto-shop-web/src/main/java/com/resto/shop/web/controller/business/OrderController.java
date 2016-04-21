 package com.resto.shop.web.controller.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.service.OrderService;

@Controller
@RequestMapping("order")
public class OrderController extends GenericController{

	@Resource
	OrderService orderService;
	
	@RequestMapping("list_push")
	@ResponseBody
	public Result list_push(){
		List<Order> order = orderService.selectPushOrder(getCurrentShopId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("list_ready")
	@ResponseBody
	public Result list_ready(){
		List<Order> order= orderService.selectReadyOrder(getCurrentShopId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("list_call")
	@ResponseBody
	public Result list_call(){
		List<Order> order = orderService.selectCallOrder(getCurrentBrandId());
		return getSuccessResult(order);
	}
	
	@RequestMapping("callNumber")
	@ResponseBody
	public Result callOrder(String orderId){
		orderService.callNumber(orderId);
		return getSuccessResult();
	}
	

	
	@RequestMapping("/printReceipt")
	public Map<String,Object> printReceipt(String orderId){
		Map<String,Object> pd = new HashMap<>();
		Map<String,Object> printTask = new HashMap<>();
		printTask = orderService.printReceipt(orderId);
		
		return printTask;
	}
	
	@RequestMapping("printkitchen")
	public Result printkitchen(String orderId){
		System.out.println("-----"+orderId);
		return getSuccessResult(orderService.testKitchen(orderId));
	}
}
