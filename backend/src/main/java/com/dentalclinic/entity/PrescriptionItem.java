package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("prescription_item")
public class PrescriptionItem {
    @TableId
    public Long id;
    public Long prescriptionId;
    public Long medicineId;
    public String medicineName;
    public String frequency;
    public String dosage;
    public Integer days;
}
