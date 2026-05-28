package com.library.controller;

import com.library.common.Result;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookCategoryMapper;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "统计报表", description = "统计数据接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookCategoryMapper categoryMapper;

    @Operation(summary = "获取统计数据")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookMapper.selectCount(null));
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalBorrows", borrowRecordMapper.selectCount(null));
        stats.put("activeBorrows", borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, "BORROWING")
        ));
        return Result.success(stats);
    }

    @Operation(summary = "获取借阅趋势数据")
    @GetMapping("/borrow-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'READER')")
    public Result<List<Map<String, Object>>> getBorrowTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        int dayCount = Math.min(Math.max(days, 1), 90);

        LocalDate startDate = LocalDate.now().minusDays(dayCount - 1);
        LocalDate endDate = LocalDate.now();
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, startDate)
                        .le(BorrowRecord::getBorrowDate, endDate)
        );

        Map<LocalDate, Long> countByDate = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getBorrowDate(),
                        Collectors.counting()));

        for (int i = dayCount - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String label = date.format(fmt);
            Long count = countByDate.getOrDefault(date, 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("label", label);
            item.put("value", count);
            trend.add(item);
        }
        return Result.success(trend);
    }

    @Operation(summary = "获取分类占比数据")
    @GetMapping("/category-distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'READER')")
    public Result<List<Map<String, Object>>> getCategoryDistribution() {
        List<Book> allBooks = bookMapper.selectList(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1)
        );
        Map<Long, Long> categoryCount = allBooks.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(Book::getCategoryId, Collectors.counting()));

        long total = allBooks.size();
        List<Map<String, Object>> result = new ArrayList<>();
        String[] colors = {"#78716c", "#0d9488", "#b45309", "#be123c", "#7c3aed", "#0369a1", "#a8a29e"};
        int idx = 0;

        List<BookCategory> categories = categoryMapper.selectList(null);
        for (BookCategory cat : categories) {
            Long count = categoryCount.getOrDefault(cat.getId(), 0L);
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", cat.getName());
                item.put("count", count);
                item.put("percentage", total > 0 ? Math.round(count * 100.0 / total) : 0);
                item.put("color", colors[idx % colors.length]);
                result.add(item);
                idx++;
            }
        }
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return Result.success(result);
    }

    @Operation(summary = "获取热门图书TOP10")
    @GetMapping("/hot-books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'READER')")
    public Result<List<Map<String, Object>>> getHotBooks() {
        List<BorrowRecord> records = borrowRecordMapper.selectList(null);
        Map<Long, Long> bookBorrowCount = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        bookBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Book book = bookMapper.selectById(entry.getKey());
                    if (book != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("title", book.getTitle());
                        item.put("borrowCount", entry.getValue());
                        result.add(item);
                    }
                });
        return Result.success(result);
    }

    @Operation(summary = "获取月度借阅趋势")
    @GetMapping("/monthly-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'READER')")
    public Result<List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") Integer months) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        int monthCount = Math.min(Math.max(months, 1), 24);

        YearMonth startMonth = YearMonth.now().minusMonths(monthCount - 1);
        LocalDate startDate = startMonth.atDay(1);
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, startDate)
        );

        Map<String, Long> borrowByMonth = allRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getBorrowDate().format(monthFmt),
                        Collectors.counting()));

        Map<String, Long> returnByMonth = allRecords.stream()
                .filter(r -> "RETURNED".equals(r.getStatus()) && r.getReturnDate() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getReturnDate().format(monthFmt),
                        Collectors.counting()));

        for (int i = monthCount - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String label = ym.format(monthFmt);
            Map<String, Object> item = new HashMap<>();
            item.put("label", ym.format(DateTimeFormatter.ofPattern("yyyy年M月")));
            item.put("shortLabel", ym.format(DateTimeFormatter.ofPattern("M月")));
            item.put("borrowCount", borrowByMonth.getOrDefault(label, 0L));
            item.put("returnCount", returnByMonth.getOrDefault(label, 0L));
            trend.add(item);
        }
        return Result.success(trend);
    }

    @Operation(summary = "获取活跃读者排行TOP10")
    @GetMapping("/top-readers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<Map<String, Object>>> getTopReaders() {
        List<BorrowRecord> records = borrowRecordMapper.selectList(null);
        Map<Long, Long> userBorrowCount = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUserId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        userBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    User user = userMapper.selectById(entry.getKey());
                    if (user != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("userId", user.getId());
                        item.put("userName", user.getUsername());
                        item.put("realName", user.getRealName());
                        item.put("borrowCount", entry.getValue());
                        item.put("role", user.getRole());
                        result.add(item);
                    }
                });
        return Result.success(result);
    }

    @Operation(summary = "获取借阅状态分布")
    @GetMapping("/borrow-status-distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<Map<String, Object>>> getBorrowStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        Long borrowing = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "BORROWING"));
        Long returned = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "RETURNED"));
        Long overdue = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "OVERDUE"));

        result.add(Map.of("name", "借阅中", "value", borrowing, "color", "#409eff"));
        result.add(Map.of("name", "已归还", "value", returned, "color", "#67c23a"));
        result.add(Map.of("name", "已逾期", "value", overdue, "color", "#f56c6c"));

        return Result.success(result);
    }

    @Operation(summary = "获取出版社借阅统计")
    @GetMapping("/publisher-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<Map<String, Object>>> getPublisherStats() {
        List<BorrowRecord> records = borrowRecordMapper.selectList(null);
        Map<Long, Long> bookBorrowCount = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        Map<String, Long> publisherBorrowCount = new HashMap<>();
        bookBorrowCount.forEach((bookId, count) -> {
            Book book = bookMapper.selectById(bookId);
            if (book != null && book.getPublisher() != null) {
                publisherBorrowCount.merge(book.getPublisher(), count, Long::sum);
            }
        });

        List<Map<String, Object>> result = new ArrayList<>();
        publisherBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("borrowCount", entry.getValue());
                    result.add(item);
                });

        return Result.success(result);
    }

    @Operation(summary = "获取本月每日借阅统计")
    @GetMapping("/daily-borrows-this-month")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<Map<String, Object>>> getDailyBorrowsThisMonth() {
        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = LocalDate.now();
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, monthStart)
                        .le(BorrowRecord::getBorrowDate, monthEnd)
        );

        Map<LocalDate, Long> countByDate = records.stream()
                .collect(Collectors.groupingBy(
                        BorrowRecord::getBorrowDate,
                        Collectors.counting()));

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            if (date.isAfter(LocalDate.now())) break;

            Long count = countByDate.getOrDefault(date, 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("label", day + "日");
            item.put("value", count);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取增长率统计")
    @GetMapping("/growth-rate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Map<String, Object>> getGrowthRate() {
        Map<String, Object> result = new HashMap<>();

        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        LocalDate thisMonthStart = thisMonth.atDay(1);
        LocalDate thisMonthEnd = thisMonth.atEndOfMonth();
        LocalDate lastMonthStart = lastMonth.atDay(1);
        LocalDate lastMonthEnd = lastMonth.atEndOfMonth();

        Long thisMonthBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, thisMonthStart)
                        .le(BorrowRecord::getBorrowDate, thisMonthEnd));

        Long lastMonthBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getBorrowDate, lastMonthStart)
                        .le(BorrowRecord::getBorrowDate, lastMonthEnd));

        double borrowGrowthRate = 0;
        if (lastMonthBorrows > 0) {
            borrowGrowthRate = Math.round((thisMonthBorrows - lastMonthBorrows) * 100.0 / lastMonthBorrows * 10) / 10.0;
        }

        LocalDateTime thisMonthStartTime = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthStartTime = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEndTime = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        Long thisMonthNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, thisMonthStartTime));

        Long lastMonthNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, lastMonthStartTime)
                        .le(User::getCreateTime, lastMonthEndTime));

        double userGrowthRate = 0;
        if (lastMonthNewUsers > 0) {
            userGrowthRate = Math.round((thisMonthNewUsers - lastMonthNewUsers) * 100.0 / lastMonthNewUsers * 10) / 10.0;
        }

        result.put("borrowGrowthRate", borrowGrowthRate);
        result.put("userGrowthRate", userGrowthRate);
        result.put("thisMonthBorrows", thisMonthBorrows);
        result.put("lastMonthBorrows", lastMonthBorrows);
        result.put("thisMonthNewUsers", thisMonthNewUsers);
        result.put("lastMonthNewUsers", lastMonthNewUsers);

        return Result.success(result);
    }
}
