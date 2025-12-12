package com.cshp.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.common.exception.BusinessException;
import com.cshp.common.result.Result;
import com.cshp.order.dto.OrderCreateDTO;
import com.cshp.order.dto.OrderDTO;
import com.cshp.order.entity.Order;
import com.cshp.order.feign.ProductFeignClient;
import com.cshp.order.feign.dto.ProductFeignDTO;
import com.cshp.order.mapper.OrderMapper;
import com.cshp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductFeignClient productFeignClient;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO dto, String buyerId, String buyerName) {
        ProductFeignDTO product = loadProduct(dto.getProductId());
        if (product.getStatus() != null && product.getStatus() != 0) {
            throw new BusinessException("商品已售出或不可购买");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setProductImage(getFirstImage(product.getImages()));
        order.setPrice(product.getPrice());
        order.setSellerId(product.getSellerId());
        // 不再设置 sellerName，因为数据库中不存在该字段
        order.setBuyerId(buyerId);
        order.setBuyerName(StringUtils.hasText(buyerName) ? buyerName : buyerId);
        order.setAddress(dto.getAddress());
        order.setPhone(dto.getPhone());
        order.setRemark(dto.getRemark());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        orderMapper.insert(order);
        return convertToDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByBuyerId(String buyerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", buyerId);
        wrapper.orderByDesc("create_time");
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersBySellerId(String sellerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId);
        wrapper.orderByDesc("create_time");
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO payOrder(Long id, String buyerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        updateProductStatus(order.getProductId(), 1);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long id, String buyerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        updateProductStatus(order.getProductId(), 0);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO shipOrder(Long id, String sellerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        updateProductShipped(order.getProductId(), 1);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO completeOrder(Long id, String buyerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO refundOrder(Long id, String buyerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，无法退款");
        }
        
        order.setStatus(5);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO confirmRefund(Long id, String sellerId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 5) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus(6);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        updateProductStatus(order.getProductId(), 0);
        updateProductShipped(order.getProductId(), 0);
        return convertToDTO(order);
    }

    private ProductFeignDTO loadProduct(Long productId) {
        Result<ProductFeignDTO> result = productFeignClient.getProduct(productId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException("查询商品信息失败");
        }
        return result.getData();
    }

    private void updateProductStatus(Long productId, Integer status) {
        Result<Void> result = productFeignClient.updateStatus(productId, status);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException("更新商品状态失败");
        }
    }

    private void updateProductShipped(Long productId, Integer shipped) {
        Result<Void> result = productFeignClient.updateShipped(productId, shipped);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException("更新商品发货状态失败");
        }
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

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
}

