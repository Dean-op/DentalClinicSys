package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.ReviewStatus;
import java.math.BigDecimal;

@TableName("doctor_profile")
public class DoctorProfile {
    @TableId
    public Long id;
    public Long userId;
    public String name;
    public String gender;
    public String title;
    public String department;
    public String specialty;
    public String introduction;
    public ReviewStatus reviewStatus;
    public Double rating;
    public BigDecimal consultationFee;
}
