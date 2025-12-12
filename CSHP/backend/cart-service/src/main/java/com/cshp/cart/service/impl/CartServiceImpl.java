package com.cshp.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.cart.dto.CartAddDTO;
import com.cshp.cart.dto.CartDTO;
import com.cshp.cart.entity.Cart;
import com.cshp.cart.feign.ProductFeignClient;
import com.cshp.cart.feign.dto.ProductFeignDTO;
import com.cshp.cart.mapper.CartMapper;
import com.cshp.cart.service.CartService;
import com.cshp.common.exception.BusinessException;
import com.cshp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductFeignClient productFeignClient;

    @Override
    @Transactional
    public CartDTO addToCart(CartAddDTO dto, String userId) {
        int quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }

        ProductFeignDTO product = loadProduct(dto.getProductId());
        if (product.getStatus() != null && product.getStatus() != 0) {
            throw new BusinessException("商品不可购买");
        }

        // 检查是否已存在
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", dto.getProductId());
        Cart existingCart = cartMapper.selectOne(wrapper);
        
        if (existingCart != null) {
            // 更新数量
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            existingCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            return convertToDTO(existingCart);
        }
        
        // 创建新购物车项
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(dto.getProductId());
        cart.setQuantity(quantity);
        cart.setProductTitle(product.getTitle());
        cart.setProductImage(getFirstImage(product.getImages()));
        cart.setPrice(product.getPrice());
        // 不再设置 sellerName，因为数据库中不存在该字段
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());
        
        cartMapper.insert(cart);
        return convertToDTO(cart);
    }

    @Override
    public List<CartDTO> getCartByUserId(String userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("update_time");
        List<Cart> carts = cartMapper.selectList(wrapper);
        return carts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId, String userId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在或无权限");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        cartMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void updateQuantity(Long cartId, Integer quantity, String userId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在或无权限");
        }
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        BeanUtils.copyProperties(cart, dto);
        return dto;
    }

    private ProductFeignDTO loadProduct(Long productId) {
        Result<ProductFeignDTO> result = productFeignClient.getProduct(productId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException("商品不存在");
        }
        return result.getData();
    }

    private String getFirstImage(String images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        if (images.contains(",")) {
            return images.split(",")[0];
        }
        return images;
    }
}

