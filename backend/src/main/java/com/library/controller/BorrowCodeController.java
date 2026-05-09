package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowCode;
import com.library.entity.BorrowRecord;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.BorrowCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "借阅码", description = "借阅码生成与验证接口")
@RestController
@RequestMapping("/borrow-codes")
@RequiredArgsConstructor
public class BorrowCodeController {

    private final BorrowCodeService borrowCodeService;
    private final BorrowRecordMapper borrowRecordMapper;

    @Operation(summary = "生成借阅码")
    @PostMapping("/generate")
    public Result<BorrowCode> generateCode(@RequestParam Long bookId,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("生成借阅码: userId={}, bookId={}", userId, bookId);
        try {
            return Result.success(borrowCodeService.generateCode(userId, bookId));
        } catch (RuntimeException e) {
            log.warn("生成借阅码失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "使用借阅码借阅")
    @PostMapping("/verify")
    public Result<Map<String, Object>> verifyAndBorrow(@RequestParam String code,
                                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("验证借阅码: userId={}, code={}", userId, code);
        try {
            BorrowCode borrowCode = borrowCodeService.verifyAndBorrow(code, userId);
            BorrowRecord record = borrowRecordMapper.selectOne(
                    new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getUserId, userId)
                            .eq(BorrowRecord::getBookId, borrowCode.getBookId())
                            .eq(BorrowRecord::getStatus, "BORROWING")
                            .orderByDesc(BorrowRecord::getCreateTime)
                            .last("LIMIT 1"));
            Map<String, Object> result = new HashMap<>();
            result.put("borrowCode", borrowCode);
            result.put("borrowRecord", record);
            return Result.success(result);
        } catch (RuntimeException e) {
            log.warn("验证借阅码失败: code={}, error={}", code, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "查询我的借阅码")
    @GetMapping("/my")
    public Result<PageResult<BorrowCode>> myCodes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(borrowCodeService.getMyCodes(page, size, userId));
    }

    @Operation(summary = "查询所有借阅码(管理员)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<BorrowCode>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.success(borrowCodeService.getPage(page, size, status));
    }

    @Operation(summary = "作废借阅码")
    @PutMapping("/{id}/invalidate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> invalidate(@PathVariable Long id) {
        borrowCodeService.invalidate(id);
        return Result.success();
    }
}
