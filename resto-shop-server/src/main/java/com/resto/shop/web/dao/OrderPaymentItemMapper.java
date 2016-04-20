package com.resto.shop.web.dao;

import com.resto.shop.web.model.OrderPaymentItem;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;

public interface OrderPaymentItemMapper  extends GenericDao<OrderPaymentItem,String> {
    int deleteByPrimaryKey(String id);

    int insert(OrderPaymentItem record);

    int insertSelective(OrderPaymentItem record);

    OrderPaymentItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderPaymentItem record);

    int updateByPrimaryKeyWithBLOBs(OrderPaymentItem record);

    int updateByPrimaryKey(OrderPaymentItem record);

	List<OrderPaymentItem> selectByOrderId(String orderId);
}
