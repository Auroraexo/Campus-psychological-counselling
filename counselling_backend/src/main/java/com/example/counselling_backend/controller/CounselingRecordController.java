package com.example.counselling_backend.controller;

import com.example.counselling_backend.model.CounselingRecord;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.UserRepository;
import com.example.counselling_backend.service.CounselingRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CounselingRecordController {
    
    private final CounselingRecordService counselingRecordService;
    private final UserRepository userRepository;
    
    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<?> getAllRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        
        System.out.println("=== 收到获取咨询记录请求 ===");
        System.out.println("page: " + page + ", size: " + size + ", status: " + status + ", q: " + q);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        
        // 如果是学生，只能查看自己的记录
        if (isStudent) {
            User student = userRepository.findByUsername(currentUsername).orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 如果提供了分页参数，返回分页结果
            if (page != null && size != null) {
                Pageable pageable = PageRequest.of(page, size);
                CounselingRecord.Status recordStatus = null;
                if (status != null && !status.trim().isEmpty()) {
                    try {
                        recordStatus = CounselingRecord.Status.valueOf(status.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的状态值
                    }
                }
                
                // 只查询该学生的记录
                List<CounselingRecord> studentRecords = counselingRecordService.getRecordsByStudentId(student.getId());
                
                // 应用状态过滤
                final CounselingRecord.Status finalRecordStatus = recordStatus;
                if (finalRecordStatus != null) {
                    studentRecords = studentRecords.stream()
                            .filter(r -> r.getStatus() == finalRecordStatus)
                            .collect(java.util.stream.Collectors.toList());
                }
                
                // 应用搜索过滤
                if (q != null && !q.trim().isEmpty()) {
                    final String searchQuery = q.toLowerCase();
                    studentRecords = studentRecords.stream()
                            .filter(r -> r.getTitle().toLowerCase().contains(searchQuery) || 
                                    (r.getSummary() != null && r.getSummary().toLowerCase().contains(searchQuery)))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                // 手动分页
                int start = page * size;
                int end = Math.min(start + size, studentRecords.size());
                List<CounselingRecord> pageRecords = start < studentRecords.size() ? 
                        studentRecords.subList(start, end) : List.of();
                
                Page<CounselingRecord> records = new org.springframework.data.domain.PageImpl<>(
                        pageRecords, pageable, studentRecords.size());
                
                System.out.println("学生分页查询结果: 总数=" + records.getTotalElements() + ", 当前页数据量=" + records.getContent().size());
                return ResponseEntity.ok(records);
            }
            
            // 否则返回该学生的所有记录
            List<CounselingRecord> studentRecords = counselingRecordService.getRecordsByStudentId(student.getId());
            System.out.println("学生查询所有记录: 总数=" + studentRecords.size());
            return ResponseEntity.ok(studentRecords);
        }
        
        // 管理员和咨询师可以查看所有记录
        // 如果提供了分页参数，返回分页结果
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            CounselingRecord.Status recordStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    recordStatus = CounselingRecord.Status.valueOf(status.toUpperCase().trim());
                } catch (IllegalArgumentException e) {
                    // 忽略无效的状态值
                }
            }
            Page<CounselingRecord> records = counselingRecordService.searchRecords(recordStatus, q, pageable);
            System.out.println("分页查询结果: 总数=" + records.getTotalElements() + ", 当前页数据量=" + records.getContent().size());
            if (!records.getContent().isEmpty()) {
                CounselingRecord first = records.getContent().get(0);
                System.out.println("第一条记录: id=" + first.getId() + ", studentId=" + first.getStudentId() + ", counselorId=" + first.getCounselorId());
            }
            return ResponseEntity.ok(records);
        }
        
        // 否则返回所有记录（保持向后兼容）
        List<CounselingRecord> allRecords = counselingRecordService.getAllRecords();
        System.out.println("查询所有记录: 总数=" + allRecords.size());
        return ResponseEntity.ok(allRecords);
    }
    
    @GetMapping("/records/{id}")
    public ResponseEntity<CounselingRecord> getRecordById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        if (!counselingRecordService.isRecordOwner(id, currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<CounselingRecord> record = counselingRecordService.getRecordById(id);
        return record.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/records/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<List<CounselingRecord>> getRecordsByStudentId(
            @PathVariable Long studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 如果是学生，只能查看自己的记录
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            if (!counselingRecordService.isStudentOwner(studentId, currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        List<CounselingRecord> records = counselingRecordService.getRecordsByStudentId(studentId);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/records/counselor/{counselorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<List<CounselingRecord>> getRecordsByCounselorId(
            @PathVariable Long counselorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 如果是咨询师，只能查看自己的记录
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COUNSELOR"))) {
            if (!counselingRecordService.isCounselorOwner(counselorId, currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        List<CounselingRecord> records = counselingRecordService.getRecordsByCounselorId(counselorId);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/records/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<List<CounselingRecord>> getRecordsByStatus(@PathVariable String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            CounselingRecord.Status recordStatus;
            try {
                recordStatus = CounselingRecord.Status.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                // Try to find status by display name or other formats
                recordStatus = CounselingRecord.Status.fromDisplayName(status);
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // 如果是咨询师，只能查看自己负责的记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COUNSELOR"))) {
                Long currentUserId = userRepository.findByUsername(currentUsername).get().getId();
                List<CounselingRecord> allRecords = counselingRecordService.getRecordsByStatus(recordStatus);
                List<CounselingRecord> filteredRecords = allRecords.stream()
                    .filter(record -> record.getCounselor().getId().equals(currentUserId))
                    .collect(java.util.stream.Collectors.toList());
                return ResponseEntity.ok(filteredRecords);
            }
            
            return ResponseEntity.ok(counselingRecordService.getRecordsByStatus(recordStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 测试端点，用于验证路径映射
    @GetMapping("/records/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("咨询记录API路径正常");
    }
    
    @PostMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> createRecord(@RequestBody CounselingRecord record) {
        System.out.println("=== 收到创建咨询记录请求 ===");
        System.out.println("请求路径: /api/counseling/records");
        System.out.println("请求方法: POST");
        try {
            System.out.println("收到创建咨询记录请求");
            System.out.println("studentId: " + record.getStudentId());
            System.out.println("counselorId: " + record.getCounselorId());
            System.out.println("appointmentTime: " + record.getAppointmentTime());
            System.out.println("status: " + record.getStatus());
            System.out.println("summary: " + record.getSummary());
            System.out.println("content: " + record.getContent());
            System.out.println("title: " + record.getTitle());
            
            // 验证必填字段
            if (record.getStudentId() == null) {
                return ResponseEntity.badRequest().body("学生ID不能为空");
            }
            if (record.getCounselorId() == null) {
                return ResponseEntity.badRequest().body("咨询师ID不能为空");
            }
            if (record.getStatus() == null) {
                // 如果前端没有提供状态，设置默认值
                record.setStatus(CounselingRecord.Status.PENDING);
                System.out.println("设置默认状态为: " + record.getStatus());
            }
            
            // 验证枚举值
            if (record.getStatus() != null) {
                try {
                    CounselingRecord.Status.valueOf(record.getStatus().toString());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("状态值无效，请使用: PENDING, IN_PROGRESS, COMPLETED, CANCELLED");
                }
            }
            
            // 验证日期格式
            if (record.getAppointmentTime() != null) {
                try {
                    // 验证日期格式是否正确
                    System.out.println("日期时间验证通过: " + record.getAppointmentTime());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("日期时间格式错误，请使用: yyyy-MM-dd HH:mm:ss");
                }
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // 如果是咨询师，只能创建自己负责的记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COUNSELOR"))) {
                Long currentUserId = userRepository.findByUsername(currentUsername)
                        .map(User::getId)
                        .orElse(null);
                if (currentUserId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法获取当前用户信息");
                }
                // 使用counselorId而不是getCounselor().getId()，因为前端只发送ID
                Long counselorId = record.getCounselorId();
                if (counselorId == null && record.getCounselor() != null) {
                    counselorId = record.getCounselor().getId();
                }
                if (counselorId == null || !counselorId.equals(currentUserId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只能创建自己负责的记录");
                }
            }
            
            CounselingRecord createdRecord = counselingRecordService.createRecord(record);
            System.out.println("咨询记录创建成功，ID: " + createdRecord.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
        } catch (RuntimeException e) {
            System.err.println("创建咨询记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("创建咨询记录时发生未预期的错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> updateRecord(
            @PathVariable Long id, 
            @RequestBody CounselingRecord recordDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 如果是咨询师，只能更新自己负责的记录
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COUNSELOR"))) {
            Optional<CounselingRecord> existingRecord = counselingRecordService.getRecordById(id);
            if (existingRecord.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("咨询记录不存在");
            }
            
            Long currentUserId = userRepository.findByUsername(currentUsername)
                    .map(User::getId)
                    .orElse(null);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无法获取当前用户信息");
            }
            
            if (!existingRecord.get().getCounselor().getId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只能更新自己负责的记录");
            }
        }
        
        try {
            CounselingRecord updatedRecord = counselingRecordService.updateRecord(id, recordDetails);
            return ResponseEntity.ok(updatedRecord);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN') or @counselingRecordService.isRecordOwner(#id, authentication.name)")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        counselingRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}