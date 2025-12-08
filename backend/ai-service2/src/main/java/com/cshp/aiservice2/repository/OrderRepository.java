package com.cshp.aiservice2.repository;

import com.cshp.aiservice2.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderRepository {

    @Select("SELECT * FROM user_order WHERE buyer_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Order> findOrdersByBuyer(@Param("userId") String userId, @Param("limit") int limit);

    @Select("SELECT * FROM user_order WHERE seller_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Order> findOrdersBySeller(@Param("userId") String userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM user_order WHERE buyer_id = #{userId} AND status = 1")
    Long countCompletedPurchases(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM user_order WHERE seller_id = #{userId} AND status = 1")
    Long countCompletedSales(@Param("userId") String userId);

    // 新增：获取用户交易总额
    @Select("SELECT COALESCE(SUM(price), 0) FROM user_order WHERE buyer_id = #{userId} AND status = 1")
    Double getTotalPurchaseAmount(@Param("userId") String userId);

    @Select("SELECT COALESCE(SUM(price), 0) FROM user_order WHERE seller_id = #{userId} AND status = 1")
    Double getTotalSalesAmount(@Param("userId") String userId);

    // 新增：获取最近完成的订单
    @Select("SELECT * FROM user_order WHERE (buyer_id = #{userId} OR seller_id = #{userId}) AND status = 1 ORDER BY complete_time DESC LIMIT #{limit}")
    List<Order> findRecentCompletedOrders(@Param("userId") String userId, @Param("limit") int limit);
}