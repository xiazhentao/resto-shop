package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.TableQrcode;

import java.util.List;

/**
 * Created by carl on 2016/12/16.
 */
public interface TableQrcodeMapper  extends GenericDao<TableQrcode, Long>{

    List<TableQrcode> selectByShopId(String shopId);

}