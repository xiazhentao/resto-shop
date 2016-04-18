package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Customer;

public interface CustomerService extends GenericService<Customer, String> {

	Customer login(String openid);

	Customer register(Customer customer);

	void bindPhone(String phone, String currentCustomerId);
    
}
