package com.cshp.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cshp.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    List<Product> selectBySellerId(@Param("sellerId") String sellerId);
    
    List<Product> selectByKeyword(@Param("keyword") String keyword);
    
    List<Product> selectByCategory(@Param("category") String category);
}

