package com.example.counselling_backend.service;

import com.example.counselling_backend.model.User;
import com.example.counselling_backend.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 密码最小长度
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    // 密码最大长度
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // 检查最近使用过的密码数量
    private static final int PASSWORD_HISTORY_COUNT = 5;
    
    // 密码复杂度正则表达式
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{" + 
            MIN_PASSWORD_LENGTH + "," + MAX_PASSWORD_LENGTH + "}$"
    );
    
    /**
     * 验证密码是否符合策略
     * @param password 待验证的密码
     * @return 验证结果，包含错误信息
     */
    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        // 检查密码长度
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            result.addError("密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        } else if (password.length() > MAX_PASSWORD_LENGTH) {
            result.addError("密码长度不能超过" + MAX_PASSWORD_LENGTH + "位");
        }
        
        // 检查密码复杂度
        if (password != null && !PASSWORD_PATTERN.matcher(password).matches()) {
            result.addError("密码必须包含大小写字母、数字和特殊字符(@#$%^&+=!_)");
        }
        
        return result;
    }
    
    /**
     * 验证新密码是否与历史密码重复
     * @param user 用户
     * @param newPassword 新密码
     * @return 验证结果，包含错误信息
     */
    public PasswordValidationResult validatePasswordHistory(User user, String newPassword) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        // 获取用户最近的密码历史
        List<com.example.counselling_backend.model.PasswordHistory> passwordHistory = 
                passwordHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        
        // 检查新密码是否与历史密码重复
        String newHashedPassword = passwordEncoder.encode(newPassword);
        for (com.example.counselling_backend.model.PasswordHistory history : passwordHistory) {
            if (passwordEncoder.matches(newPassword, history.getHashedPassword()) || 
                newHashedPassword.equals(history.getHashedPassword())) {
                result.addError("不能重复使用最近" + PASSWORD_HISTORY_COUNT + "次使用过的密码");
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 保存密码到历史记录
     * @param user 用户
     * @param hashedPassword 加密后的密码
     */
    public void savePasswordHistory(User user, String hashedPassword) {
        com.example.counselling_backend.model.PasswordHistory passwordHistory = 
                new com.example.counselling_backend.model.PasswordHistory();
        passwordHistory.setUser(user);
        passwordHistory.setHashedPassword(hashedPassword);
        
        passwordHistoryRepository.save(passwordHistory);
        
        // 清理过多的历史记录，只保留最近10个
        List<com.example.counselling_backend.model.PasswordHistory> allHistory = 
                passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        if (allHistory.size() > 10) {
            for (int i = 10; i < allHistory.size(); i++) {
                passwordHistoryRepository.delete(allHistory.get(i));
            }
        }
    }
    
    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private boolean valid = true;
        private String errorMessage = "";
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void addError(String message) {
            this.valid = false;
            if (!this.errorMessage.isEmpty()) {
                this.errorMessage += "; ";
            }
            this.errorMessage += message;
        }
    }
}