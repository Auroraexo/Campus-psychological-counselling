package com.example.counselling_backend.service;

import com.example.counselling_backend.dto.CounselingSessionDTO;
import com.example.counselling_backend.model.CounselingRecord;
import com.example.counselling_backend.model.CounselingSession;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.CounselingRecordRepository;
import com.example.counselling_backend.repository.CounselingSessionRepository;
import com.example.counselling_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselingSessionService {
    
    private final CounselingSessionRepository sessionRepository;
    private final CounselingRecordRepository recordRepository;
    private final UserRepository userRepository;
    
    private CounselingSessionDTO convertToDTO(CounselingSession session) {
        CounselingSessionDTO dto = new CounselingSessionDTO();
        dto.setId(session.getId());
        dto.setRecordId(session.getRecordId());
        dto.setSessionNumber(session.getSessionNumber());
        dto.setSessionDate(session.getSessionDate());
        dto.setDuration(session.getDuration());
        dto.setSessionContent(session.getSessionContent());
        dto.setNextPlan(session.getNextPlan());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        
        // 加载关联数据，设置额外字段
        if (session.getRecord() != null) {
            dto.setRecordTitle(session.getRecord().getTitle());
            dto.setStudentName(session.getRecord().getStudent() != null ? 
                session.getRecord().getStudent().getRealName() : "");
            dto.setCounselorName(session.getRecord().getCounselor() != null ? 
                session.getRecord().getCounselor().getRealName() : "");
        }
        
        return dto;
    }
    
    public List<CounselingSessionDTO> getAllSessions() {
        // 使用自定义查询来预加载关联对象，避免LAZY加载问题
        List<CounselingSession> sessions = sessionRepository.findAllWithRecords();
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<CounselingSessionDTO> searchSessions(Long recordId, String q, Pageable pageable) {
        Page<CounselingSession> sessions = sessionRepository.searchSessions(recordId, q, pageable);
        return sessions.map(this::convertToDTO);
    }
    
    public Optional<CounselingSessionDTO> getSessionById(Long id) {
        Optional<CounselingSession> session = sessionRepository.findById(id);
        return session.map(this::convertToDTO);
    }
    
    public List<CounselingSessionDTO> getSessionsByRecordId(Long recordId) {
        List<CounselingSession> sessions = sessionRepository.findByRecordId(recordId);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CounselingSessionDTO createSession(CounselingSession session) {
        // 验证咨询记录是否存在
        CounselingRecord record = recordRepository.findById(session.getRecordId())
                .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
        
        // 检查会话序号是否已存在
        Optional<CounselingSession> existing = sessionRepository.findByRecordIdAndSessionNumber(
                session.getRecordId(), session.getSessionNumber());
        if (existing.isPresent() && !existing.get().getId().equals(session.getId())) {
            throw new RuntimeException("该咨询记录已存在相同序号的会话");
        }
        
        session.setRecord(record);
        CounselingSession savedSession = sessionRepository.save(session);
        return convertToDTO(savedSession);
    }
    
    public CounselingSessionDTO updateSession(Long id, CounselingSession sessionDetails) {
        CounselingSession updatedSession = sessionRepository.findById(id)
                .map(session -> {
                    session.setSessionNumber(sessionDetails.getSessionNumber());
                    session.setSessionDate(sessionDetails.getSessionDate());
                    session.setDuration(sessionDetails.getDuration());
                    session.setSessionContent(sessionDetails.getSessionContent());
                    session.setNextPlan(sessionDetails.getNextPlan());
                    
                    // 如果提供了新的记录ID，更新记录
                    if (sessionDetails.getRecordId() != null && 
                        !sessionDetails.getRecordId().equals(session.getRecord().getId())) {
                        CounselingRecord newRecord = recordRepository.findById(sessionDetails.getRecordId())
                                .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
                        session.setRecord(newRecord);
                    }
                    
                    // 检查会话序号是否冲突
                    if (sessionDetails.getSessionNumber() != null) {
                        Optional<CounselingSession> existing = sessionRepository.findByRecordIdAndSessionNumber(
                                session.getRecord().getId(), sessionDetails.getSessionNumber());
                        if (existing.isPresent() && !existing.get().getId().equals(id)) {
                            throw new RuntimeException("该咨询记录已存在相同序号的会话");
                        }
                    }
                    
                    return sessionRepository.save(session);
                })
                .orElseThrow(() -> new RuntimeException("会话记录不存在"));
        
        return convertToDTO(updatedSession);
    }
    
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }
    
    // 获取学生自己的会话记录
    public List<CounselingSessionDTO> getAllSessionsForStudent(String username) {
        // 获取当前学生用户
        Optional<User> studentOpt = userRepository.findByUsername(username);
        if (studentOpt.isEmpty() || !studentOpt.get().getRole().equals("STUDENT")) {
            return List.of();
        }
        
        Long studentId = studentOpt.get().getId();
        
        // 获取该学生相关的所有咨询记录
        List<CounselingRecord> studentRecords = recordRepository.findByStudentId(studentId);
        
        // 获取这些记录的所有会话
        return studentRecords.stream()
                .flatMap(record -> sessionRepository.findByRecordId(record.getId()).stream())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // 分页查询学生自己的会话记录
    public Page<CounselingSessionDTO> searchSessionsForStudent(String username, Long recordId, String q, Pageable pageable) {
        // 获取当前学生用户
        Optional<User> studentOpt = userRepository.findByUsername(username);
        if (studentOpt.isEmpty() || !studentOpt.get().getRole().equals("STUDENT")) {
            return Page.empty(pageable);
        }
        
        Long studentId = studentOpt.get().getId();
        
        // 获取该学生相关的所有咨询记录ID
        List<Long> studentRecordIds = recordRepository.findByStudentId(studentId).stream()
                .map(CounselingRecord::getId)
                .collect(Collectors.toList());
        
        // 如果没有记录，返回空页面
        if (studentRecordIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // 使用自定义查询，只查询学生相关的会话
        Page<CounselingSession> sessions = sessionRepository.searchSessionsForStudent(studentRecordIds, recordId, q, pageable);
        return sessions.map(this::convertToDTO);
    }
}

