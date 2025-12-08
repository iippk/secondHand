package com.cshp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private String buyerId;
    private String buyerName;
    private String sellerId;
    // sellerName 字段已移除，因为数据库中不存在该字段
    // 如需获取卖家名称，可通过 sellerId 关联查询 user 表
    private Integer status; // 0:待付款 1:已付款 2:已发货 3:已完成 4:已取消 5:退款中 6:已退款
    private String address;
    private String phone;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
}

