package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("medicine")
public class Medicine {
    @TableId
    public Long id;
    public String name;
    public String effect;
    public String usageInstruction;
    public BigDecimal price;
    public Integer stock;
    public Boolean onShelf;
}
