package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("patient_profile")
public class PatientProfile {
    @TableId
    public Long id;
    public Long userId;
    public String name;
    public String gender;
    public String phone;
    public String address;
    public String allergyHistory;
    public BigDecimal balance;
}
