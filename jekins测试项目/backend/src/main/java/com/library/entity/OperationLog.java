package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String action;

    private String description;

    private Long userId;

    private String username;

    private String ip;

    private String userAgent;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String errorMsg;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
