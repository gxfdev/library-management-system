package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bookshelf_storey")
public class BookshelfStorey {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookshelfId;

    private String name;

    private Integer levelNum;

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
    private java.util.List<BookLocation> locations;
}
