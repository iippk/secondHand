package com.cshp.cart.controller;

import com.cshp.cart.dto.CartAddDTO;
import com.cshp.cart.dto.CartDTO;
import com.cshp.cart.service.CartService;
import com.cshp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public Result<CartDTO> addToCart(@RequestBody @Valid CartAddDTO dto,
                                     @RequestHeader("X-Student-Id") String userId) {
        return Result.success(cartService.addToCart(dto, userId));
    }

    @GetMapping
    public Result<List<CartDTO>> getCartList(@RequestHeader("X-Student-Id") String userId) {
        return Result.success(cartService.getCartByUserId(userId));
    }

    @DeleteMapping("/{cartId}")
    public Result<Void> removeFromCart(@PathVariable Long cartId,
                                       @RequestHeader("X-Student-Id") String userId) {
        cartService.removeFromCart(cartId, userId);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart(@RequestHeader("X-Student-Id") String userId) {
        cartService.clearCart(userId);
        return Result.success();
    }

    @PutMapping("/{cartId}")
    public Result<Void> updateQuantity(@PathVariable Long cartId,
                                       @RequestParam Integer quantity,
                                       @RequestHeader("X-Student-Id") String userId) {
        cartService.updateQuantity(cartId, quantity, userId);
        return Result.success();
    }
}

