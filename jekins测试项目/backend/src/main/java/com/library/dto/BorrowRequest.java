package com.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BorrowRequest {

    private Long userId;

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @Min(value = 1, message = "借阅天数至少1天")
    @Max(value = 90, message = "借阅天数最多90天")
    private Integer borrowDays;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
