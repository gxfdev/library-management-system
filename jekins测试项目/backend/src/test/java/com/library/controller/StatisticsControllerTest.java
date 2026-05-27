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
@DisplayName("统计报表接口测试")
class StatisticsControllerTest {

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
    @DisplayName("GET /statistics - 基础统计")
    class BasicStatistics {

        @Test
        @DisplayName("管理员访问基础统计应返回200")
        void adminAccessStatisticsShouldReturn200() throws Exception {
            mockMvc.perform(get("/statistics")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalBooks").exists())
                    .andExpect(jsonPath("$.data.totalUsers").exists())
                    .andExpect(jsonPath("$.data.totalBorrows").exists())
                    .andExpect(jsonPath("$.data.activeBorrows").exists());
        }

        @Test
        @DisplayName("馆员访问基础统计应返回200")
        void librarianAccessStatisticsShouldReturn200() throws Exception {
            mockMvc.perform(get("/statistics")
                            .header("Authorization", "Bearer " + librarianToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("读者访问基础统计应返回403")
        void readerAccessStatisticsShouldReturn403() throws Exception {
            mockMvc.perform(get("/statistics")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未认证用户访问基础统计应返回401")
        void unauthenticatedAccessStatisticsShouldReturn401() throws Exception {
            mockMvc.perform(get("/statistics"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /statistics/borrow-trend - 借阅趋势")
    class BorrowTrend {

        @Test
        @DisplayName("管理员获取借阅趋势应返回7天数据")
        void adminGetBorrowTrendShouldReturn7Days() throws Exception {
            mockMvc.perform(get("/statistics/borrow-trend")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(7))
                    .andExpect(jsonPath("$.data[0].label").exists())
                    .andExpect(jsonPath("$.data[0].value").exists());
        }

        @Test
        @DisplayName("读者可访问借阅趋势")
        void readerGetBorrowTrendShouldReturn200() throws Exception {
            mockMvc.perform(get("/statistics/borrow-trend")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /statistics/category-distribution - 分类占比")
    class CategoryDistribution {

        @Test
        @DisplayName("获取分类占比应返回数组")
        void getCategoryDistributionShouldReturnArray() throws Exception {
            mockMvc.perform(get("/statistics/category-distribution")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /statistics/hot-books - 热门图书")
    class HotBooks {

        @Test
        @DisplayName("获取热门图书应返回数组")
        void getHotBooksShouldReturnArray() throws Exception {
            mockMvc.perform(get("/statistics/hot-books")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /statistics/monthly-trend - 月度趋势")
    class MonthlyTrend {

        @Test
        @DisplayName("管理员获取月度趋势应返回12个月数据")
        void adminGetMonthlyTrendShouldReturn12Months() throws Exception {
            mockMvc.perform(get("/statistics/monthly-trend?months=12")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(12))
                    .andExpect(jsonPath("$.data[0].borrowCount").exists())
                    .andExpect(jsonPath("$.data[0].returnCount").exists());
        }

        @Test
        @DisplayName("读者可访问月度趋势")
        void readerCanAccessMonthlyTrend() throws Exception {
            mockMvc.perform(get("/statistics/monthly-trend")
                            .header("Authorization", "Bearer " + readerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /statistics/top-readers - 活跃读者")
    class TopReaders {

        @Test
        @DisplayName("管理员获取活跃读者应返回数组")
        void adminGetTopReadersShouldReturnArray() throws Exception {
            mockMvc.perform(get("/statistics/top-readers")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /statistics/borrow-status-distribution - 借阅状态分布")
    class BorrowStatusDistribution {

        @Test
        @DisplayName("获取借阅状态分布应返回3种状态")
        void getBorrowStatusDistributionShouldReturn3Statuses() throws Exception {
            mockMvc.perform(get("/statistics/borrow-status-distribution")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    @Nested
    @DisplayName("GET /statistics/publisher-stats - 出版社统计")
    class PublisherStats {

        @Test
        @DisplayName("获取出版社统计应返回数组")
        void getPublisherStatsShouldReturnArray() throws Exception {
            mockMvc.perform(get("/statistics/publisher-stats")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /statistics/daily-borrows-this-month - 本月每日借阅")
    class DailyBorrowsThisMonth {

        @Test
        @DisplayName("获取本月每日借阅应返回数组")
        void getDailyBorrowsThisMonthShouldReturnArray() throws Exception {
            mockMvc.perform(get("/statistics/daily-borrows-this-month")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /statistics/growth-rate - 增长率统计")
    class GrowthRate {

        @Test
        @DisplayName("获取增长率统计应返回完整数据")
        void getGrowthRateShouldReturnCompleteData() throws Exception {
            mockMvc.perform(get("/statistics/growth-rate")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.borrowGrowthRate").exists())
                    .andExpect(jsonPath("$.data.userGrowthRate").exists())
                    .andExpect(jsonPath("$.data.thisMonthBorrows").exists())
                    .andExpect(jsonPath("$.data.lastMonthBorrows").exists())
                    .andExpect(jsonPath("$.data.thisMonthNewUsers").exists())
                    .andExpect(jsonPath("$.data.lastMonthNewUsers").exists());
        }
    }
}
