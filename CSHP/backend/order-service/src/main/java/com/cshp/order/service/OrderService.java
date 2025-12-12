package com.cshp.order.service;

import com.cshp.order.dto.OrderCreateDTO;
import com.cshp.order.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(OrderCreateDTO dto, String buyerId, String buyerName);
    
    OrderDTO getOrderById(Long id);
    
    List<OrderDTO> getOrdersByBuyerId(String buyerId);
    
    List<OrderDTO> getOrdersBySellerId(String sellerId);
    
    OrderDTO payOrder(Long id, String buyerId);
    
    OrderDTO cancelOrder(Long id, String buyerId);
    
    OrderDTO shipOrder(Long id, String sellerId);
    
    OrderDTO completeOrder(Long id, String buyerId);
    
    OrderDTO refundOrder(Long id, String buyerId);
    
    OrderDTO confirmRefund(Long id, String sellerId);
}

