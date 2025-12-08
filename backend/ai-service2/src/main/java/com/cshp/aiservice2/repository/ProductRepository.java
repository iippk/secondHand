package com.cshp.aiservice2.repository;

import com.cshp.aiservice2.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductRepository {

    @Select("SELECT * FROM product WHERE status = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> findRecentProducts(@Param("limit") int limit);

    @Select("SELECT * FROM product WHERE category LIKE CONCAT('%', #{category}, '%') AND status = 0 LIMIT #{limit}")
    List<Product> findProductsByCategory(@Param("category") String category, @Param("limit") int limit);

    @Select("SELECT * FROM product WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND status = 0 LIMIT #{limit}")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    @Select("SELECT * FROM product WHERE seller_id = #{sellerId} AND status = 0 ORDER BY create_time DESC")
    List<Product> findProductsBySeller(@Param("sellerId") String sellerId);

    // 新增：按价格范围搜索
    @Select("SELECT * FROM product WHERE status = 0 AND price BETWEEN #{minPrice} AND #{maxPrice} ORDER BY price ASC LIMIT #{limit}")
    List<Product> findProductsByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, @Param("limit") int limit);

    // 新增：按条件（成色）搜索
    @Select("SELECT * FROM product WHERE status = 0 AND `condition` = #{condition} ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> findProductsByCondition(@Param("condition") String condition, @Param("limit") int limit);

    // 新增：获取价格统计
    @Select("SELECT MIN(price) as min_price, MAX(price) as max_price, AVG(price) as avg_price FROM product WHERE status = 0 AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))")
    PriceStats getPriceStats(@Param("keyword") String keyword);

    // 价格统计内部类
    class PriceStats {
        private Double minPrice;
        private Double maxPrice;
        private Double avgPrice;

        // getter and setter methods
        public Double getMinPrice() { return minPrice; }
        public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
        public Double getMaxPrice() { return maxPrice; }
        public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
        public Double getAvgPrice() { return avgPrice; }
        public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }
    }
}