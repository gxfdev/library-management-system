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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("操作日志接口测试")
class OperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String librarianToken;
    private String readerToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateToken(1L, "admin", "ADMIN");
        librarianToken = jwtTokenProvider.generateToken(2L, "librarian", "LIBRARIAN");
        readerToken = jwtTokenProvider.generateToken(3L, "reader", "READER");
    }

    @Nested
    @DisplayName("GET /operation-logs - 分页查询日志")
    class GetOperationLogs {

        @Test
        @DisplayName("管理员查询操作日志应返回200")
        void adminGetOperationLogsShouldReturn200() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").exists());
        }

        @Test
        @DisplayName("馆员查询操作日志应返回403")
        void librarianGetOperationLogsShouldReturn403() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .header("Authorization", "Bearer " + librarianToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("读者查询操作日志应返回403")
        void readerGetOperationLogsShouldReturn403() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未认证用户查询操作日志应返回401")
        void unauthenticatedGetOperationLogsShouldReturn401() throws Exception {
            mockMvc.perform(get("/operation-logs"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("带关键词筛选查询日志")
        void getOperationLogsWithKeyword() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .param("keyword", "admin")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("带模块筛选查询日志")
        void getOperationLogsWithModule() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .param("module", "BOOK")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("带日期范围筛选查询日志")
        void getOperationLogsWithDateRange() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2026-12-31")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("分页参数验证")
        void paginationParametersValidation() throws Exception {
            mockMvc.perform(get("/operation-logs")
                            .param("page", "1")
                            .param("size", "5")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").exists())
                    .andExpect(jsonPath("$.data.size").exists());
        }
    }

    @Nested
    @DisplayName("GET /operation-logs/modules - 获取模块列表")
    class GetModules {

        @Test
        @DisplayName("管理员获取模块列表应返回200")
        void adminGetModulesShouldReturn200() throws Exception {
            mockMvc.perform(get("/operation-logs/modules")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /operation-logs/summary - 获取日志摘要")
    class GetSummary {

        @Test
        @DisplayName("管理员获取日志摘要应返回200")
        void adminGetSummaryShouldReturn200() throws Exception {
            mockMvc.perform(get("/operation-logs/summary")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("DELETE /operation-logs/cleanup - 清理历史日志")
    class CleanupLogs {

        @Test
        @DisplayName("管理员清理历史日志应返回200")
        void adminCleanupLogsShouldReturn200() throws Exception {
            mockMvc.perform(delete("/operation-logs/cleanup")
                            .param("days", "90")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("馆员清理历史日志应返回403")
        void librarianCleanupLogsShouldReturn403() throws Exception {
            mockMvc.perform(delete("/operation-logs/cleanup")
                            .param("days", "90")
                            .header("Authorization", "Bearer " + librarianToken))
                    .andExpect(status().isForbidden());
        }
    }
}
