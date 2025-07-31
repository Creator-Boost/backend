package com.example.gig_service.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GigImageDTO {
    private String url;
    private boolean isPrimary;


    public Object isIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Object isPrimary) {
    }
}
