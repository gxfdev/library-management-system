package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.entity.BorrowCode;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowCodeMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowCodeService;
import com.library.service.BorrowRecordService;
import com.library.dto.BorrowRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowCodeServiceImpl implements BorrowCodeService {

    private final BorrowCodeMapper borrowCodeMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordService borrowRecordService;

    private static final int CODE_EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public BorrowCode generateCode(Long userId, Long bookId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getStatus() != null && user.getStatus() != 1) throw new RuntimeException("用户已被禁用");

        Book book = bookMapper.selectById(bookId);
        if (book == null) throw new RuntimeException("图书不存在");
        if (book.getStatus() == 0) throw new RuntimeException("图书已下架");
        if (book.getStockAvailable() <= 0) throw new RuntimeException("图书库存不足");

        long activeCount = borrowCodeMapper.selectCount(
                new LambdaQueryWrapper<BorrowCode>()
                        .eq(BorrowCode::getUserId, userId)
                        .eq(BorrowCode::getStatus, "VALID")
                        .gt(BorrowCode::getExpireTime, LocalDateTime.now()));
        if (activeCount >= 5) throw new RuntimeException("您有过多未使用的借阅码，请先使用或等待过期");

        String code;
        int attempts = 0;
        do {
            code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            attempts++;
            if (attempts > 10) {
                code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                break;
            }
        } while (borrowCodeMapper.selectCount(
                new LambdaQueryWrapper<BorrowCode>().eq(BorrowCode::getCode, code)) > 0);

        BorrowCode borrowCode = new BorrowCode();
        borrowCode.setCode(code);
        borrowCode.setUserId(userId);
        borrowCode.setBookId(bookId);
        borrowCode.setStatus("VALID");
        borrowCode.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        borrowCodeMapper.insert(borrowCode);

        borrowCode.setBookTitle(book.getTitle());
        borrowCode.setUserName(user.getRealName());
        log.info("借阅码生成成功: code={}, userId={}, bookId={}", code, userId, bookId);
        return borrowCode;
    }

    @Override
    @Transactional
    public BorrowCode verifyAndBorrow(String code, Long userId) {
        if (code == null || code.trim().isEmpty()) throw new RuntimeException("借阅码不能为空");

        BorrowCode borrowCode = borrowCodeMapper.selectOne(
                new LambdaQueryWrapper<BorrowCode>().eq(BorrowCode::getCode, code.trim()));
        if (borrowCode == null) throw new RuntimeException("借阅码不存在");

        if ("EXPIRED".equals(borrowCode.getStatus()) || borrowCode.getExpireTime().isBefore(LocalDateTime.now())) {
            borrowCode.setStatus("EXPIRED");
            borrowCodeMapper.updateById(borrowCode);
            throw new RuntimeException("借阅码已过期，请重新生成");
        }
        if ("USED".equals(borrowCode.getStatus())) throw new RuntimeException("借阅码已被使用，不可重复使用");
        if ("INVALID".equals(borrowCode.getStatus())) throw new RuntimeException("借阅码已作废");
        if (!borrowCode.getUserId().equals(userId)) throw new RuntimeException("借阅码与当前用户不匹配");

        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setUserId(userId);
        borrowRequest.setBookId(borrowCode.getBookId());
        borrowRequest.setRemark("借阅码[" + code + "]自动借阅");
        borrowRecordService.borrow(borrowRequest);

        borrowCode.setStatus("USED");
        borrowCode.setUsedTime(LocalDateTime.now());
        borrowCodeMapper.updateById(borrowCode);

        Book book = bookMapper.selectById(borrowCode.getBookId());
        if (book != null) borrowCode.setBookTitle(book.getTitle());
        log.info("借阅码验证成功: code={}, userId={}, bookId={}", code, userId, borrowCode.getBookId());
        return borrowCode;
    }

    @Override
    public PageResult<BorrowCode> getMyCodes(int page, int size, Long userId) {
        Page<BorrowCode> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowCode> wrapper = new LambdaQueryWrapper<BorrowCode>()
                .eq(BorrowCode::getUserId, userId)
                .orderByDesc(BorrowCode::getCreateTime);
        IPage<BorrowCode> result = borrowCodeMapper.selectPage(pageParam, wrapper);
        enrichCodes(result);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public PageResult<BorrowCode> getPage(int page, int size, String status) {
        Page<BorrowCode> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowCode> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(BorrowCode::getStatus, status);
        wrapper.orderByDesc(BorrowCode::getCreateTime);
        IPage<BorrowCode> result = borrowCodeMapper.selectPage(pageParam, wrapper);
        enrichCodes(result);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public void invalidate(Long id) {
        BorrowCode borrowCode = borrowCodeMapper.selectById(id);
        if (borrowCode == null) throw new RuntimeException("借阅码不存在");
        if ("USED".equals(borrowCode.getStatus())) throw new RuntimeException("已使用的借阅码不可作废");
        borrowCode.setStatus("INVALID");
        borrowCodeMapper.updateById(borrowCode);
        log.info("借阅码作废: id={}, code={}", id, borrowCode.getCode());
    }

    private void enrichCodes(IPage<BorrowCode> result) {
        for (BorrowCode bc : result.getRecords()) {
            if (bc.getBookTitle() == null) {
                Book book = bookMapper.selectById(bc.getBookId());
                if (book != null) bc.setBookTitle(book.getTitle());
            }
            if (bc.getUserName() == null) {
                User user = userMapper.selectById(bc.getUserId());
                if (user != null) bc.setUserName(user.getRealName());
            }
            if ("VALID".equals(bc.getStatus()) && bc.getExpireTime() != null && bc.getExpireTime().isBefore(LocalDateTime.now())) {
                bc.setStatus("EXPIRED");
                borrowCodeMapper.updateById(bc);
            }
        }
    }
}
