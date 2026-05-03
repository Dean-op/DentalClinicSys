package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("medicine_order")
public class MedicineOrder {
    @TableId
    public Long id;
    public String orderNo;
    public Long patientId;
    public BigDecimal totalAmount;
    public String deliveryMethod;
    public OrderStatus status;
    public LocalDateTime createdAt;
}
