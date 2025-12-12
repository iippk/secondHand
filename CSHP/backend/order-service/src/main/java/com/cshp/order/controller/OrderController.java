package com.cshp.order.controller;

import com.cshp.common.result.Result;
import com.cshp.common.util.HeaderUtil;
import com.cshp.order.dto.OrderCreateDTO;
import com.cshp.order.dto.OrderDTO;
import com.cshp.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<OrderDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto,
                                        @RequestHeader("X-Student-Id") String buyerId,
                                        @RequestHeader(value = "X-Buyer-Name", required = false) String buyerName,
                                        @RequestHeader(value = "X-User-Name", required = false) String userName) {
        // 解码可能包含非 ASCII 字符的请求头
        String decodedBuyerName = buyerName != null ? HeaderUtil.decodeHeaderValue(buyerName) : null;
        String decodedUserName = userName != null ? HeaderUtil.decodeHeaderValue(userName) : null;
        String finalBuyerName = StringUtils.hasText(decodedBuyerName) ? decodedBuyerName : decodedUserName;
        OrderDTO order = orderService.createOrder(dto, buyerId, finalBuyerName);
        return Result.success(order);
    }

    @GetMapping("/{id}")
    public Result<OrderDTO> getOrder(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return Result.success(order);
    }

    @GetMapping("/my-buy")
    public Result<List<OrderDTO>> getMyBuyOrders(@RequestHeader("X-Student-Id") String buyerId) {
        List<OrderDTO> orders = orderService.getOrdersByBuyerId(buyerId);
        return Result.success(orders);
    }

    @GetMapping("/my-sell")
    public Result<List<OrderDTO>> getMySellOrders(@RequestHeader("X-Student-Id") String sellerId) {
        List<OrderDTO> orders = orderService.getOrdersBySellerId(sellerId);
        return Result.success(orders);
    }

    @PutMapping("/{id}/pay")
    public Result<OrderDTO> payOrder(@PathVariable Long id,
                                     @RequestHeader("X-Student-Id") String buyerId) {
        OrderDTO order = orderService.payOrder(id, buyerId);
        return Result.success(order);
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderDTO> cancelOrder(@PathVariable Long id,
                                        @RequestHeader("X-Student-Id") String buyerId) {
        OrderDTO order = orderService.cancelOrder(id, buyerId);
        return Result.success(order);
    }

    @PutMapping("/{id}/ship")
    public Result<OrderDTO> shipOrder(@PathVariable Long id,
                                      @RequestHeader("X-Student-Id") String sellerId) {
        OrderDTO order = orderService.shipOrder(id, sellerId);
        return Result.success(order);
    }

    @PutMapping("/{id}/complete")
    public Result<OrderDTO> completeOrder(@PathVariable Long id,
                                          @RequestHeader("X-Student-Id") String buyerId) {
        OrderDTO order = orderService.completeOrder(id, buyerId);
        return Result.success(order);
    }

    @PutMapping("/{id}/refund")
    public Result<OrderDTO> refundOrder(@PathVariable Long id,
                                        @RequestHeader("X-Student-Id") String buyerId) {
        OrderDTO order = orderService.refundOrder(id, buyerId);
        return Result.success(order);
    }

    @PutMapping("/{id}/confirm-refund")
    public Result<OrderDTO> confirmRefund(@PathVariable Long id,
                                          @RequestHeader("X-Student-Id") String sellerId) {
        OrderDTO order = orderService.confirmRefund(id, sellerId);
        return Result.success(order);
    }
}

