package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fine_record")
public class FineRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long borrowId;

    private Long userId;

    private BigDecimal amount;

    private String reason;

    private String status;

    private LocalDateTime payTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
