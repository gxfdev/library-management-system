package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bookshelf")
public class Bookshelf {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private Long deptId;

    private String location;

    private Integer capacity;

    private Integer status;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private java.util.List<BookshelfStorey> storeys;
}
