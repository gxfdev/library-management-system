package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_flow")
public class StockFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    private Long locationId;

    private String flowType;

    private Integer quantity;

    private Long relatedId;

    private Long operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String operatorName;
}
