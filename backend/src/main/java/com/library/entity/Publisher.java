package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("publisher")
public class Publisher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private String contactPhone;

    private String contactEmail;

    private String website;

    private String description;

    private Integer status;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
