package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.BorrowRequest;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<BorrowRecord> getPage(int page, int size, String keyword, String status, Long userId) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        IPage<BorrowRecord> result = borrowRecordMapper.selectPageWithDetail(pageParam, keyword, status, userId);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public BorrowRecord getById(Long id) {
        BorrowRecord record = borrowRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        return record;
    }

    @Override
    @Transactional
    public BorrowRecord borrow(BorrowRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用，无法借阅");
        }

        Book book = bookMapper.selectById(request.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStatus() == 0) {
            throw new RuntimeException("图书已下架，无法借阅");
        }
        if (book.getStockAvailable() <= 0) {
            throw new RuntimeException("图书库存不足，无法借阅");
        }

        Long activeCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, request.getUserId())
                        .eq(BorrowRecord::getStatus, "BORROWING")
        );
        if (activeCount >= 10) {
            throw new RuntimeException("借阅数量已达上限(10本)");
        }

        Long duplicateBorrow = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, request.getUserId())
                        .eq(BorrowRecord::getBookId, request.getBookId())
                        .eq(BorrowRecord::getStatus, "BORROWING")
        );
        if (duplicateBorrow > 0) {
            throw new RuntimeException("您已借阅该图书且尚未归还，不可重复借阅");
        }

        int borrowDays = request.getBorrowDays() != null ? request.getBorrowDays() : 30;

        BorrowRecord record = new BorrowRecord();
        record.setUserId(request.getUserId());
        record.setBookId(request.getBookId());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(borrowDays));
        record.setRenewCount(0);
        record.setStatus("BORROWING");
        record.setRemark(request.getRemark());
        borrowRecordMapper.insert(record);

        book.setStockAvailable(book.getStockAvailable() - 1);
        bookMapper.updateById(book);

        return record;
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long id) {
        BorrowRecord record = borrowRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("该图书已归还");
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus("RETURNED");
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStockAvailable(book.getStockAvailable() + 1);
            bookMapper.updateById(book);
        }

        return record;
    }

    @Override
    @Transactional
    public BorrowRecord renew(Long id) {
        BorrowRecord record = borrowRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("该图书已归还，无法续借");
        }
        if (record.getRenewCount() >= 2) {
            throw new RuntimeException("续借次数已达上限(2次)");
        }

        record.setDueDate(record.getDueDate().plusDays(30));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordMapper.updateById(record);

        return record;
    }

    @Override
    public PageResult<BorrowRecord> getMyBorrows(int page, int size, Long userId, String status) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        IPage<BorrowRecord> result = borrowRecordMapper.selectPageWithDetail(pageParam, null, status, userId);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }
}
