package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.CounselingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CounselingRecordRepository extends JpaRepository<CounselingRecord, Long> {
    
    @Query("SELECT cr FROM CounselingRecord cr WHERE cr.student.id = :studentId")
    List<CounselingRecord> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT cr FROM CounselingRecord cr WHERE cr.counselor.id = :counselorId")
    List<CounselingRecord> findByCounselorId(@Param("counselorId") Long counselorId);
    
    List<CounselingRecord> findByStatus(CounselingRecord.Status status);
    
    @Query("SELECT cr FROM CounselingRecord cr WHERE cr.student.id = :studentId OR cr.counselor.id = :userId")
    List<CounselingRecord> findByStudentOrCounselor(@Param("studentId") Long studentId, @Param("userId") Long userId);
    
    @Query("SELECT cr FROM CounselingRecord cr WHERE cr.appointmentTime BETWEEN :startTime AND :endTime")
    List<CounselingRecord> findByAppointmentTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);
    
    @EntityGraph(attributePaths = {"student", "counselor"})
    @Query(value = """
        SELECT cr FROM CounselingRecord cr
        WHERE (:status IS NULL OR cr.status = :status)
          AND (
            :q IS NULL OR 
            LOWER(cr.student.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(cr.counselor.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(cr.summary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """,
        countQuery = """
        SELECT COUNT(cr) FROM CounselingRecord cr
        WHERE (:status IS NULL OR cr.status = :status)
          AND (
            :q IS NULL OR 
            LOWER(cr.student.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(cr.counselor.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(cr.summary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<CounselingRecord> searchRecords(@Param("status") CounselingRecord.Status status,
                                         @Param("q") String q,
                                         Pageable pageable);
    
    @Query("SELECT DISTINCT cr FROM CounselingRecord cr LEFT JOIN FETCH cr.student LEFT JOIN FETCH cr.counselor")
    List<CounselingRecord> findAllWithRelations();
}