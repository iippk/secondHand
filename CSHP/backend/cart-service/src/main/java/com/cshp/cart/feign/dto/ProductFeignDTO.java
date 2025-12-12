package com.cshp.cart.feign.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFeignDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String images;
    private String sellerId;
    private String sellerName;
    private Integer status;
    private Integer shipped;
}


