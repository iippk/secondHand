package com.cshp.cart.feign;

import com.cshp.cart.feign.dto.ProductFeignDTO;
import com.cshp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/product/internal/{id}")
    Result<ProductFeignDTO> getProduct(@PathVariable("id") Long id);
}


