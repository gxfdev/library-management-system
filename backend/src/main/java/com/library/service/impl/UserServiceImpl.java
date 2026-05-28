package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.ChangePasswordRequest;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.dto.UpdateProfileRequest;
import com.library.dto.UserRequest;
import com.library.entity.User;
import com.library.entity.BorrowRecord;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        response.setUserInfo(userInfo);

        return response;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("READER");
        user.setStatus(1);
        userMapper.insert(user);

        user.setPassword(null);
        return user;
    }

    @Override
    public PageResult<User> getPage(int page, int size, String keyword, String role, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> result = userMapper.selectPageWithKeyword(pageParam, keyword, role, status);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User create(UserRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "123456"));
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        userMapper.insert(user);

        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User update(UserRequest request) {
        if (request.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        Long activeBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, id).eq(BorrowRecord::getStatus, "BORROWING")
        );
        if (activeBorrows > 0) {
            throw new RuntimeException("该用户有未归还的借阅记录，无法删除");
        }
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String rawPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.updateById(user);
        return rawPassword;
    }

    @Override
    @Transactional
    public void adminChangePassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private String generateRandomPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghjkmnpqrstuvwxyz";
        String digits = "23456789";
        String all = upper + lower + digits;
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        for (int i = 3; i < 8; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        return sb.toString();
    }

    private final Map<String, ResetCodeEntry> resetCodeStore = new java.util.concurrent.ConcurrentHashMap<>();

    private static class ResetCodeEntry {
        final String code;
        final Long userId;
        final long timestamp;
        ResetCodeEntry(String code, Long userId, long timestamp) {
            this.code = code;
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }

    @Override
    public Map<String, String> sendResetCode(String phone) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        long now = System.currentTimeMillis();
        resetCodeStore.put(phone, new ResetCodeEntry(code, user.getId(), now));

        cleanupExpiredResetCodes();

        Map<String, String> result = new HashMap<>();
        result.put("code", code);
        result.put("message", "验证码已发送");
        return result;
    }

    @Override
    @Transactional
    public void resetPasswordByPhone(String phone, String code, String newPassword) {
        ResetCodeEntry entry = resetCodeStore.get(phone);
        if (entry == null) {
            throw new RuntimeException("请先获取验证码");
        }
        if ((System.currentTimeMillis() - entry.timestamp) > 300_000) {
            resetCodeStore.remove(phone);
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        resetCodeStore.remove(phone);

        User user = userMapper.selectById(entry.userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private void cleanupExpiredResetCodes() {
        long now = System.currentTimeMillis();
        if (resetCodeStore.size() > 5000) {
            resetCodeStore.entrySet().removeIf(e -> (now - e.getValue().timestamp) > 300_000);
        }
    }

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (request.getUsername().trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
            );
            if (count > 0) {
                throw new RuntimeException("用户名已存在，请更换");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getMyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        Long totalBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId)
        );
        Long activeBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "BORROWING")
        );
        Long overdueBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, userId).eq(BorrowRecord::getStatus, "OVERDUE")
        );
        stats.put("totalBorrows", totalBorrows);
        stats.put("activeBorrows", activeBorrows);
        stats.put("overdueBorrows", overdueBorrows);
        return stats;
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的头像文件");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new RuntimeException("头像图片大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new RuntimeException("文件名包含非法字符");
        }
        int lastDotIndex = originalName.lastIndexOf(".");
        if (lastDotIndex <= 0) {
            throw new RuntimeException("文件扩展名无效");
        }
        String ext = originalName.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        try {
            String datePath = LocalDate.now().toString();
            String fileName = "avatar_" + userId + "_" + UUID.randomUUID() + ext;
            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path dirPath = baseDir.resolve("avatars").resolve(datePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName).toAbsolutePath().normalize();
            if (!filePath.startsWith(baseDir)) {
                throw new RuntimeException("非法文件路径");
            }
            file.transferTo(filePath.toFile());
            String relativePath = "/uploads/avatars/" + datePath + "/" + fileName;
            user.setAvatar(relativePath);
            userMapper.updateById(user);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败，请重试");
        }
    }
}
