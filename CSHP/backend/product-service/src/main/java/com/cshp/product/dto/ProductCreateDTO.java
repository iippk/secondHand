package com.cshp.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "商品标题不能为空")
    private String title;
    
    @NotBlank(message = "商品描述不能为空")
    private String description;
    
    @NotNull(message = "商品价格不能为空")
    @Positive(message = "商品价格必须大于0")
    private BigDecimal price;
    
    @NotBlank(message = "商品分类不能为空")
    private String category;
    
    private String images;
}

