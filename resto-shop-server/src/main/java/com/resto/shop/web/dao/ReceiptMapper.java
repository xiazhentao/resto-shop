package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.dto.ReceiptOrder;
import com.resto.shop.web.dto.ReceiptPos;
import com.resto.shop.web.dto.ReceiptPosOrder;
import com.resto.shop.web.model.Receipt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReceiptMapper extends GenericDao<Receipt,String> {
    int deleteByPrimaryKey(Long id);

    int insert(Receipt record);

    int insertSelective(Receipt record);

    Receipt selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Receipt record);

    int updateByPrimaryKey(Receipt record);

    int updateState(Receipt record);

    List<ReceiptOrder> selectReceiptOrderList(@Param("customerId")String customerId);

    List<ReceiptOrder> selectApplyReceiptOrderList(@Param("customerId")String customerId);

    ReceiptPosOrder getReceiptOrderList(@Param("receiptId")Integer receiptId);

    List<ReceiptPos> getNowReceiptList(@Param("shopId")String shopId,@Param("state")Integer state);

    List<ReceiptPos> getBeforeReceiptList(@Param("shopId")String shopId);
}