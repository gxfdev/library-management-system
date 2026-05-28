package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApprovalActionRequest {

    @NotBlank(message = "审批操作不能为空")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "审批操作只能是APPROVE或REJECT")
    private String action;

    @Pattern(regexp = "^[^<>]*$", message = "审批意见不能包含尖括号")
    private String comment;
}
