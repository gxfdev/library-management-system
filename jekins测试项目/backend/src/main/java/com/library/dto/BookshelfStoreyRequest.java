package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookshelfStoreyRequest {

    private Long id;

    @NotNull(message = "书架ID不能为空")
    private Long bookshelfId;

    @NotBlank(message = "层名称不能为空")
    private String name;

    @NotNull(message = "层号不能为空")
    private Integer levelNum;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    private Integer status;
}
