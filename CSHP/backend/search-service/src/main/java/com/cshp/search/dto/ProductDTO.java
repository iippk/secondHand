package com.cshp.search.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
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
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

