package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("doctor_review")
public class DoctorReview {
    @TableId
    public Long id;
    public Long patientId;
    public Long doctorId;
    public Long appointmentId;
    public Integer rating;
    public String comment;
    public LocalDateTime createdAt;
}
