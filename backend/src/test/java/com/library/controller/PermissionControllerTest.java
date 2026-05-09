package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BorrowRequest;
import com.library.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @DisplayName("未认证用户访问受保护接口应返回401")
    void unauthenticatedAccessShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("读者访问仪表盘应返回200")
    void readerAccessDashboardShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("读者访问用户管理应返回403")
    void readerAccessUserManagementShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理员访问用户管理应返回200")
    void adminAccessUserManagementShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证用户访问图书列表应返回200(公开接口)")
    void unauthenticatedAccessBooksShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证用户访问分类列表应返回200(公开接口)")
    void unauthenticatedAccessCategoriesShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("读者自助借阅应返回200")
    void readerSelfBorrowShouldReturn200() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        mockMvc.perform(post("/api/borrows/self")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("读者访问借阅管理接口应返回403")
    void readerAccessBorrowManagementShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/borrows")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("馆员访问借阅管理接口应返回200")
    void librarianAccessBorrowManagementShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/borrows")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("读者访问我的借阅应返回200")
    void readerAccessMyBorrowsShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/borrows/my")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("读者访问库存管理应返回403")
    void readerAccessInventoryShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/inventory/stats")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("读者访问统计报表应返回403")
    void readerAccessStatisticsShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/statistics")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("读者访问个人中心应返回200")
    void readerAccessProfileShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("无效Token访问应返回401")
    void invalidTokenAccessShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer invalidtoken123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("读者新增图书应返回403")
    void readerCreateBookShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("馆员新增图书应返回400(参数校验)")
    void librarianCreateBookWithInvalidDataShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("读者访问通知公告列表应返回200")
    void readerAccessNoticesShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/notices")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证用户访问通知公告列表应返回200(公开接口)")
    void unauthenticatedAccessNoticesShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
