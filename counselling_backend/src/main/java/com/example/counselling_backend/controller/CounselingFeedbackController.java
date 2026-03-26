package com.example.counselling_backend.controller;

import com.example.counselling_backend.model.CounselingFeedback;
import com.example.counselling_backend.service.CounselingFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/counseling/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CounselingFeedbackController {
    
    private final CounselingFeedbackService feedbackService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<?> getAllFeedback(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean anonymous,
            @RequestParam(required = false) String q) {
        
        try {
            System.out.println("收到获取反馈记录请求 - page: " + page + ", size: " + size + ", rating: " + rating + ", anonymous: " + anonymous + ", q: " + q);
            
            if (page != null && size != null) {
                Pageable pageable = PageRequest.of(page, size);
                Page<CounselingFeedback> feedback = feedbackService.searchFeedback(rating, anonymous, q, pageable);
                System.out.println("查询到的反馈记录数量: " + feedback.getTotalElements());
                return ResponseEntity.ok(feedback);
            }
            
            List<CounselingFeedback> feedback = feedbackService.getAllFeedback();
            System.out.println("查询到的反馈记录数量: " + feedback.size());
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            System.err.println("获取反馈记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取反馈记录失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<CounselingFeedback> getFeedbackById(@PathVariable Long id) {
        Optional<CounselingFeedback> feedback = feedbackService.getFeedbackById(id);
        return feedback.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<CounselingFeedback> getFeedbackByRecordId(@PathVariable Long recordId) {
        Optional<CounselingFeedback> feedback = feedbackService.getFeedbackByRecordId(recordId);
        return feedback.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<CounselingFeedback> createFeedback(@RequestBody CounselingFeedback feedback) {
        try {
            CounselingFeedback createdFeedback = feedbackService.createFeedback(feedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFeedback);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<CounselingFeedback> updateFeedback(
            @PathVariable Long id,
            @RequestBody CounselingFeedback feedbackDetails) {
        try {
            CounselingFeedback updatedFeedback = feedbackService.updateFeedback(id, feedbackDetails);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/response")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR')")
    public ResponseEntity<CounselingFeedback> updateResponse(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String counselorResponse = request.get("counselorResponse");
            CounselingFeedback updatedFeedback = feedbackService.updateResponse(id, counselorResponse);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}

