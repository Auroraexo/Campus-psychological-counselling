package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    // 根据咨询师ID和日期查询预约时间段
    List<AppointmentSlot> findByCounselorIdAndSlotDate(Long counselorId, LocalDate slotDate);

    // 根据咨询师ID查询所有预约时间段
    List<AppointmentSlot> findByCounselorId(Long counselorId);

    // 根据日期查询所有预约时间段
    List<AppointmentSlot> findBySlotDate(LocalDate slotDate);

    // 根据状态查询预约时间段
    List<AppointmentSlot> findByStatus(AppointmentSlot.SlotStatus status);

    // 根据咨询师ID和状态查询预约时间段
    List<AppointmentSlot> findByCounselorIdAndStatus(Long counselorId, AppointmentSlot.SlotStatus status);

    // 根据学生ID查询已预约的时间段
    List<AppointmentSlot> findByStudentId(Long studentId);
}
