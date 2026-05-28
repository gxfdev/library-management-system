package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_location")
public class BookLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long storeyId;

    private String code;

    private Long bookId;

    private String status;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String coverImage;
}
