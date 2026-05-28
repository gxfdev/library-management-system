package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("borrow_code")
public class BorrowCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private Long userId;

    private Long bookId;

    private String status;

    private LocalDateTime expireTime;

    private LocalDateTime usedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String userName;
}
