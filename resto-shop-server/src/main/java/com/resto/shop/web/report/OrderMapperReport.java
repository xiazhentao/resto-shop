package com.resto.shop.web.report;


import com.resto.brand.web.dto.BrandOrderReportDto;
import com.resto.brand.web.dto.ShopIncomeDto;
import com.resto.brand.web.dto.ShopOrderReportDto;
import com.resto.shop.web.dto.OrderNumDto;
import com.resto.shop.web.model.Order;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderMapperReport{

	/**
	 * 每个店铺的交易笔数
	 * @param brandId
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	List<OrderNumDto> selectOrderNumByTimeAndBrandId(@Param("brandId") String brandId, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

	BrandOrderReportDto procDayAllOrderItemBrand(Map<String, Object> selectMap);

	List<Order> selectListByTime(@Param("beginDate")Date begin, @Param("endDate")Date end, @Param("shopId") String shopId, @Param("customerId") String customerId);

	List<ShopIncomeDto> callProcDayAllOrderItem(Map<String, Object> selectMap);

	List<ShopIncomeDto> callProcDayAllOrderPayMent(Map<String, Object> selectMap);

	ShopOrderReportDto procDayAllOrderItemShop(Date beginDate, Date endDate, String shopId);

	/**
	 * 查询店铺下所有的已消费的订单
	 * @param begin
	 * @param end
	 * @param shopId
	 * @return
	 */

	List<Order> selectListByShopId(@Param("beginDate") Date begin, @Param("endDate") Date end,@Param("shopId") String shopId);

}
