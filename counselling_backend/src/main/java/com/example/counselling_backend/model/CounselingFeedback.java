package com.example.counselling_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "counseling_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CounselingFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, unique = true)
    private CounselingRecord record;
    
    @Transient
    private Long recordId;
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(nullable = false)
    private Boolean anonymous = false;
    
    @Column(name = "counselor_response", columnDefinition = "TEXT")
    private String counselorResponse;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PostLoad
    private void postLoad() {
        if (record != null) {
            recordId = record.getId();
        }
    }
    
    @PrePersist
    @PreUpdate
    private void preSave() {
        if (recordId != null && record == null) {
            record = new CounselingRecord();
            record.setId(recordId);
        }
    }
}

