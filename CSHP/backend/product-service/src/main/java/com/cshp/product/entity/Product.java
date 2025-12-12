package com.cshp.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String images;
    private String sellerId;
    // sellerName 字段已移除，因为数据库中不存在该字段
    // 如需获取卖家名称，可通过 sellerId 关联查询 user 表
    private Integer status; // 0:待售 1:已售出 2:已下架
    private Integer shipped; // 0:未寄出 1:已寄出
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

