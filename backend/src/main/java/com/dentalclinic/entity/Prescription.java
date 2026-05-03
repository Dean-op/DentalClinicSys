package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("prescription")
public class Prescription {
    @TableId
    public Long id;
    public Long recordId;
    public Long patientId;
    public Long doctorId;
    public String note;
    public LocalDateTime createdAt;
}
