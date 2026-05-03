package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("symptom_rule")
public class SymptomRule {
    @TableId
    public Long id;
    public String keywords;
    public String possibleCause;
    public String recommendedDepartment;
    public String recommendedMedicineIds;
    public String advice;
}
