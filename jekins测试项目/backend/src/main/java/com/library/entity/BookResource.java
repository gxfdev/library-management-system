package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_resource")
public class BookResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private String resourceType;

    private Integer sortOrder;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
