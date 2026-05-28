package com.library.controller;

import com.library.annotation.AuditLog;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BorrowRequest;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
@Tag(name = "借阅管理", description = "借阅/归还/续借接口")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    @Operation(summary = "分页查询借阅记录")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<BorrowRecord>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        log.debug("查询借阅记录: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        return Result.success(borrowRecordService.getPage(page, size, keyword, status, null));
    }

    @Operation(summary = "管理员/馆员办理借阅")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @AuditLog(module = "BORROW", action = "ADMIN_BORROW")
    public Result<BorrowRecord> borrow(@Valid @RequestBody BorrowRequest request) {
        log.info("管理员办理借阅: userId={}, bookId={}", request.getUserId(), request.getBookId());
        if (request.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return Result.success(borrowRecordService.borrow(request));
    }

    @Operation(summary = "读者自助借阅")
    @PostMapping("/self")
    @AuditLog(module = "BORROW", action = "SELF_BORROW")
    public Result<BorrowRecord> selfBorrow(@Valid @RequestBody BorrowRequest request,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("读者自助借阅: userId={}, bookId={}", userId, request.getBookId());
        request.setUserId(userId);
        return Result.success(borrowRecordService.borrow(request));
    }

    @Operation(summary = "管理员/馆员办理归还")
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @AuditLog(module = "BORROW", action = "ADMIN_RETURN")
    public Result<BorrowRecord> returnBook(@PathVariable Long id) {
        log.info("管理员归还图书: recordId={}", id);
        return Result.success(borrowRecordService.returnBook(id));
    }

    @Operation(summary = "读者自助归还")
    @PutMapping("/{id}/self-return")
    @AuditLog(module = "BORROW", action = "SELF_RETURN")
    public Result<BorrowRecord> selfReturnBook(@PathVariable Long id,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("读者自助归还: userId={}, recordId={}", userId, id);
        BorrowRecord record = borrowRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "借阅记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            log.warn("越权归还: userId={}, recordUserId={}, recordId={}", userId, record.getUserId(), id);
            return Result.error(403, "只能归还自己的借阅记录");
        }
        if ("RETURNED".equals(record.getStatus())) {
            return Result.error(400, "该图书已归还");
        }
        return Result.success(borrowRecordService.returnBook(id));
    }

    @Operation(summary = "续借")
    @PutMapping("/{id}/renew")
    @AuditLog(module = "BORROW", action = "RENEW")
    public Result<BorrowRecord> renew(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("续借请求: userId={}, recordId={}", userId, id);
        BorrowRecord record = borrowRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "借阅记录不存在");
        }
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        if (!"ADMIN".equals(role) && !"LIBRARIAN".equals(role) && !record.getUserId().equals(userId)) {
            log.warn("越权续借: userId={}, recordUserId={}, recordId={}", userId, record.getUserId(), id);
            return Result.error(403, "只能续借自己的借阅记录");
        }
        return Result.success(borrowRecordService.renew(id));
    }

    @Operation(summary = "我的借阅记录")
    @GetMapping("/my")
    public Result<PageResult<BorrowRecord>> myBorrows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.debug("查询我的借阅: userId={}, status={}", userId, status);
        return Result.success(borrowRecordService.getMyBorrows(page, size, userId, status));
    }
}
