package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_item")
public class PurchaseItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String bookTitle;

    private String author;

    private String isbn;

    private String publisher;

    private Long categoryId;

    private Integer quantity;

    private Integer instockQuantity;

    private BigDecimal price;

    private String remark;

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
