package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_config")
public class AiConfig {
    @TableId
    public Long id;
    public String providerName;
    public String baseUrl;
    public String apiKey;
    public String model;
    public Double temperature;
    public Integer maxTokens;
    public Boolean enabled;
    public String remark;
    public LocalDateTime updatedAt;
}
