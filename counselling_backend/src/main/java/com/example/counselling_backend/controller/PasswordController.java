package com.example.counselling_backend.controller;

import com.example.counselling_backend.dto.PasswordUpdateRequest;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.service.PasswordPolicyService;
import com.example.counselling_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {
    
    private final UserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 更新当前用户密码
     */
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody PasswordUpdateRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            User currentUser = userService.getUserByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 验证当前密码
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                response.put("status", "error");
                response.put("message", "当前密码不正确");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证新密码策略
            PasswordPolicyService.PasswordValidationResult validation = 
                    passwordPolicyService.validatePassword(request.getNewPassword());
            if (!validation.isValid()) {
                response.put("status", "error");
                response.put("message", "新密码不符合要求: " + validation.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证新密码是否与当前密码相同
            if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
                response.put("status", "error");
                response.put("message", "新密码不能与当前密码相同");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证密码历史
            PasswordPolicyService.PasswordValidationResult historyValidation = 
                    passwordPolicyService.validatePasswordHistory(currentUser, request.getNewPassword());
            if (!historyValidation.isValid()) {
                response.put("status", "error");
                response.put("message", historyValidation.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 更新密码
            User updatedUser = new User();
            updatedUser.setPassword(request.getNewPassword());
            userService.updateUser(currentUser.getId(), updatedUser);
            
            response.put("status", "success");
            response.put("message", "密码更新成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "密码更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 验证密码强度
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String password = request.get("password");
        if (password == null) {
            response.put("valid", false);
            response.put("message", "密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        PasswordPolicyService.PasswordValidationResult validation = 
                passwordPolicyService.validatePassword(password);
        
        response.put("valid", validation.isValid());
        response.put("message", validation.getErrorMessage());
        
        return ResponseEntity.ok(response);
    }
}