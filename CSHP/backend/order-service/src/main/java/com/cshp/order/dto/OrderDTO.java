package com.cshp.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private String buyerId;
    private String buyerName;
    private String sellerId;
    private String sellerName;
    private Integer status;
    private String address;
    private String phone;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
}

