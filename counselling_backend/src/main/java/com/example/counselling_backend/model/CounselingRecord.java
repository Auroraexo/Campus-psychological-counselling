package com.example.counselling_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "counseling_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CounselingRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User counselor;
    
    // 为了兼容前端，添加studentId和counselorId字段
    @Transient
    private Long studentId;
    
    @Transient
    private Long counselorId;
    
    @Column(name = "appointment_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    
    @Column(name = "title", length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 为了兼容前端，在获取数据时设置studentId和counselorId
    @PostLoad
    private void postLoad() {
        if (student != null) {
            studentId = student.getId();
        }
        if (counselor != null) {
            counselorId = counselor.getId();
        }
    }
    
    // 注意：不要在这里设置student和counselor对象
    // 这些应该由Service层在保存前设置，以确保实体被正确加载
    // preSave方法已移除，因为创建只有ID的User对象会导致JPA问题
    
    public enum Status {
        PENDING("待处理"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static Status fromDisplayName(String displayName) {
            for (Status status : values()) {
                if (status.displayName.equals(displayName)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的显示名称: " + displayName);
        }
    }
}