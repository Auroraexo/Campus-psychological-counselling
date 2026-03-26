package com.example.counselling_backend.service;

import com.example.counselling_backend.dto.AuthResponse;
import com.example.counselling_backend.dto.LoginRequest;
import com.example.counselling_backend.dto.RegisterRequest;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.model.User.Role;
import com.example.counselling_backend.repository.UserRepository;
import com.example.counselling_backend.security.CustomUserDetailsService;
import com.example.counselling_backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private PasswordPolicyService passwordPolicyService;
    
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 加载用户详情
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        
        // 生成JWT令牌
        String token = jwtTokenUtil.generateToken(userDetails);
        
        // 获取用户信息
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 构建响应
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRealName(),
                user.getRole().name()
        );
    }
    
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 验证密码策略
        PasswordPolicyService.PasswordValidationResult validation = 
                passwordPolicyService.validatePassword(registerRequest.getPassword());
        if (!validation.isValid()) {
            throw new RuntimeException("密码不符合要求: " + validation.getErrorMessage());
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRealName(registerRequest.getRealName());
        
        // 设置角色，默认为STUDENT
        if (registerRequest.getRole() != null && 
            (registerRequest.getRole().equals("COUNSELOR") || registerRequest.getRole().equals("ADMIN"))) {
            user.setRole(Role.valueOf(registerRequest.getRole()));
        } else {
            user.setRole(Role.STUDENT);
        }
        
        user.setActive(true);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 保存密码历史
        passwordPolicyService.savePasswordHistory(savedUser, savedUser.getPassword());
        
        // 自动登录并生成令牌
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());
        
        return authenticateUser(loginRequest);
    }
}