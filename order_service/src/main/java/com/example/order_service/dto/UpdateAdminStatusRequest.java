package com.example.order_service.dto;

import com.example.order_service.entity.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminStatusRequest {
    private Order.AdminStatus adminStatus;

    public UpdateAdminStatusRequest() {}

    public UpdateAdminStatusRequest(Order.AdminStatus adminStatus) {
        this.adminStatus = adminStatus;
    }
}
