package com.cshp.aiservice2.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;         // 数据库字段：order_no
    private Long productId;         // 数据库字段：product_id
    private String productTitle;    // 数据库字段：product_title
    private String productImage;    // 数据库字段：product_image
    private BigDecimal price;       // 数据库字段：price
    private String buyerId;         // 数据库字段：buyer_id
    private String buyerName;       // 数据库字段：buyer_name
    private String sellerId;        // 数据库字段：seller_id
    private String sellerName;      // 数据库字段：seller_name
    private Integer status;         // 数据库字段：status
    private String address;         // 数据库字段：address
    private String phone;           // 数据库字段：phone
    private String remark;          // 数据库字段：remark
    private LocalDateTime createTime; // 数据库字段：create_time
    private LocalDateTime updateTime; // 数据库字段：update_time
    private LocalDateTime payTime;    // 数据库字段：pay_time
    private LocalDateTime shipTime;   // 数据库字段：ship_time
    private LocalDateTime completeTime; // 数据库字段：complete_time
}