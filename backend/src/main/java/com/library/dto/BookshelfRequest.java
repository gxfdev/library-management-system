package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookshelfRequest {

    private Long id;

    @NotBlank(message = "书架名称不能为空")
    @Size(max = 100, message = "书架名称不能超过100个字符")
    private String name;

    @NotBlank(message = "书架编号不能为空")
    @Size(max = 50, message = "书架编号不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "书架编号只能包含字母、数字、横线和下划线")
    private String code;

    private Long deptId;

    @Size(max = 200, message = "位置不能超过200个字符")
    private String location;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    @Max(value = 99999, message = "容量超出范围")
    private Integer capacity;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
