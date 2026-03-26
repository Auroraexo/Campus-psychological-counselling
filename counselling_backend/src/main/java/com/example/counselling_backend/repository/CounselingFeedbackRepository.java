package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.CounselingFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounselingFeedbackRepository extends JpaRepository<CounselingFeedback, Long> {
    
    @Query("SELECT DISTINCT cf FROM CounselingFeedback cf LEFT JOIN FETCH cf.record r LEFT JOIN FETCH r.student LEFT JOIN FETCH r.counselor")
    List<CounselingFeedback> findAllWithRecords();
    
    @Query("SELECT cf FROM CounselingFeedback cf LEFT JOIN FETCH cf.record r LEFT JOIN FETCH r.student LEFT JOIN FETCH r.counselor WHERE r.id = :recordId")
    Optional<CounselingFeedback> findByRecordId(@Param("recordId") Long recordId);
    
    @Query("""
        SELECT DISTINCT cf FROM CounselingFeedback cf
        LEFT JOIN FETCH cf.record r
        LEFT JOIN FETCH r.student
        LEFT JOIN FETCH r.counselor
        WHERE (:rating IS NULL OR cf.rating = :rating)
          AND (:anonymous IS NULL OR cf.anonymous = :anonymous)
          AND (
            :q IS NULL OR 
            LOWER(r.student.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(r.counselor.realName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(cf.feedback, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<CounselingFeedback> searchFeedback(@Param("rating") Integer rating,
                                            @Param("anonymous") Boolean anonymous,
                                            @Param("q") String q,
                                            Pageable pageable);
}

