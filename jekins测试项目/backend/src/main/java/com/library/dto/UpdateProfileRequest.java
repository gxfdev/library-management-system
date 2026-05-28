package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Size(max = 20, message = "真实姓名不超过20个字符")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "真实姓名包含非法字符")
    private String realName;

    @Size(max = 20, message = "手机号格式不正确")
    @Pattern(regexp = "^$|1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    @Size(max = 100, message = "邮箱长度不超过100个字符")
    @Email(message = "邮箱格式不正确")
    private String email;
}
