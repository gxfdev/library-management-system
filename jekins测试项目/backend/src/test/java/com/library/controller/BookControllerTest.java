package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookRequest;
import com.library.dto.BorrowRequest;
import com.library.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("图书管理接口测试")
class BookControllerTest {

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

    @Nested
    @DisplayName("GET /books - 图书列表查询")
    class GetBooks {

        @Test
        @DisplayName("未认证用户可访问图书列表")
        void unauthenticatedCanAccessBooks() throws Exception {
            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").exists());
        }

        @Test
        @DisplayName("带关键词搜索图书")
        void searchBooksWithKeyword() throws Exception {
            mockMvc.perform(get("/books")
                            .param("keyword", "Java")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("带分类筛选查询图书")
        void searchBooksWithCategory() throws Exception {
            mockMvc.perform(get("/books")
                            .param("categoryId", "1")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("分页参数验证 - 默认分页")
        void defaultPaginationWorks() throws Exception {
            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").exists())
                    .andExpect(jsonPath("$.data.size").exists());
        }
    }

    @Nested
    @DisplayName("GET /books/{id} - 图书详情")
    class GetBookById {

        @Test
        @DisplayName("获取存在的图书详情")
        void getExistingBookById() throws Exception {
            mockMvc.perform(get("/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").exists())
                    .andExpect(jsonPath("$.data.isbn").exists());
        }

        @Test
        @DisplayName("获取不存在的图书应返回错误")
        void getNonExistingBookShouldReturnError() throws Exception {
            mockMvc.perform(get("/books/99999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("POST /books - 新增图书")
    class CreateBook {

        @Test
        @DisplayName("管理员新增图书应成功")
        void adminCreateBookShouldSucceed() throws Exception {
            BookRequest request = new BookRequest();
            request.setIsbn("9787111999999");
            request.setTitle("测试图书");
            request.setAuthor("测试作者");
            request.setPublisher("测试出版社");
            request.setCategoryId(1L);
            request.setStockTotal(5);

            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("测试图书"));
        }

        @Test
        @DisplayName("馆员新增图书应成功")
        void librarianCreateBookShouldSucceed() throws Exception {
            BookRequest request = new BookRequest();
            request.setIsbn("9787111888888");
            request.setTitle("馆员测试图书");
            request.setAuthor("测试作者");
            request.setPublisher("测试出版社");
            request.setCategoryId(1L);
            request.setStockTotal(3);

            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + librarianToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("读者新增图书应返回403")
        void readerCreateBookShouldReturn403() throws Exception {
            BookRequest request = new BookRequest();
            request.setIsbn("9787111777777");
            request.setTitle("读者测试图书");
            request.setAuthor("测试作者");
            request.setPublisher("测试出版社");
            request.setCategoryId(1L);
            request.setStockTotal(5);

            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + readerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填字段应返回400")
        void createBookWithMissingFieldsShouldReturn400() throws Exception {
            mockMvc.perform(post("/books")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("借阅相关接口测试")
    class BorrowTests {

        @Test
        @DisplayName("读者自助借阅应成功")
        void readerSelfBorrowShouldSucceed() throws Exception {
            BorrowRequest request = new BorrowRequest();
            request.setBookId(2L);

            mockMvc.perform(post("/borrows/self")
                            .header("Authorization", "Bearer " + readerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("读者查看我的借阅应成功")
        void readerViewMyBorrowsShouldSucceed() throws Exception {
            mockMvc.perform(get("/borrows/my")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("馆员查看所有借阅记录应成功")
        void librarianViewAllBorrowsShouldSucceed() throws Exception {
            mockMvc.perform(get("/borrows")
                            .header("Authorization", "Bearer " + librarianToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
