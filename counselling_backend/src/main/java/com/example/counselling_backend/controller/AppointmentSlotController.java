package com.example.counselling_backend.controller;

import com.example.counselling_backend.model.AppointmentSlot;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.UserRepository;
import com.example.counselling_backend.service.AppointmentSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment-slots")
@CrossOrigin(origins = "*")
public class AppointmentSlotController {

    private final AppointmentSlotService appointmentSlotService;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentSlotController(AppointmentSlotService appointmentSlotService, UserRepository userRepository) {
        this.appointmentSlotService = appointmentSlotService;
        this.userRepository = userRepository;
    }
    
    // 获取当前登录用户的ID
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
    
    // 检查是否是管理员
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // 获取所有预约时间段（支持按咨询师ID和日期筛选）
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<?> getAllAppointmentSlots(
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) String date) {

        try {
            System.out.println("收到获取预约时间段请求 - counselorId: " + counselorId + ", date: " + date);

            LocalDate slotDate = null;
            if (date != null && !date.trim().isEmpty()) {
                try {
                    slotDate = LocalDate.parse(date);
                    System.out.println("解析后的日期: " + slotDate);
                } catch (Exception e) {
                    System.out.println("日期解析失败: " + e.getMessage());
                    return ResponseEntity.badRequest().body("日期格式错误，请使用格式：yyyy-MM-dd");
                }
            }

            // 如果是咨询师，只能查看自己的时间段
            if (!isAdmin()) {
                Long currentUserId = getCurrentUserId();
                System.out.println("当前用户ID: " + currentUserId);
                if (currentUserId != null) {
                    counselorId = currentUserId;
                    System.out.println("咨询师角色，设置counselorId为: " + counselorId);
                }
            }

            List<AppointmentSlot> slots = appointmentSlotService.getAppointmentSlotsByCounselorAndDate(counselorId, slotDate);
            System.out.println("查询到的预约时间段数量: " + slots.size());
            if (!slots.isEmpty()) {
                System.out.println("第一个时间段示例: " + slots.get(0));
            }
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            System.err.println("获取预约时间段时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("加载预约时间段失败: " + e.getMessage());
        }
    }

    // 根据ID获取预约时间段
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<AppointmentSlot> getAppointmentSlotById(@PathVariable Long id) {
        return appointmentSlotService.getAppointmentSlotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建新的预约时间段（管理员和咨询师可以创建）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> createAppointmentSlot(@RequestBody AppointmentSlot appointmentSlot) {
        try {
            // 如果是咨询师，只能创建自己的时间段
            if (!isAdmin()) {
                Long currentUserId = getCurrentUserId();
                if (currentUserId != null) {
                    appointmentSlot.setCounselorId(currentUserId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法获取当前用户信息");
                }
            }
            
            AppointmentSlot createdSlot = appointmentSlotService.createAppointmentSlot(appointmentSlot);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSlot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 更新预约时间段（管理员和咨询师可以更新）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> updateAppointmentSlot(@PathVariable Long id, @RequestBody AppointmentSlot appointmentSlot) {
        try {
            // 检查权限：咨询师只能更新自己的时间段
            if (!isAdmin()) {
                Long currentUserId = getCurrentUserId();
                AppointmentSlot existingSlot = appointmentSlotService.getAppointmentSlotById(id)
                        .orElseThrow(() -> new RuntimeException("预约时间段不存在"));
                
                if (!existingSlot.getCounselorId().equals(currentUserId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只能修改自己的预约时间段");
                }
                
                // 确保咨询师不能修改counselorId
                appointmentSlot.setCounselorId(currentUserId);
            }
            
            AppointmentSlot updatedSlot = appointmentSlotService.updateAppointmentSlot(id, appointmentSlot);
            return ResponseEntity.ok(updatedSlot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 删除预约时间段（管理员和咨询师可以删除）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> deleteAppointmentSlot(@PathVariable Long id) {
        try {
            // 检查权限：咨询师只能删除自己的时间段
            if (!isAdmin()) {
                Long currentUserId = getCurrentUserId();
                AppointmentSlot existingSlot = appointmentSlotService.getAppointmentSlotById(id)
                        .orElseThrow(() -> new RuntimeException("预约时间段不存在"));
                
                if (!existingSlot.getCounselorId().equals(currentUserId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只能删除自己的预约时间段");
                }
            }
            
            appointmentSlotService.deleteAppointmentSlot(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 预约时间段（学生预约）
    @PostMapping("/{id}/book")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> bookSlot(@PathVariable Long id, @RequestParam Long studentId) {
        try {
            AppointmentSlot bookedSlot = appointmentSlotService.bookSlot(id, studentId);
            return ResponseEntity.ok(bookedSlot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 取消预约
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<?> cancelSlot(@PathVariable Long id) {
        try {
            AppointmentSlot cancelledSlot = appointmentSlotService.cancelSlot(id);
            return ResponseEntity.ok(cancelledSlot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
