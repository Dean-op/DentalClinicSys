package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("medical_record")
public class MedicalRecord {
    @TableId
    public Long id;
    public Long patientId;
    public Long doctorId;
    public Long appointmentId;
    public String chiefComplaint;
    public String presentIllness;
    public String examination;
    public String diagnosis;
    public String treatmentPlan;
    public String reportImagePath;
    public LocalDateTime createdAt;
}
