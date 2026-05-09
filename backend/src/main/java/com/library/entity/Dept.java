package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dept")
public class Dept {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String ancestors;

    private String name;

    private Integer sortOrder;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private java.util.List<Dept> children;
}
