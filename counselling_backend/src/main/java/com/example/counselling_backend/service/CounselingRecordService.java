package com.example.counselling_backend.service;

import com.example.counselling_backend.model.CounselingRecord;
import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.CounselingRecordRepository;
import com.example.counselling_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounselingRecordService {
    
    private final CounselingRecordRepository counselingRecordRepository;
    private final UserRepository userRepository;
    
    public List<CounselingRecord> getAllRecords() {
        // 使用自定义查询来预加载关联对象
        return counselingRecordRepository.findAllWithRelations();
    }
    
    public Page<CounselingRecord> searchRecords(CounselingRecord.Status status, String q, Pageable pageable) {
        return counselingRecordRepository.searchRecords(status, q, pageable);
    }
    
    public Optional<CounselingRecord> getRecordById(Long id) {
        return counselingRecordRepository.findById(id);
    }
    
    public List<CounselingRecord> getRecordsByStudentId(Long studentId) {
        return counselingRecordRepository.findByStudentId(studentId);
    }
    
    public List<CounselingRecord> getRecordsByCounselorId(Long counselorId) {
        return counselingRecordRepository.findByCounselorId(counselorId);
    }
    
    public List<CounselingRecord> getRecordsByStatus(CounselingRecord.Status status) {
        return counselingRecordRepository.findByStatus(status);
    }
    
    public CounselingRecord createRecord(CounselingRecord record) {
        // 验证学生和咨询师是否存在
        User student = userRepository.findById(record.getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        User counselor = userRepository.findById(record.getCounselorId())
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));
        
        // 验证角色
        if (student.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("指定用户不是学生");
        }
        if (counselor.getRole() != User.Role.COUNSELOR && counselor.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("指定用户不是咨询师或管理员");
        }
        
        // 如果没有提供title，从summary或content生成
        if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
            String title = record.getSummary();
            if (title == null || title.trim().isEmpty()) {
                title = record.getContent();
            }
            if (title != null && !title.trim().isEmpty()) {
                // 取前100个字符作为title
                title = title.trim();
                if (title.length() > 100) {
                    title = title.substring(0, 100);
                }
                record.setTitle(title);
            } else {
                // 如果都没有，使用默认值
                record.setTitle("咨询记录");
            }
        }
        
        record.setStudent(student);
        record.setCounselor(counselor);
        
        return counselingRecordRepository.save(record);
    }
    
    public CounselingRecord updateRecord(Long id, CounselingRecord recordDetails) {
        return counselingRecordRepository.findById(id)
                .map(record -> {
                    // 更新title，如果没有提供则从summary或content生成
                    if (recordDetails.getTitle() != null && !recordDetails.getTitle().trim().isEmpty()) {
                        record.setTitle(recordDetails.getTitle());
                    } else if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
                        // 如果原记录也没有title，则生成一个
                        String title = recordDetails.getSummary();
                        if (title == null || title.trim().isEmpty()) {
                            title = recordDetails.getContent();
                        }
                        if (title != null && !title.trim().isEmpty()) {
                            title = title.trim();
                            if (title.length() > 100) {
                                title = title.substring(0, 100);
                            }
                            record.setTitle(title);
                        } else {
                            record.setTitle("咨询记录");
                        }
                    }
                    
                    record.setSummary(recordDetails.getSummary());
                    record.setContent(recordDetails.getContent());
                    record.setStatus(recordDetails.getStatus());
                    record.setAppointmentTime(recordDetails.getAppointmentTime());
                    
                    // 如果提供了新的学生ID，更新学生
                    if (recordDetails.getStudentId() != null && !recordDetails.getStudentId().equals(record.getStudent().getId())) {
                        User newStudent = userRepository.findById(recordDetails.getStudentId())
                                .orElseThrow(() -> new RuntimeException("学生不存在"));
                        if (newStudent.getRole() != User.Role.STUDENT) {
                            throw new RuntimeException("指定用户不是学生");
                        }
                        record.setStudent(newStudent);
                    }
                    
                    // 如果提供了新的咨询师ID，更新咨询师
                    if (recordDetails.getCounselorId() != null && !recordDetails.getCounselorId().equals(record.getCounselor().getId())) {
                        User newCounselor = userRepository.findById(recordDetails.getCounselorId())
                                .orElseThrow(() -> new RuntimeException("咨询师不存在"));
                        if (newCounselor.getRole() != User.Role.COUNSELOR && newCounselor.getRole() != User.Role.ADMIN) {
                            throw new RuntimeException("指定用户不是咨询师或管理员");
                        }
                        record.setCounselor(newCounselor);
                    }
                    
                    return counselingRecordRepository.save(record);
                })
                .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
    }
    
    public void deleteRecord(Long id) {
        counselingRecordRepository.deleteById(id);
    }
    
    // 权限检查方法：检查用户是否是学生本人
    public boolean isStudentOwner(Long studentId, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        return user.getRole() == User.Role.STUDENT && user.getId().equals(studentId);
    }
    
    // 权限检查方法：检查用户是否是咨询师本人
    public boolean isCounselorOwner(Long counselorId, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        return (user.getRole() == User.Role.COUNSELOR || user.getRole() == User.Role.ADMIN) && user.getId().equals(counselorId);
    }
    
    // 权限检查方法：检查用户是否是记录的所有者（学生或咨询师）
    public boolean isRecordOwner(Long recordId, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        
        // 管理员可以访问所有记录
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        
        // 检查记录是否存在
        Optional<CounselingRecord> recordOptional = counselingRecordRepository.findById(recordId);
        if (recordOptional.isEmpty()) {
            return false;
        }
        
        CounselingRecord record = recordOptional.get();
        
        // 咨询员只能访问自己负责的记录
        if (user.getRole() == User.Role.COUNSELOR && record.getCounselor().getId().equals(user.getId())) {
            return true;
        }
        
        // 学生只能访问自己的记录
        if (user.getRole() == User.Role.STUDENT && record.getStudent().getId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }
}