package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission_log")
public class PermissionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private String operatorName;

    private Long targetUserId;

    private String targetUserName;

    private String action;

    private String oldRole;

    private String newRole;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
