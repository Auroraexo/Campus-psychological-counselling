package com.example.counselling_backend.controller;

import com.example.counselling_backend.model.User;
import com.example.counselling_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCounselor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COUNSELOR"));
        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        
        // 根据用户角色过滤显示内容
        if (isStudent) {
            // 学生只能查看自己
            q = currentUsername; // 使用用户名作为搜索条件，确保只能找到自己
            role = "STUDENT";
        } else if (!isAdmin && (role == null || !role.equals("COUNSELOR"))) {
            // 咨询师默认查看咨询师列表
            role = "COUNSELOR";
        }
        
        Page<User> result = userService.searchUsers(role, q, active, page, size);
        // 清除密码字段
        result.getContent().forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // 检查用户是否有权限访问该用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        return userService.getUserById(id)
                .map(user -> {
                    // 只有管理员或用户本人可以查看详细信息
                    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) 
                        || user.getUsername().equals(currentUsername)) {
                        // 清除密码字段
                        user.setPassword(null);
                        return ResponseEntity.ok(user);
                    } else {
                        // 返回部分信息
                        User publicUser = new User();
                        publicUser.setId(user.getId());
                        publicUser.setUsername(user.getUsername());
                        publicUser.setRealName(user.getRealName());
                        publicUser.setRole(user.getRole());
                        return ResponseEntity.ok(publicUser);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        // 检查用户是否有权限访问该用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        return userService.getUserByUsername(username)
                .map(user -> {
                    // 只有管理员或用户本人可以查看详细信息
                    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) 
                        || user.getUsername().equals(currentUsername)) {
                        // 清除密码字段
                        user.setPassword(null);
                        return ResponseEntity.ok(user);
                    } else {
                        // 返回部分信息
                        User publicUser = new User();
                        publicUser.setId(user.getId());
                        publicUser.setUsername(user.getUsername());
                        publicUser.setRealName(user.getRealName());
                        publicUser.setRole(user.getRole());
                        return ResponseEntity.ok(publicUser);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            // 清除密码字段
            createdUser.setPassword(null);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // 检查用户是否有权限更新该用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        try {
            // 只有管理员或用户本人可以更新信息
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) 
                || userService.getUserById(id).map(u -> u.getUsername().equals(currentUsername)).orElse(false)) {
                User updatedUser = userService.updateUser(id, userDetails);
                // 清除密码字段
                updatedUser.setPassword(null);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(403).build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<Page<User>> getStudents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 咨询师和管理员可以查看学生列表，用于创建咨询记录
        Page<User> result = userService.searchUsers("STUDENT", q, active, page, size);
        // 清除密码字段
        result.getContent().forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(result);
    }
}