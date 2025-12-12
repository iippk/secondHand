package com.cshp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

