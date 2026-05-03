package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.ReviewStatus;
import java.time.LocalDateTime;

@TableName("doctor_qualification")
public class DoctorQualification {
    @TableId
    public Long id;
    public Long doctorId;
    public String certificateType;
    public String filePath;
    public ReviewStatus status;
    public String reviewComment;
    public LocalDateTime submittedAt;
    public LocalDateTime reviewedAt;
}
