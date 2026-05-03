package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("cart_item")
public class CartItem {
    @TableId
    public Long id;
    public Long patientId;
    public Long medicineId;
    public Integer quantity;
}
