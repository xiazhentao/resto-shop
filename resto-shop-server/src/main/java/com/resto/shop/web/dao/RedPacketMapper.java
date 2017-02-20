package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.RedPacketDto;
import com.resto.shop.web.model.AccountLog;
import com.resto.shop.web.model.RedPacket;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RedPacketMapper extends GenericDao<RedPacket,String> {

    RedPacket selectFirstRedPacket(@Param("customerId") String customerId,@Param("redType") Integer redType);

    void updateRedRemainderMoney(Map<String, Object> param);

    List<RedPacketDto> selectRedPacketLog(Map<String, Object> selectMap);

    Map<String, Object> selectUseRedOrder(Map<String, Object> selectMap);
}