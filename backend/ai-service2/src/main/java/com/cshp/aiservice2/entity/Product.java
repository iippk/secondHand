package com.cshp.aiservice2.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String title;           // 数据库字段：title
    private String description;     // 数据库字段：description
    private BigDecimal price;       // 数据库字段：price
    private String category;        // 数据库字段：category
    private String images;          // 数据库字段：images
    private String sellerId;        // 数据库字段：seller_id
    private String sellerName;      // 数据库字段：seller_name
    private Integer status;         // 数据库字段：status
    private Integer shipped;        // 数据库字段：shipped
    private Integer viewCount;      // 数据库字段：view_count
    private String condition;       // 数据库字段：condition（注意：这是MySQL关键字，用反引号）
    private LocalDateTime createTime; // 数据库字段：create_time
    private LocalDateTime updateTime; // 数据库字段：update_time
}