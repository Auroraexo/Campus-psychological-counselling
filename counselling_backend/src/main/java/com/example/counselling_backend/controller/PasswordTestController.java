package com.example.counselling_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class PasswordTestController {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @GetMapping("/check-password")
    public String checkPassword(@RequestParam String password) {
        String hashedPassword = "$2a$10$v46Mk7MyxFOVMUPWTxuzFuL1x7qrmec22ot67XY7jO24q44HZZTvG";
        
        if (passwordEncoder.matches(password, hashedPassword)) {
            return "Password matches: " + password;
        } else {
            return "Password does not match: " + password;
        }
    }
    
    @GetMapping("/encode-password")
    public String encodePassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}