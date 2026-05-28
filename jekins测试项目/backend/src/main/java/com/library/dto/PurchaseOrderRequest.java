package com.library.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    private Long id;

    private Long deptId;

    private Long templateId;

    private LocalDate deadline;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    @Valid
    @NotNull(message = "采购项目不能为空")
    private List<PurchaseItemRequest> items;

    @Data
    public static class PurchaseItemRequest {

        @NotBlank(message = "图书名称不能为空")
        @Size(max = 200, message = "图书名称不能超过200个字符")
        private String bookTitle;

        @Size(max = 100, message = "作者名不能超过100个字符")
        private String author;

        @Size(max = 20, message = "ISBN不能超过20个字符")
        @Pattern(regexp = "^[a-zA-Z0-9\\-]*$", message = "ISBN格式不正确")
        private String isbn;

        @Size(max = 200, message = "出版社不能超过200个字符")
        private String publisher;

        private Long categoryId;

        @NotNull(message = "采购数量不能为空")
        @Min(value = 1, message = "采购数量必须大于0")
        @Max(value = 9999, message = "采购数量超出范围")
        private Integer quantity;

        @DecimalMin(value = "0.01", message = "价格必须大于0")
        @DecimalMax(value = "99999.99", message = "价格超出范围")
        private BigDecimal price;

        @Size(max = 500, message = "备注不能超过500个字符")
        private String remark;
    }
}
