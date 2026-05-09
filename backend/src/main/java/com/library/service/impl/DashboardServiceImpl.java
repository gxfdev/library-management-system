package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.DashboardStats;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalBooks(bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1)
        ));

        stats.setTotalUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "READER").eq(User::getStatus, 1)
        ));

        stats.setTodayBorrows(borrowRecordMapper.countTodayBorrows());

        stats.setOverdueCount(borrowRecordMapper.countByStatus("OVERDUE"));

        stats.setTotalBorrows(borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "BORROWING")
        ));

        stats.setAvailableBooks(bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1).gt(Book::getStockAvailable, 0)
        ));

        return stats;
    }
}
