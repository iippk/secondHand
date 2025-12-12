package com.cshp.cart.service;

import com.cshp.cart.dto.CartAddDTO;
import com.cshp.cart.dto.CartDTO;

import java.util.List;

public interface CartService {
    CartDTO addToCart(CartAddDTO dto, String userId);
    List<CartDTO> getCartByUserId(String userId);
    void removeFromCart(Long cartId, String userId);
    void clearCart(String userId);
    void updateQuantity(Long cartId, Integer quantity, String userId);
}

