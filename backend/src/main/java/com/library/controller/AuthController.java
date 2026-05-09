package com.library.controller;

import com.library.common.Result;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理", description = "登录注册相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "读者注册")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "忘记密码-发送短信验证码")
    @PostMapping("/forgot-password/send-code")
    public Result<Map<String, String>> forgotPasswordSendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        return Result.success(userService.sendResetCode(phone));
    }

    @Operation(summary = "忘记密码-验证码重置密码")
    @PostMapping("/forgot-password/reset")
    public Result<Void> forgotPasswordReset(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error(400, "验证码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "新密码长度不能少于6位");
        }
        userService.resetPasswordByPhone(phone, code, newPassword);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            User user = userService.getById(userId);
            if (user != null) {
                user.setPassword(null);
                return Result.success(user);
            }
        }
        return Result.error(401, "用户信息获取失败");
    }
}
