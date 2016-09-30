package com.resto.shop.web.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.resto.brand.core.entity.JSONResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.*;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderService extends GenericService<Order, String> {
    
	/**
     * 根据当前 店铺ID 和 用户ID 分页查询 其订单列表
     * @param start
     * @param datalength
     * @param shopId
     * @param customerId
     * @return
     */
	public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId,String ORDER_STATE);
	
	/**
	 * 根据订单ID查询订单状态和生产状态
	 * @param orderId
	 * @return
	 */
	public Order selectOrderStatesById(String orderId);

	public JSONResult createOrder(Order order)throws AppException;

	public Order findCustomerNewOrder(String customerId,String shopId,String orderId);

	public boolean cancelOrder(String string);

	public boolean autoRefundOrder(String string);

	Boolean checkRefundLimit(Order order);

	public Order orderWxPaySuccess(OrderPaymentItem item);

	public Order pushOrder(String orderId) throws AppException;
	
	public Order callNumber(String orderId);
	
	public List<Map<String,Object>> getPrintData(String order);
	
	public Order printSuccess(String orderId) throws AppException;

	Order getOrderAccount(String shopId);

	/**
	 * 查询当天某些状态的订单
	 * @param shopId
	 * @return
	 */
	public List<Order> selectTodayOrder(String shopId, int[] is);

	public List<Order> selectReadyOrder(String currentShopId);

	public List<Order> selectPushOrder(String currentShopId,Long lastTime);

	public List<Order> selectCallOrder(String currentBrandId,Long lastTime);

	public Map<String, Object> printReceipt(String orderId,Integer selectPrinterId);

	/**
	 * 打印厨房的小票
	 * @param order			订单信息
	 * @param articleList	订单菜品集合
	 * @return
	 */
	public List<Map<String,Object>> printKitchen(Order order, List<OrderItem> articleList);

	 
	public Order confirmOrder(Order order);

	public Order getOrderInfo(String orderId);

	public List<Order> selectHistoryOrderList(String currentShopId, Date date,Integer shopMode);

	public List<Order> selectErrorOrderList(String currentShopId, Date date);



	public List<Order> getOrderNoPayList(String currentShopId, Date date);


	public Order cancelOrderPos(String orderId) throws AppException;

	public void changePushOrder(Order order);

	public List<Map<String, Object>> printOrderAll(String orderId);

	public void setTableNumber(String orderId, String tableNumber);
	
	/**
	 * 根据取餐码查询店铺中已支付的订单
	 * @param vercode
	 * @return
	 */
	public List<Order> selectOrderByVercode(String vercode,String shopId);
	
	/**
	 * 根据桌号查询店铺中已支付的订单
	 * @param tableNumber
	 * @return
	 */
	public List<Order> selectOrderByTableNumber(String tableNumber,String shopId);
	
	/**
	 * 修改就餐模式
	 * @param modeId
	 * @param orderId
	 */
	public void updateDistributionMode(Integer modeId, String orderId);

	/**
	 * 清除所有订单状态信息
	 * @param currentShopId
	 */
	public void clearNumber(String currentShopId);

	public List<Order> listOrderByStatus(String currentShopId, Date begin, Date end, int[] productionStatus,
			int[] orderState);

	public void updateAllowContinue(String id, boolean b);

	List<Order> selectByParentId(String parentOrderId);

	public Order findCustomerNewPackage(String currentCustomerId, String currentShopId);
	
	/**
	 * 根据时间 和指定 店铺ID 查询已完成的订单(orderSatus = 2,10,11,12)
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	//SaleReportDto selectArticleSumCountByData(String beginDate,String endDate,String brandId);
	
	/**
	 * 根据时间 和 指定 店铺 查询 已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDate(String beginDate,String endDate,String shopId,String sort);
	
	/**
	 * 根据时间 查询 当前品牌已完成的订单的 菜品销售详情(品牌端显示)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public List<ArticleSellDto> selectBrandArticleSellByDate(String beginDate,String endDate,String order);

	public List<ArticleSellDto> selectBrandArticleSellByDateAndArticleFamilyId(String beginDate, String endDate,
			String articleFamilyId,String sort);
	/**
	 * 根据时间,指定店铺，指定菜品分类，查询已完成订单的销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @param articleFamilyId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDateAndArticleFamilyId(String beginDate, String endDate,
			String shopId, String articleFamilyId,String sort);


	/**
	 * 根据时间 和 指定 店铺 查询 已完成的订单的 菜品销售详情(店铺端显示)
	 * @param currentShopId
	 * @param beginDate
	 * @param endDate
	 * @param sort
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleByDate(String currentShopId, String beginDate, String endDate, String sort);


	/**
	 * 根据时间 和 指定 店铺  指定分类的  查询 已完成的订单的 菜品销售详情(店铺端显示)
	 * @param beginDate
	 * @param endDate
	 * @param articleFamilyId
	 * @param sort
	 * @return
	 */

	public List<ArticleSellDto> selectShopArticleByDateAndArcticleFamilyId(String beginDate, String endDate,String shopId,
			String articleFamilyId, String sort);


	/**
	 * 根据时间查询已消费订单的订单数目和订单总额
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public OrderPayDto selectBytimeAndState(String beginDate, String endDate,String brandId);


	/**
	 * 比较订单的店铺id 和 二维码的店铺id
	 * @param orderId 订单号
	 * @param shopId 二维码的店铺id
	 * @return 相等返回true 返回返回false
     */
	Boolean checkShop(String orderId,String shopId);
	
	/**
	 * 获取品牌菜品的销售数量
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public brandArticleReportDto selectBrandArticleNum(String beginDate, String endDate,String brandId);
	
	/**
	 * 获取店铺菜品的销售数据
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public List<ShopArticleReportDto> selectShopArticleDetails(String beginDate, String endDate,String brandId);
	
	/**
	 * 根据时间 查询 当前品牌已完成的订单的 菜品分类销售详情(品牌端显示)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */

	public List<ArticleSellDto> selectBrandArticleSellByDateAndFamilyId(String currentBrandId, String beginDate,
			String endDate, String sort);
	
	/**
	 * 根据时间 查询 当前品牌已完成的订单的 菜品销售详情(品牌端显示)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */

	public List<ArticleSellDto> selectBrandArticleSellByDateAndId(String brandId ,String beginDate, String endDate, String sort);

	

	/**
	 * 查询订单数目和订单金额
	 * @param beginDate
	 * @param endDate
	 * @param currentBrandId
	 * @return
	 */

	public Map<String,Object> selectMoneyAndNumByDate(String beginDate, String endDate, String currentBrandId);


	/**
	 * 根据时间 查询 当前选择店铺已完成的订单的 菜品分类销售详情(品牌端显示)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */


	public List<ArticleSellDto> selectShopArticleSellByDateAndFamilyId(String beginDate, String endDate, String shopId,
			String sort);


	/**
	 * 根据时间 查询 选中店铺已完成的订单的 菜品销售详情(品牌端显示)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */

	public List<ArticleSellDto> selectShopArticleSellByDateAndId(String beginDate, String endDate, String shopId,
			String sort);

	public List<Order> selectListByTime(String beginDate, String endDate, String shopId);

	//查询订单的详细信息(客户和菜品以及菜品信息分类)
	public Order selectOrderDetails(String orderId);

	Boolean setOrderPrintFail(String orderId);

	public List<ArticleSellDto> selectArticleFamilyByBrandAndFamilyName(String currentBrandId, String beginDate,
			String endDate, String selectValue);
	
	//查询品牌所有已消费的订单
	public List<Order> selectListBybrandId(String beginDate, String endDate, String currentBrandId);

	//查询店铺所有的已消费的订单
	public  List<Order> selectListByShopId(String beginDate,String endDate,String shopId);


	//查询订单关联评论的内容
	public List<Order> selectAppraiseByShopId(String beginDate, String endDate, String shopId);

	void autoRefundMoney();

	/**
	 * 检查此订单的菜品是否有库存
	 * @param orderId 订单号
	 * @return
     */
	Result checkArticleCount(String orderId);


	/**
	 * 出单时更新库存
	 * @param order
	 * @return
     */
	Boolean updateStock(Order order) throws AppException;


	/**
	 * 出单时还原库存
	 * @param order
	 * @return
	 */
	Boolean addStock(Order order) throws AppException;

	List<Map<String, Object>> printTotal(String shopId);


    /**
     * 打印厨房小票
     * @param oid
     * @return
     */
    List<Map<String, Object>>  printKitchenReceipt(String oid);


    List<OrderArticleDto> selectOrderArticle(String currentBrandId,String beginDate,String endDate);

    /**
     * 查询品牌菜品销售  用于中间数据库
     * @param currentBrandId
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Map<String,Object>> selectBrandArticleSellList(String currentBrandId, String beginDate, String endDate);

    /**
     * 查询店铺菜品销售 用于中间数据库
     * @param currentBrandId
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Map<String,Object>> selectShopArticleSellList(String currentBrandId, String beginDate, String endDate);

    /**
     * 查询订单详情 用于中间数据库
     * @param beginDate
     * @param endDate
     * @param currentBrandId
     * @return
     */
    List<Order> selectListByTimeAndBrandId(String currentBrandId,String beginDate, String endDate);

	/**
	 * 根据订单状态和生产状态查询指定店铺的订单
	 * @param shopId
	 * @param orderStates
	 * @param productionStates
	 * @return
	 */
	List<Order> selectByOrderSatesAndProductionStates(String shopId,String[] orderStates,String[] productionStates);

	Order payOrderModeFive(String orderId);

	Order payPrice(BigDecimal factMoney,String orderId);

	void useRedPrice(BigDecimal factMoney,String orderId);
}
