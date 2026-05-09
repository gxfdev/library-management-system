package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.PublisherRequest;
import com.library.entity.Publisher;
import com.library.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出版社管理", description = "出版社CRUD接口")
@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @Operation(summary = "分页查询出版社列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<Publisher>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(publisherService.getPage(page, size, keyword));
    }

    @Operation(summary = "根据ID查询出版社")
    @GetMapping("/{id}")
    public Result<Publisher> getById(@PathVariable Long id) {
        return Result.success(publisherService.getById(id));
    }

    @Operation(summary = "新增出版社")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Publisher> create(@Valid @RequestBody PublisherRequest request) {
        return Result.success(publisherService.create(request));
    }

    @Operation(summary = "更新出版社")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Publisher> update(@Valid @RequestBody PublisherRequest request) {
        return Result.success(publisherService.update(request));
    }

    @Operation(summary = "删除出版社")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        publisherService.delete(id);
        return Result.success();
    }
}
