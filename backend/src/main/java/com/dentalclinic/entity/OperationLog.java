package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("operation_log")
public class OperationLog {
    @TableId
    public Long id;
    public Long operatorId;
    public String operatorRole;
    public String action;
    public String detail;
    public LocalDateTime createdAt;
}
