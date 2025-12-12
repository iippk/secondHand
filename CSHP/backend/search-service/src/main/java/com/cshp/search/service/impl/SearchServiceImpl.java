package com.cshp.search.service.impl;

import com.cshp.common.result.Result;
import com.cshp.search.dto.ProductDTO;
import com.cshp.search.feign.ProductFeignClient;
import com.cshp.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductFeignClient productFeignClient;

    @Override
    public List<ProductDTO> search(String keyword) {
        Result<List<ProductDTO>> result = productFeignClient.searchProducts(keyword);
        return unwrap(result);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        Result<List<ProductDTO>> result = productFeignClient.getAllProducts();
        return unwrap(result);
    }

    @Override
    public List<ProductDTO> searchByCategory(String category) {
        Result<List<ProductDTO>> result = productFeignClient.getProductsByCategory(category);
        return unwrap(result);
    }

    private List<ProductDTO> unwrap(Result<List<ProductDTO>> result) {
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            return Collections.emptyList();
        }
        return result.getData();
    }
}

