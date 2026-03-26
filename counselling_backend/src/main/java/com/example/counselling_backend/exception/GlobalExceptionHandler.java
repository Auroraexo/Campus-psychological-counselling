package com.example.counselling_backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "请求数据格式错误";
        
        // 检查是否包含InvalidFormatException，这通常表示日期时间格式错误
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) e.getCause();
            
            if (ife.getTargetType() != null && ife.getTargetType().equals(java.time.LocalDateTime.class)) {
                message = "日期时间格式错误，请使用格式：yyyy-MM-dd HH:mm:ss (例如: 2023-12-25 14:30:00)";
            } else if (ife.getTargetType() != null && ife.getTargetType().equals(java.time.LocalDate.class)) {
                message = "日期格式错误，请使用格式：yyyy-MM-dd (例如: 2023-12-25)";
            } else if (ife.getTargetType() != null && ife.getTargetType().equals(java.time.LocalTime.class)) {
                message = "时间格式错误，请使用格式：HH:mm:ss (例如: 14:30:00)";
            } else {
                message = "数据格式错误：字段 '" + ife.getPath().get(0).getFieldName() + "' 的值 '" + 
                          ife.getValue() + "' 无法转换为 " + ife.getTargetType().getSimpleName() + " 类型";
            }
        } 
        // 检查是否包含DateTimeParseException
        else if (e.getCause() instanceof DateTimeParseException) {
            DateTimeParseException dtpe = (DateTimeParseException) e.getCause();
            message = "日期时间解析错误：'" + dtpe.getParsedString() + "' 不是有效的日期时间格式，请使用 yyyy-MM-dd HH:mm:ss 格式";
        }
        // 其他错误处理
        else if (e.getMessage() != null) {
            if (e.getMessage().contains("LocalDateTime")) {
                message = "日期时间格式错误，请使用格式：yyyy-MM-dd HH:mm:ss (例如: 2023-12-25 14:30:00)";
            } else if (e.getMessage().contains("LocalDate")) {
                message = "日期格式错误，请使用格式：yyyy-MM-dd (例如: 2023-12-25)";
            } else if (e.getMessage().contains("LocalTime")) {
                message = "时间格式错误，请使用格式：HH:mm:ss (例如: 14:30:00)";
            } else if (e.getMessage().contains("Cannot deserialize value") && e.getMessage().contains("Enum")) {
                message = "状态值错误，请使用有效的状态值：PENDING, IN_PROGRESS, COMPLETED, CANCELLED";
            } else if (e.getMessage().contains("JSON")) {
                message = "JSON格式错误：" + e.getMessage();
            } else if (e.getMessage().contains("String is empty")) {
                message = "必填字段不能为空，请检查所有必填字段是否已正确填写";
            } else if (e.getMessage().contains("Cannot read value")) {
                message = "数据类型不匹配，请检查会话序号、时长等字段是否为有效数字";
            } else {
                message = "数据格式错误：" + e.getMessage();
            }
        }
        
        System.err.println("JSON反序列化错误: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("根本原因: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
        }
        e.printStackTrace();
        return ResponseEntity.badRequest().body(message);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        if (message != null && message.contains("Status")) {
            message = "状态值错误：" + message;
        }
        System.err.println("参数错误: " + message);
        return ResponseEntity.badRequest().body(message);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // 记录错误日志
        System.err.println("未处理的异常: " + e.getClass().getName() + " - " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("服务器内部错误：" + e.getMessage());
    }
}

