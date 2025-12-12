package com.cshp.search.controller;

import com.cshp.common.result.Result;
import com.cshp.search.dto.ProductDTO;
import com.cshp.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Result<List<ProductDTO>> search(@RequestParam String keyword) {
        List<ProductDTO> products = searchService.search(keyword);
        return Result.success(products);
    }

    @GetMapping("/all")
    public Result<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = searchService.getAllProducts();
        return Result.success(products);
    }

    @GetMapping("/category/{category}")
    public Result<List<ProductDTO>> searchByCategory(@PathVariable String category) {
        List<ProductDTO> products = searchService.searchByCategory(category);
        return Result.success(products);
    }
}

