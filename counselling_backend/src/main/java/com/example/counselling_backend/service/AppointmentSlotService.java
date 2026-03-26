package com.example.counselling_backend.service;

import com.example.counselling_backend.model.AppointmentSlot;
import com.example.counselling_backend.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    public AppointmentSlotService(AppointmentSlotRepository appointmentSlotRepository) {
        this.appointmentSlotRepository = appointmentSlotRepository;
    }

    // 获取所有预约时间段
    public List<AppointmentSlot> getAllAppointmentSlots() {
        return appointmentSlotRepository.findAll();
    }

    // 根据ID获取预约时间段
    public Optional<AppointmentSlot> getAppointmentSlotById(Long id) {
        return appointmentSlotRepository.findById(id);
    }

    // 根据咨询师ID和日期获取预约时间段
    public List<AppointmentSlot> getAppointmentSlotsByCounselorAndDate(Long counselorId, LocalDate date) {
        if (counselorId != null && date != null) {
            return appointmentSlotRepository.findByCounselorIdAndSlotDate(counselorId, date);
        } else if (counselorId != null) {
            return appointmentSlotRepository.findByCounselorId(counselorId);
        } else if (date != null) {
            return appointmentSlotRepository.findBySlotDate(date);
        } else {
            return getAllAppointmentSlots();
        }
    }

    // 创建新的预约时间段
    public AppointmentSlot createAppointmentSlot(AppointmentSlot appointmentSlot) {
        // 检查时间段是否重叠
        checkSlotOverlap(appointmentSlot);
        return appointmentSlotRepository.save(appointmentSlot);
    }

    // 更新预约时间段
    public AppointmentSlot updateAppointmentSlot(Long id, AppointmentSlot updatedSlot) {
        AppointmentSlot existingSlot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约时间段不存在"));

        // 如果更新了时间信息，检查是否与其他时间段重叠
        if (!existingSlot.getSlotDate().equals(updatedSlot.getSlotDate()) ||
            !existingSlot.getStartTime().equals(updatedSlot.getStartTime()) ||
            !existingSlot.getEndTime().equals(updatedSlot.getEndTime()) ||
            !existingSlot.getCounselorId().equals(updatedSlot.getCounselorId())) {
            checkSlotOverlap(updatedSlot);
        }

        // 更新字段
        existingSlot.setCounselorId(updatedSlot.getCounselorId());
        existingSlot.setSlotDate(updatedSlot.getSlotDate());
        existingSlot.setStartTime(updatedSlot.getStartTime());
        existingSlot.setEndTime(updatedSlot.getEndTime());
        existingSlot.setStatus(updatedSlot.getStatus());
        existingSlot.setStudentId(updatedSlot.getStudentId());

        return appointmentSlotRepository.save(existingSlot);
    }

    // 删除预约时间段
    public void deleteAppointmentSlot(Long id) {
        AppointmentSlot slot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约时间段不存在"));
        appointmentSlotRepository.delete(slot);
    }

    // 预约时间段（学生预约）
    public AppointmentSlot bookSlot(Long id, Long studentId) {
        AppointmentSlot slot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约时间段不存在"));

        if (slot.getStatus() != AppointmentSlot.SlotStatus.AVAILABLE) {
            throw new RuntimeException("该时间段已被预约");
        }

        slot.setStatus(AppointmentSlot.SlotStatus.BOOKED);
        slot.setStudentId(studentId);
        return appointmentSlotRepository.save(slot);
    }

    // 取消预约
    public AppointmentSlot cancelSlot(Long id) {
        AppointmentSlot slot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约时间段不存在"));

        slot.setStatus(AppointmentSlot.SlotStatus.CANCELLED);
        slot.setStudentId(null);
        return appointmentSlotRepository.save(slot);
    }

    // 检查时间段是否重叠
    private void checkSlotOverlap(AppointmentSlot slot) {
        List<AppointmentSlot> existingSlots = appointmentSlotRepository.findByCounselorIdAndSlotDate(
                slot.getCounselorId(), slot.getSlotDate());

        for (AppointmentSlot existingSlot : existingSlots) {
            // 排除当前正在更新的记录
            if (slot.getId() != null && slot.getId().equals(existingSlot.getId())) {
                continue;
            }

            // 检查时间段重叠
            boolean isOverlap = (slot.getStartTime().isBefore(existingSlot.getEndTime()) &&
                    slot.getEndTime().isAfter(existingSlot.getStartTime()));

            if (isOverlap) {
                throw new RuntimeException("时间段与已存在的预约重叠");
            }
        }
    }
}
