package com.library.service;

import com.library.common.PageResult;
import com.library.dto.ChangePasswordRequest;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.dto.UpdateProfileRequest;
import com.library.dto.UserRequest;
import com.library.entity.User;

import java.util.Map;

public interface UserService {

    LoginResponse login(LoginRequest request);

    User register(RegisterRequest request);

    PageResult<User> getPage(int page, int size, String keyword, String role, Integer status);

    User getById(Long id);

    User create(UserRequest request);

    User update(UserRequest request);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    String resetPassword(Long id);

    void adminChangePassword(Long id, String newPassword);

    Map<String, String> sendResetCode(String phone);

    void resetPasswordByPhone(String phone, String code, String newPassword);

    User getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    Map<String, Object> getMyStats(Long userId);

    String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file);
}
