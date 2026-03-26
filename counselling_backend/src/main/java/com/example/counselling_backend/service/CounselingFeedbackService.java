package com.example.counselling_backend.service;

import com.example.counselling_backend.model.CounselingFeedback;
import com.example.counselling_backend.model.CounselingRecord;
import com.example.counselling_backend.repository.CounselingFeedbackRepository;
import com.example.counselling_backend.repository.CounselingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounselingFeedbackService {
    
    private final CounselingFeedbackRepository feedbackRepository;
    private final CounselingRecordRepository recordRepository;
    
    public List<CounselingFeedback> getAllFeedback() {
        // 使用自定义查询来预加载关联对象，避免LAZY加载问题
        return feedbackRepository.findAllWithRecords();
    }
    
    public Page<CounselingFeedback> searchFeedback(Integer rating, Boolean anonymous, String q, Pageable pageable) {
        return feedbackRepository.searchFeedback(rating, anonymous, q, pageable);
    }
    
    public Optional<CounselingFeedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }
    
    public Optional<CounselingFeedback> getFeedbackByRecordId(Long recordId) {
        return feedbackRepository.findByRecordId(recordId);
    }
    
    public CounselingFeedback createFeedback(CounselingFeedback feedback) {
        // 验证咨询记录是否存在
        CounselingRecord record = recordRepository.findById(feedback.getRecordId())
                .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
        
        // 检查该记录是否已有反馈
        Optional<CounselingFeedback> existing = feedbackRepository.findByRecordId(feedback.getRecordId());
        if (existing.isPresent() && !existing.get().getId().equals(feedback.getId())) {
            throw new RuntimeException("该咨询记录已存在反馈");
        }
        
        // 验证评分范围
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        feedback.setRecord(record);
        return feedbackRepository.save(feedback);
    }
    
    public CounselingFeedback updateFeedback(Long id, CounselingFeedback feedbackDetails) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    if (feedbackDetails.getRating() != null) {
                        if (feedbackDetails.getRating() < 1 || feedbackDetails.getRating() > 5) {
                            throw new RuntimeException("评分必须在1-5之间");
                        }
                        feedback.setRating(feedbackDetails.getRating());
                    }
                    if (feedbackDetails.getFeedback() != null) {
                        feedback.setFeedback(feedbackDetails.getFeedback());
                    }
                    if (feedbackDetails.getAnonymous() != null) {
                        feedback.setAnonymous(feedbackDetails.getAnonymous());
                    }
                    if (feedbackDetails.getCounselorResponse() != null) {
                        feedback.setCounselorResponse(feedbackDetails.getCounselorResponse());
                    }
                    return feedbackRepository.save(feedback);
                })
                .orElseThrow(() -> new RuntimeException("反馈记录不存在"));
    }
    
    public CounselingFeedback updateResponse(Long id, String counselorResponse) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    feedback.setCounselorResponse(counselorResponse);
                    return feedbackRepository.save(feedback);
                })
                .orElseThrow(() -> new RuntimeException("反馈记录不存在"));
    }
    
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}

