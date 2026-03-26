package com.example.counselling_backend.controller;

import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 健康检查接口
    @GetMapping("/health")
    public String healthCheck() {
        return "服务运行正常";
    }
    
    // 临时密码重置接口 - 仅用于测试
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        try {
            // 查找用户
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setActive(true); // 确保用户处于激活状态
            userRepository.save(user);
            
            return "用户密码已重置: " + username;
        } catch (Exception e) {
            return "重置失败: " + e.getMessage();
        }
    }
    
    // 测试密码匹配
    @GetMapping("/test-password")
    public String testPassword(@RequestParam String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        
        return "原始密码: " + rawPassword + "\n" +
               "编码密码: " + encodedPassword + "\n" +
               "匹配结果: " + matches;
    }
}
