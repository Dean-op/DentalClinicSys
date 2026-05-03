package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("order_item")
public class OrderItem {
    @TableId
    public Long id;
    public Long orderId;
    public Long medicineId;
    public String medicineName;
    public BigDecimal unitPrice;
    public Integer quantity;
}
