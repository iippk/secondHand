package com.cshp.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartDTO {
    private Long id;
    private String userId;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private String sellerName;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

