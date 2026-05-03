package com.dentalclinic.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dentalclinic.domain.AnnouncementStatus;
import java.time.LocalDateTime;

@TableName("announcement")
public class Announcement {
    @TableId
    public Long id;
    public String title;
    public String category;
    public String content;
    public AnnouncementStatus status;
    public LocalDateTime publishAt;
    public LocalDateTime createdAt;
}
