package com.cshp.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private Long productId;
    private String productTitle;
    private String productImage;
    private java.math.BigDecimal price;
    // sellerName 字段已移除，因为数据库中不存在该字段
    // 如需获取卖家名称，可通过 productId 关联查询 product 表，再通过 sellerId 查询 user 表
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

