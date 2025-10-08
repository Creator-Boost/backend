package com.example.order_service.dto;

import com.example.order_service.entity.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    private Order.OrderStatus status;
}
