package com.example.counselling_backend.controller;

import com.example.counselling_backend.dto.CounselingSessionDTO;
import com.example.counselling_backend.model.CounselingSession;
import com.example.counselling_backend.service.CounselingSessionService;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/counseling/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CounselingSessionController {
    
    private final CounselingSessionService sessionService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<?> getAllSessions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long recordId,
            @RequestParam(required = false) String q) {
        
        try {
            System.out.println("收到获取会话记录请求 - page: " + page + ", size: " + size + ", recordId: " + recordId + ", q: " + q);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            
            // 如果是学生，只能查看与自己相关的会话记录
            if (isStudent) {
                if (page != null && size != null) {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<CounselingSessionDTO> sessions = sessionService.searchSessionsForStudent(currentUsername, recordId, q, pageable);
                    System.out.println("学生查询到的会话记录数量: " + sessions.getTotalElements());
                    return ResponseEntity.ok(sessions);
                }
                
                List<CounselingSessionDTO> sessions = sessionService.getAllSessionsForStudent(currentUsername);
                System.out.println("学生查询到的会话记录数量: " + sessions.size());
                return ResponseEntity.ok(sessions);
            } else {
                // 管理员和咨询师可以查看所有会话记录
                if (page != null && size != null) {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<CounselingSessionDTO> sessions = sessionService.searchSessions(recordId, q, pageable);
                    System.out.println("查询到的会话记录数量: " + sessions.getTotalElements());
                    return ResponseEntity.ok(sessions);
                }
                
                List<CounselingSessionDTO> sessions = sessionService.getAllSessions();
                System.out.println("查询到的会话记录数量: " + sessions.size());
                return ResponseEntity.ok(sessions);
            }
        } catch (Exception e) {
            System.err.println("获取会话记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取会话记录失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<CounselingSessionDTO> getSessionById(@PathVariable Long id) {
        Optional<CounselingSessionDTO> session = sessionService.getSessionById(id);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")
    public ResponseEntity<List<CounselingSessionDTO>> getSessionsByRecordId(@PathVariable Long recordId) {
        List<CounselingSessionDTO> sessions = sessionService.getSessionsByRecordId(recordId);
        return ResponseEntity.ok(sessions);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> createSession(@RequestBody CounselingSession session) {
        try {
            System.out.println("收到创建会话记录请求");
            System.out.println("recordId: " + session.getRecordId());
            System.out.println("sessionNumber: " + session.getSessionNumber());
            System.out.println("sessionDate: " + session.getSessionDate());
            System.out.println("duration: " + session.getDuration());
            System.out.println("sessionContent: " + session.getSessionContent());
            System.out.println("nextPlan: " + session.getNextPlan());
            
            // 验证必填字段
            if (session.getRecordId() == null) {
                return ResponseEntity.badRequest().body("咨询记录ID不能为空");
            }
            if (session.getSessionNumber() == null) {
                return ResponseEntity.badRequest().body("会话序号不能为空");
            }
            if (session.getSessionDate() == null) {
                return ResponseEntity.badRequest().body("会话时间不能为空");
            }
            
            CounselingSessionDTO createdSession = sessionService.createSession(session);
            System.out.println("会话记录创建成功，ID: " + createdSession.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
        } catch (RuntimeException e) {
            System.err.println("创建会话记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("创建会话记录时发生未预期的错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> updateSession(
            @PathVariable Long id,
            @RequestBody CounselingSession sessionDetails) {
        try {
            CounselingSessionDTO updatedSession = sessionService.updateSession(id, sessionDetails);
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}

