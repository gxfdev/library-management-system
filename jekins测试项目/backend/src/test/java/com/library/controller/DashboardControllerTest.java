package com.library.controller;

import com.library.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("仪表盘接口测试")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String readerToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateToken(1L, "admin", "ADMIN");
        readerToken = jwtTokenProvider.generateToken(3L, "reader", "READER");
    }

    @Nested
    @DisplayName("GET /dashboard/stats - 仪表盘统计")
    class DashboardStats {

        @Test
        @DisplayName("管理员获取仪表盘数据应返回200")
        void adminGetDashboardStatsShouldReturn200() throws Exception {
            mockMvc.perform(get("/dashboard/stats")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("读者获取仪表盘数据应返回200")
        void readerGetDashboardStatsShouldReturn200() throws Exception {
            mockMvc.perform(get("/dashboard/stats")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("未认证用户获取仪表盘数据应返回401")
        void unauthenticatedGetDashboardStatsShouldReturn401() throws Exception {
            mockMvc.perform(get("/dashboard/stats"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
