package com.cshp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cshp.order.dto.OrderDTO;
import com.cshp.order.dto.OrderUpdateDTO;
import com.cshp.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}

