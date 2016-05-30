package com.resto.shop.web.controller.business;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.SmsTicket;
import com.resto.brand.web.service.SmsTicketService;
import com.resto.shop.web.controller.GenericController;

@Controller
@RequestMapping("invoice")
public class InvoiceController extends GenericController{

	private SmsTicketService smsTicketService;
	
	@RequestMapping("list")
	public void list(){
	}
	
	@RequestMapping("list_all")
	@ResponseBody
	public Result list_all(){
		List<SmsTicket> list = smsTicketService.selectList();
		return getSuccessResult(list);
	}
}
