package com.resto.shop.web.service;

import java.math.BigDecimal;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Account;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.Order;

public interface AccountService extends GenericService<Account, String> {

	/**
	 * @param maxUseAccount 最高使用余额的金额
	 * @param account
	 * @return
	 */
	BigDecimal useAccount(BigDecimal maxUseAccount, Account account,Integer source);

	void addAccount(BigDecimal value, String accountId, String remark,Integer source);
    
	/**
	 * 根据用户查询 余额 和 交易明细
	 * @param customerId
	 * @return
	 */
	Account selectAccountAndLogByCustomerId(String customerId);

	Account createCustomerAccount(Customer cus);

	BigDecimal payOrder(Order order, BigDecimal payMoney, Customer customer);

	BigDecimal houFuPayOrder(Order order, BigDecimal payMoney, Customer customer);



}
