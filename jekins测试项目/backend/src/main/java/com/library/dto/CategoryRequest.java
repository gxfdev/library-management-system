package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不超过100个字符")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-()]*$", message = "分类名称包含非法字符")
    private String name;

    @Min(value = 0, message = "父分类ID无效")
    private Long parentId;

    @Min(value = 0, message = "排序值无效")
    @Max(value = 99999, message = "排序值超出范围")
    private Integer sortOrder;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
