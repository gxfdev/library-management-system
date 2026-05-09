package com.library.service;

import com.library.dto.LoginResponse;
import com.library.dto.UserRequest;
import com.library.entity.User;
import com.library.common.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void loginSuccess() {
        var request = new com.library.dto.LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = userService.login(request);
        assertNotNull(response.getToken());
        assertNotNull(response.getUserInfo());
        assertEquals("admin", response.getUserInfo().getUsername());
        assertEquals("ADMIN", response.getUserInfo().getRole());
    }

    @Test
    void loginWithWrongPassword() {
        var request = new com.library.dto.LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    void getPage() {
        PageResult<User> result = userService.getPage(1, 10, null, null, null);
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void createUser() {
        UserRequest request = new UserRequest();
        request.setUsername("testuser");
        request.setPassword("test123");
        request.setRealName("测试用户");
        request.setRole("READER");

        User user = userService.create(request);
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("测试用户", user.getRealName());
    }

    @Test
    void updateUser() {
        UserRequest request = new UserRequest();
        request.setId(1L);
        request.setRealName("管理员2号");

        User user = userService.update(request);
        assertNotNull(user);
        assertEquals("管理员2号", user.getRealName());
    }

    @Test
    void resetPassword() {
        assertDoesNotThrow(() -> userService.resetPassword(1L));
    }
}
