package com.example.counselling_backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private String role; // STUDENT, COUNSELOR, ADMIN
}