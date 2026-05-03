package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("medication_reminder")
public class MedicationReminder {
    @TableId
    public Long id;
    public Long patientId;
    public Long medicineId;
    public String medicineName;
    public LocalDate startDate;
    public Integer totalQuantity;
    public Integer dailyDose;
    public LocalDate expectedRunOutDate;
    public Boolean warned;
}
