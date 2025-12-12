package com.cshp.product.controller;

import com.cshp.common.result.Result;
import com.cshp.product.dto.ProductCreateDTO;
import com.cshp.product.dto.ProductDTO;
import com.cshp.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Result<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto,
                                            @RequestHeader("X-Student-Id") String sellerId) {
        // 不再接收 X-Seller-Name，因为数据库中不存在 seller_name 字段
        // 如需获取卖家名称，可通过 sellerId 关联查询 user 表
        ProductDTO product = productService.createProduct(dto, sellerId);
        return Result.success(product);
    }

    @GetMapping("/{id}")
    public Result<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return Result.success(product);
    }

    @GetMapping("/internal/{id}")
    public Result<ProductDTO> getProductInternal(@PathVariable Long id) {
        ProductDTO product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @GetMapping("/list")
    public Result<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        System.out.println( products);
        return Result.success(products);
    }

    @GetMapping("/my-sell")
    public Result<List<ProductDTO>> getMySellProducts(@RequestHeader("X-Student-Id") String sellerId) {
        List<ProductDTO> products = productService.getProductsBySellerId(sellerId);
        return Result.success(products);
    }

    @PutMapping("/{id}")
    public Result<ProductDTO> updateProduct(@PathVariable Long id,
                                            @Valid @RequestBody ProductCreateDTO dto,
                                            @RequestHeader("X-Student-Id") String sellerId) {
        ProductDTO product = productService.updateProduct(id, dto, sellerId);
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id,
                                      @RequestHeader("X-Student-Id") String sellerId) {
        productService.deleteProduct(id, sellerId);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestHeader("X-Student-Id") String sellerId) {
        productService.updateProductStatus(id, status, sellerId);
        return Result.success();
    }

    @PutMapping("/{id}/shipped")
    public Result<Void> updateShipped(@PathVariable Long id,
                                      @RequestParam Integer shipped,
                                      @RequestHeader("X-Student-Id") String sellerId) {
        productService.updateProductShipped(id, shipped, sellerId);
        return Result.success();
    }

    @PutMapping("/internal/{id}/status")
    public Result<Void> updateStatusInternal(@PathVariable Long id,
                                             @RequestParam Integer status) {
        productService.updateProductStatusInternal(id, status);
        return Result.success();
    }

    @PutMapping("/internal/{id}/shipped")
    public Result<Void> updateShippedInternal(@PathVariable Long id,
                                              @RequestParam Integer shipped) {
        productService.updateProductShippedInternal(id, shipped);
        return Result.success();
    }

    @GetMapping("/search")
    public Result<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        return Result.success(products);
    }

    @GetMapping("/category/{category}")
    public Result<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return Result.success(products);
    }
}

