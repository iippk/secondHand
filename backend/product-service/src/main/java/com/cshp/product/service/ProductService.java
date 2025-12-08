package com.cshp.product.service;

import com.cshp.product.dto.ProductCreateDTO;
import com.cshp.product.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductCreateDTO dto, String sellerId);
    
    ProductDTO getProductById(Long id);
    
    List<ProductDTO> getAllProducts();
    
    List<ProductDTO> getProductsBySellerId(String sellerId);
    
    ProductDTO updateProduct(Long id, ProductCreateDTO dto, String sellerId);
    
    void deleteProduct(Long id, String sellerId);
    
    void updateProductStatus(Long id, Integer status, String sellerId);
    
    void updateProductShipped(Long id, Integer shipped, String sellerId);
    void updateProductStatusInternal(Long id, Integer status);
    void updateProductShippedInternal(Long id, Integer shipped);
    
    List<ProductDTO> searchProducts(String keyword);
    
    List<ProductDTO> getProductsByCategory(String category);
    
    void incrementViewCount(Long id);

    /**
     * 内部服务调用，不增加浏览量
     */
    ProductDTO getProductDetail(Long id);
}

