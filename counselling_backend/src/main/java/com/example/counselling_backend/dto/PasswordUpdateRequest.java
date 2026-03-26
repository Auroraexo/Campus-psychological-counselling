package com.example.counselling_backend.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}