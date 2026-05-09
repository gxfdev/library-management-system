package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookLocationRequest {

    private Long id;

    @NotNull(message = "书架层ID不能为空")
    private Long storeyId;

    @NotBlank(message = "库位编号不能为空")
    @Size(max = 50, message = "库位编号不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "库位编号只能包含字母、数字、横线和下划线")
    private String code;

    private Long bookId;

    @Pattern(regexp = "^(EMPTY|OCCUPIED|DISABLED)?$", message = "状态值无效")
    private String status;
}
