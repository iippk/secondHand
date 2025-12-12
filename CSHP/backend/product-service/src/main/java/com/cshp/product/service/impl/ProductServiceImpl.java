package com.cshp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.common.exception.BusinessException;
import com.cshp.product.dto.ProductCreateDTO;
import com.cshp.product.dto.ProductDTO;
import com.cshp.product.entity.Product;
import com.cshp.product.mapper.ProductMapper;
import com.cshp.product.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductCreateDTO dto, String sellerId) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setSellerId(sellerId);
        // 不再设置 sellerName，因为数据库中不存在该字段
        product.setStatus(0);
        product.setShipped(0);
        product.setViewCount(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        productMapper.insert(product);
        
        return convertToDTO(product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        ProductDTO dto = getProductDetail(id);
        incrementViewCount(id);
        return dto;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByDesc("create_time");
        List<Product> products = productMapper.selectList(wrapper);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsBySellerId(String sellerId) {
        List<Product> products = productMapper.selectBySellerId(sellerId);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductCreateDTO dto, String sellerId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此商品");
        }
        
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }
        product.setUpdateTime(LocalDateTime.now());
        
        productMapper.updateById(product);
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String sellerId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权删除此商品");
        }
        productMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status, String sellerId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此商品状态");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void updateProductStatusInternal(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void updateProductShipped(Long id, Integer shipped, String sellerId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此商品状态");
        }
        product.setShipped(shipped);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void updateProductShippedInternal(Long id, Integer shipped) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setShipped(shipped);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        List<Product> products = productMapper.selectByKeyword(keyword);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String category) {
        List<Product> products = productMapper.selectByCategory(category);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setViewCount(product.getViewCount() + 1);
            productMapper.updateById(product);
        }
    }

    @Override
    public ProductDTO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return convertToDTO(product);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}

