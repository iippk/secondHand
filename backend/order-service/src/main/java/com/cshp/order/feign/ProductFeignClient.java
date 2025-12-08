package com.cshp.order.feign;

import com.cshp.common.result.Result;
import com.cshp.order.feign.dto.ProductFeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/product/internal/{id}")
    Result<ProductFeignDTO> getProduct(@PathVariable("id") Long id);

    @PutMapping("/product/internal/{id}/status")
    Result<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    @PutMapping("/product/internal/{id}/shipped")
    Result<Void> updateShipped(@PathVariable("id") Long id, @RequestParam("shipped") Integer shipped);
}


