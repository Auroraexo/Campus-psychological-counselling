package com.example.counselling_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.counselling_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class PasswordUpdateController {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/update-all-passwords")
    public String updateAllPasswords(@RequestParam(defaultValue = "123456") String password) {
        String hashedPassword = passwordEncoder.encode(password);
        
        // 更新所有用户的密码
        userRepository.updateAllPasswords(hashedPassword);
        
        return "All passwords updated with hash: " + hashedPassword;
    }
}