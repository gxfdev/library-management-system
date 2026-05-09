package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "图书管理", description = "图书CRUD接口")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "分页查询图书列表")
    @GetMapping
    public Result<PageResult<Book>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(bookService.getPage(page, size, keyword, categoryId, status));
    }

    @Operation(summary = "根据ID查询图书")
    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }

    @Operation(summary = "新增图书")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Book> create(@Valid @RequestBody BookRequest request) {
        log.info("新增图书: title={}, isbn={}", request.getTitle(), request.getIsbn());
        return Result.success(bookService.create(request));
    }

    @Operation(summary = "更新图书")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Book> update(@Valid @RequestBody BookRequest request) {
        log.info("更新图书: id={}, title={}", request.getId(), request.getTitle());
        return Result.success(bookService.update(request));
    }

    @Operation(summary = "删除图书")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除图书: id={}", id);
        bookService.delete(id);
        return Result.success();
    }

    @Operation(summary = "更新图书状态(上架/下架)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新图书状态: id={}, status={}", id, status);
        bookService.updateStatus(id, status);
        return Result.success();
    }
}
