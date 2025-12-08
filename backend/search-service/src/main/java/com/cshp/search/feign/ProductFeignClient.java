package com.cshp.search.feign;

import com.cshp.common.result.Result;
import com.cshp.search.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductFeignClient {
    @GetMapping("/product/search")
    Result<List<ProductDTO>> searchProducts(@RequestParam("keyword") String keyword);
    
    @GetMapping("/product/list")
    Result<List<ProductDTO>> getAllProducts();
    
    @GetMapping("/product/category/{category}")
    Result<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category);
}

