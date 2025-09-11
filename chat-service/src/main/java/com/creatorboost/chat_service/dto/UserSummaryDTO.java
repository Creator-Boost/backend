package com.creatorboost.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryDTO {
    private String userId;
    private String name;
    private String imageUrl;
}