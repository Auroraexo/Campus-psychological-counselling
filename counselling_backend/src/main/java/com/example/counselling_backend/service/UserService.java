package com.example.counselling_backend.service;

import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 验证密码策略
        PasswordPolicyService.PasswordValidationResult validation = 
                passwordPolicyService.validatePassword(user.getPassword());
        if (!validation.isValid()) {
            throw new RuntimeException("密码不符合要求: " + validation.getErrorMessage());
        }
        
        // 加密密码
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 保存密码历史
        passwordPolicyService.savePasswordHistory(savedUser, hashedPassword);
        
        return savedUser;
    }
    
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    user.setRealName(userDetails.getRealName());
                    user.setRole(userDetails.getRole());
                    user.setActive(userDetails.getActive());
                    
                    // 只有在密码不为空时才更新密码
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        // 验证密码策略
                        PasswordPolicyService.PasswordValidationResult validation = 
                                passwordPolicyService.validatePassword(userDetails.getPassword());
                        if (!validation.isValid()) {
                            throw new RuntimeException("密码不符合要求: " + validation.getErrorMessage());
                        }
                        
                        // 验证密码历史
                        PasswordPolicyService.PasswordValidationResult historyValidation = 
                                passwordPolicyService.validatePasswordHistory(user, userDetails.getPassword());
                        if (!historyValidation.isValid()) {
                            throw new RuntimeException(historyValidation.getErrorMessage());
                        }
                        
                        // 加密新密码
                        String hashedPassword = passwordEncoder.encode(userDetails.getPassword());
                        user.setPassword(hashedPassword);
                        
                        // 保存密码历史
                        passwordPolicyService.savePasswordHistory(user, hashedPassword);
                    }
                    
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public Page<User> searchUsers(String role, String q, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User.Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role);
            } catch (IllegalArgumentException ignored) {
                roleEnum = null;
            }
        }
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return userRepository.searchUsers(roleEnum, active, query, pageable);
    }
}