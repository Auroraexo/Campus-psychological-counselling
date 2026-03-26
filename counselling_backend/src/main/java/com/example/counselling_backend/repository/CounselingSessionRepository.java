package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.CounselingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {
    
    @Query("SELECT DISTINCT cs FROM CounselingSession cs LEFT JOIN FETCH cs.record r LEFT JOIN FETCH r.student LEFT JOIN FETCH r.counselor")
    List<CounselingSession> findAllWithRecords();
    
    @Query("SELECT cs FROM CounselingSession cs LEFT JOIN FETCH cs.record r LEFT JOIN FETCH r.student LEFT JOIN FETCH r.counselor WHERE cs.record.id = :recordId")
    List<CounselingSession> findByRecordId(@Param("recordId") Long recordId);
    
    @Query("SELECT cs FROM CounselingSession cs LEFT JOIN FETCH cs.record WHERE cs.record.id = :recordId AND cs.sessionNumber = :sessionNumber")
    Optional<CounselingSession> findByRecordIdAndSessionNumber(@Param("recordId") Long recordId, @Param("sessionNumber") Integer sessionNumber);
    
    @Query("""
        SELECT DISTINCT cs FROM CounselingSession cs
        LEFT JOIN FETCH cs.record r
        LEFT JOIN FETCH r.student
        LEFT JOIN FETCH r.counselor
        WHERE (:recordId IS NULL OR r.id = :recordId)
          AND (
            :q IS NULL OR 
            LOWER(COALESCE(r.summary, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(r.student.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(r.counselor.realName) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<CounselingSession> searchSessions(@Param("recordId") Long recordId,
                                           @Param("q") String q,
                                           Pageable pageable);
    
    // 针对学生的查询，只返回与学生相关记录的会话
    @Query("""
        SELECT DISTINCT cs FROM CounselingSession cs
        LEFT JOIN FETCH cs.record r
        LEFT JOIN FETCH r.student
        LEFT JOIN FETCH r.counselor
        WHERE r.id IN :studentRecordIds
          AND (:recordId IS NULL OR r.id = :recordId)
          AND (
            :q IS NULL OR 
            LOWER(COALESCE(r.summary, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(r.student.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(r.counselor.realName) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<CounselingSession> searchSessionsForStudent(@Param("studentRecordIds") List<Long> studentRecordIds,
                                                     @Param("recordId") Long recordId,
                                                     @Param("q") String q,
                                                     Pageable pageable);
}

