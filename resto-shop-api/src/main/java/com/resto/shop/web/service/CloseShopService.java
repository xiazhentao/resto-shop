package com.resto.shop.web.service;

import com.resto.brand.web.model.Brand;
import com.resto.brand.web.model.ShopDetail;
import com.resto.shop.web.model.OffLineOrder;

/**
 yz 2017/08/15把结店短信的 方法抽出来
 */
public interface CloseShopService {

	public  void cleanShopOrder(ShopDetail shopDetail, OffLineOrder offLineOrder, Brand brand);
}
