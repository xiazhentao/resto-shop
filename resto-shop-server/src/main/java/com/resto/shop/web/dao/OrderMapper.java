package com.resto.shop.web.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.Order;

public interface OrderMapper  extends GenericDao<Order,String> {
    int deleteByPrimaryKey(String id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    
    /**
     * 根据当前 店铺ID 和 用户ID 分页查询 其订单列表
     * @param start
     * @param datalength
     * @param shopId
     * @param customerId
     * @return
     */
    List<Order> orderList(@Param("start") Integer start,@Param("datalength") Integer datalength,@Param("shopId") String shopId,@Param("customerId") String customerId,@Param("orderState") String[] orderState);
    
    /**
     * 根据订单查询 订单状态 和 生产状态
     * @param orderId
     * @return
     */
    Order selectOrderStatesById(@Param("orderId") String orderId);
    
    /**
     * 查询  用户的新订单,如果 订单ID有值。则查询订单的详情
     * @param beginDate		当天的开始时间
     * @param customerId	当前用户ID
     * @param shopId		当前店铺ID
     * @param orderId		订单ID
     * @return
     */
    Order findCustomerNewOrder(@Param("beginDate") Date beginDate,@Param("customerId") String customerId,@Param("shopId") String shopId,@Param("orderState") Integer[] orderState,@Param("orderId") String orderId);

    /**
     * 查询某个店铺某天，某个状态的订单
     * @param shopId
     * @param date
     * @param productionState
     * @return
     */
	List<Order> selectShopOrderByDateAndProductionStates(@Param("shopId")String shopId,@Param("date") Date date,@Param("proStatus") int[] proStatus);

	/**
	 * 查询某天的历史订单
	 * @param currentShopId
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	List<Order> selectHistoryOrderList(@Param("shopId")String currentShopId, @Param("dateBegin")Date dateBegin, @Param("dateEnd")Date dateEnd);

	void clearPushOrder(String id, int notOrder);
}
