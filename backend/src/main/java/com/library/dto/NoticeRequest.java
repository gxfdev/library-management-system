package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class NoticeRequest {

    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    @Pattern(regexp = "^[^<>]*$", message = "标题不能包含尖括号")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 50000, message = "内容不能超过50000字")
    private String content;

    @Pattern(regexp = "^(NOTICE|NEWS|ANNOUNCEMENT)?$", message = "通知类型无效")
    private String type;

    @Pattern(regexp = "^(DRAFT|PUBLISHED)?$", message = "状态值无效")
    private String status;
}
