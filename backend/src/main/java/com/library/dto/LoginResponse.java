package com.library.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private String phone;
        private String email;
        private String avatar;
    }
}
