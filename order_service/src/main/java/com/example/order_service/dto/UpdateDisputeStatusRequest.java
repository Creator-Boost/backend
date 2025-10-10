package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDisputeStatusRequest {
    private Boolean resolved;

    public UpdateDisputeStatusRequest() {}

    public UpdateDisputeStatusRequest(Boolean resolved) {
        this.resolved = resolved;
    }
}
