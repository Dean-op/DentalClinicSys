package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.AppointmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("appointment")
public class Appointment {
    @TableId
    public Long id;
    public Long patientId;
    public Long doctorId;
    public Long scheduleId;
    public LocalDate visitDate;
    public LocalTime timeSlot;
    public String symptoms;
    public String demand;
    public BigDecimal feeAmount;
    public Boolean feeRefunded;
    public AppointmentStatus status;
    public String statusReason;
    public LocalDateTime createdAt;
}
