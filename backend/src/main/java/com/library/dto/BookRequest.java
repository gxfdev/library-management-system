package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookRequest {

    private Long id;

    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20, message = "ISBN长度不超过20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9\\-]+$", message = "ISBN格式不正确")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名长度不超过200个字符")
    private String title;

    @Size(max = 100, message = "作者名长度不超过100个字符")
    @Pattern(regexp = "^[\\p{L}\\s\\-,\\.']*$", message = "作者名包含非法字符")
    private String author;

    @Size(max = 200, message = "出版社名称不超过200个字符")
    @Pattern(regexp = "^[\\p{L}\\s\\-,\\.()0-9]*$", message = "出版社名称包含非法字符")
    private String publisher;

    private LocalDate publishDate;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "99999.99", message = "价格超出范围")
    private BigDecimal price;

    @Min(value = 1, message = "页数必须大于0")
    @Max(value = 99999, message = "页数超出范围")
    private Integer pages;

    @Size(max = 500, message = "封面URL长度不超过500个字符")
    @Pattern(regexp = "^(https?://[\\w\\-.]+(:\\d+)?(/.*)?|/uploads/[^\\s]+)?$", message = "封面URL格式不正确")
    private String coverImage;

    @Size(max = 2000, message = "简介长度不超过2000个字符")
    private String description;

    @Size(max = 100, message = "馆藏位置不超过100个字符")
    @Pattern(regexp = "^[\\p{L}\\d\\-A-Z区层架]+$", message = "馆藏位置包含非法字符")
    private String location;

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 99999, message = "库存数量超出范围")
    private Integer stockTotal;

    private Integer status;
}
