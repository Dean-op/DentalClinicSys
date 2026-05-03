package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.AccountStatus;
import com.dentalclinic.domain.Role;
import java.time.LocalDateTime;

@TableName("user_account")
public class UserAccount {
    @TableId
    public Long id;
    public String username;
    public String passwordHash;
    public Role role;
    public AccountStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime lastLoginAt;
}
