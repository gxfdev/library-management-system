package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long deptId;

    private Long templateId;

    private Long applicantId;

    private String status;

    private String currentNodeId;

    private LocalDate deadline;

    private String remark;

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
    private String applicantName;

    @TableField(exist = false)
    private String templateName;

    @TableField(exist = false)
    private java.util.List<PurchaseItem> items;
}
