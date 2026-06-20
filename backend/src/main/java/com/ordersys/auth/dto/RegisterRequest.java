package com.ordersys.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String phone;
    private String password;
    private String address;
}
