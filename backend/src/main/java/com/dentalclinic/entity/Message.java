package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("message")
public class Message {
    @TableId
    public Long id;
    public Long patientId;
    public Long doctorId;
    public String question;
    public String reply;
    public LocalDateTime createdAt;
    public LocalDateTime repliedAt;
}
