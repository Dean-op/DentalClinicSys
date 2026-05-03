package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalTime;

@TableName("doctor_schedule")
public class DoctorSchedule {
    @TableId
    public Long id;
    public Long doctorId;
    public LocalDate workDate;
    public LocalTime startTime;
    public LocalTime endTime;
    public Integer capacity;
    public Integer bookedCount;
}
