package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private Long categoryId;

    private BigDecimal price;

    private Integer pages;

    private String coverImage;

    private String description;

    private String location;

    private Integer stockTotal;

    private Integer stockAvailable;

    private Integer status;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String categoryName;
}
