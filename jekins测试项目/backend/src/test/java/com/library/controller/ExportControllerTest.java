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
@DisplayName("导出接口测试")
class ExportControllerTest {

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
    @DisplayName("GET /export/books/excel - 图书Excel导出")
    class ExportBooksExcel {

        @Test
        @DisplayName("管理员导出图书Excel应返回文件")
        void adminExportBooksExcelShouldReturnFile() throws Exception {
            mockMvc.perform(get("/export/books/excel")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".xlsx")));
        }

        @Test
        @DisplayName("馆员导出图书Excel应返回文件")
        void librarianExportBooksExcelShouldReturnFile() throws Exception {
            mockMvc.perform(get("/export/books/excel")
                            .header("Authorization", "Bearer " + librarianToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("读者导出图书Excel应返回403")
        void readerExportBooksExcelShouldReturn403() throws Exception {
            mockMvc.perform(get("/export/books/excel")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未认证用户导出图书Excel应返回401")
        void unauthenticatedExportBooksExcelShouldReturn401() throws Exception {
            mockMvc.perform(get("/export/books/excel"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /export/borrows/excel - 借阅记录Excel导出")
    class ExportBorrowsExcel {

        @Test
        @DisplayName("管理员导出借阅记录Excel应返回文件")
        void adminExportBorrowsExcelShouldReturnFile() throws Exception {
            mockMvc.perform(get("/export/borrows/excel")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        }

        @Test
        @DisplayName("带状态筛选导出借阅记录")
        void exportBorrowsWithStatusFilter() throws Exception {
            mockMvc.perform(get("/export/borrows/excel")
                            .param("status", "BORROWING")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /export/users/excel - 用户Excel导出")
    class ExportUsersExcel {

        @Test
        @DisplayName("管理员导出用户Excel应返回文件")
        void adminExportUsersExcelShouldReturnFile() throws Exception {
            mockMvc.perform(get("/export/users/excel")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        }
    }

    @Nested
    @DisplayName("GET /export/books/csv - 图书CSV导出")
    class ExportBooksCsv {

        @Test
        @DisplayName("管理员导出图书CSV应返回文件")
        void adminExportBooksCsvShouldReturnFile() throws Exception {
            mockMvc.perform(get("/export/books/csv")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")));
        }
    }
}
