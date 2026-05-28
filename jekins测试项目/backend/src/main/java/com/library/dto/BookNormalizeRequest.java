package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookNormalizeRequest {
    private Long id;

    @Size(max = 200, message = "书名长度不超过200个字符")
    private String title;

    @Size(max = 100, message = "作者名长度不超过100个字符")
    private String author;

    @Size(max = 20, message = "ISBN长度不超过20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9\\-]*$", message = "ISBN格式不正确")
    private String isbn;

    @Size(max = 200, message = "出版社名称不超过200个字符")
    private String publisher;
}
