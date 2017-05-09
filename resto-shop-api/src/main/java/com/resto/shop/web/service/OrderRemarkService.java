package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OrderRemark;

import java.util.List;

public interface OrderRemarkService extends GenericService<OrderRemark, String>{

    List<OrderRemark> selectOrderRemarkByShopId(String shopId);

    void deleteByBoOrderRemarkId(String boOrderRemarkId);
}
