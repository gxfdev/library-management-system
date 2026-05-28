package com.library.controller;

import com.library.common.Result;
import com.library.dto.ChangePasswordRequest;
import com.library.dto.UpdateProfileRequest;
import com.library.entity.User;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Tag(name = "个人中心", description = "个人信息相关接口")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @Operation(summary = "获取个人信息")
    @GetMapping
    public Result<User> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.getProfile(userId);
        return Result.success(user);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateProfile(userId, request);
        log.info("更新个人信息: userId={}", userId);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request);
        log.info("修改密码成功: userId={}", userId);
        return Result.success();
    }

    @Operation(summary = "获取个人借阅统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getMyStats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> stats = userService.getMyStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "上传个人头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String relativePath = userService.uploadAvatar(userId, file);
        log.info("上传头像: userId={}, path={}", userId, relativePath);
        return Result.success(relativePath);
    }
}
