package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$", message = "用户名包含非法字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码6-50个字符")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名不超过20个字符")
    private String realName;

    @Size(max = 20, message = "手机号格式不正确")
    @Pattern(regexp = "^$|1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    @Size(max = 100, message = "邮箱长度不超过100个字符")
    @Email(message = "邮箱格式不正确")
    private String email;
}
