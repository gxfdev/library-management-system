package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PublisherRequest {

    private Long id;

    @NotBlank(message = "出版社名称不能为空")
    @Size(max = 200, message = "出版社名称不能超过200字")
    private String name;

    @Size(max = 500, message = "地址不能超过500字")
    private String address;

    @Pattern(regexp = "^$|1[3-9]\\d{9}", message = "联系电话格式不正确")
    private String contactPhone;

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不超过100个字符")
    private String contactEmail;

    @Size(max = 200, message = "官网地址不能超过200字")
    @Pattern(regexp = "^$|^https?://[\\w\\-.]+(:\\d+)?(/.*)?$", message = "官网地址格式不正确")
    private String website;

    @Size(max = 500, message = "简介不能超过500字")
    private String description;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
