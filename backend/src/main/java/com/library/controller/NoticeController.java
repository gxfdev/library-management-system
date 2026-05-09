package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.NoticeRequest;
import com.library.entity.Notice;
import com.library.service.NoticeService;
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
@RequestMapping("/notices")
@RequiredArgsConstructor
@Tag(name = "通知公告", description = "通知公告CRUD接口")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "分页查询通知公告列表")
    @GetMapping
    public Result<PageResult<Notice>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.success(noticeService.getPage(page, size, type, status));
    }

    @Operation(summary = "根据ID查询通知公告")
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    @Operation(summary = "新增通知公告")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Notice> create(@Valid @RequestBody NoticeRequest request,
                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("创建通知: userId={}", userId);
        return Result.success(noticeService.create(request, userId));
    }

    @Operation(summary = "更新通知公告")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Notice> update(@Valid @RequestBody NoticeRequest request) {
        log.info("更新通知: id={}", request.getId());
        return Result.success(noticeService.update(request));
    }

    @Operation(summary = "删除通知公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除通知: id={}", id);
        noticeService.delete(id);
        return Result.success();
    }

    @Operation(summary = "发布通知公告")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Notice> publish(@PathVariable Long id) {
        log.info("发布通知: id={}", id);
        return Result.success(noticeService.publish(id));
    }
}
