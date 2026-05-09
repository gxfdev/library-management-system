package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_normalize_log")
public class BookNormalizeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private String oldTitle;

    private String newTitle;

    private String oldAuthor;

    private String newAuthor;

    private String oldIsbn;

    private String newIsbn;

    private String oldPublisher;

    private String newPublisher;

    private String operatorName;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
