package com.cshp.search.service;

import com.cshp.search.dto.ProductDTO;

import java.util.List;

public interface SearchService {
    List<ProductDTO> search(String keyword);
    
    List<ProductDTO> getAllProducts();
    
    List<ProductDTO> searchByCategory(String category);
}

