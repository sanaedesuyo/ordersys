package com.ordersys.user.dto;

import lombok.Data;

@Data
public class AddressRequest {
    private String label;
    private String detail;
    private Boolean isDefault;
}
