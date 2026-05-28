package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BookLocationRequest;
import com.library.dto.BookshelfRequest;
import com.library.dto.BookshelfStoreyRequest;
import com.library.entity.BookLocation;
import com.library.entity.Bookshelf;
import com.library.entity.BookshelfStorey;
import com.library.service.BookshelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "书架/库位管理", description = "书架、书架层、库位管理接口")
@RestController
@RequestMapping("/bookshelves")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @Operation(summary = "分页查询书架列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<Bookshelf>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId) {
        return Result.success(bookshelfService.getPage(page, size, keyword, deptId));
    }

    @Operation(summary = "根据ID查询书架")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Bookshelf> getById(@PathVariable Long id) {
        return Result.success(bookshelfService.getById(id));
    }

    @Operation(summary = "新增书架")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Bookshelf> create(@Valid @RequestBody BookshelfRequest request) {
        return Result.success(bookshelfService.create(request));
    }

    @Operation(summary = "更新书架")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Bookshelf> update(@Valid @RequestBody BookshelfRequest request) {
        return Result.success(bookshelfService.update(request));
    }

    @Operation(summary = "删除书架")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        bookshelfService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取书架下的层列表")
    @GetMapping("/{bookshelfId}/storeys")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<BookshelfStorey>> getStoreys(@PathVariable Long bookshelfId) {
        return Result.success(bookshelfService.getStoreysByBookshelfId(bookshelfId));
    }

    @Operation(summary = "新增书架层")
    @PostMapping("/storeys")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BookshelfStorey> createStorey(@Valid @RequestBody BookshelfStoreyRequest request) {
        return Result.success(bookshelfService.createStorey(request));
    }

    @Operation(summary = "更新书架层")
    @PutMapping("/storeys")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BookshelfStorey> updateStorey(@Valid @RequestBody BookshelfStoreyRequest request) {
        return Result.success(bookshelfService.updateStorey(request));
    }

    @Operation(summary = "删除书架层")
    @DeleteMapping("/storeys/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Void> deleteStorey(@PathVariable Long id) {
        bookshelfService.deleteStorey(id);
        return Result.success();
    }

    @Operation(summary = "分页查询库位列表")
    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<BookLocation>> getLocations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long storeyId,
            @RequestParam(required = false) String status) {
        return Result.success(bookshelfService.getLocations(page, size, storeyId, status));
    }

    @Operation(summary = "新增库位")
    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BookLocation> createLocation(@Valid @RequestBody BookLocationRequest request) {
        return Result.success(bookshelfService.createLocation(request));
    }

    @Operation(summary = "更新库位")
    @PutMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BookLocation> updateLocation(@Valid @RequestBody BookLocationRequest request) {
        return Result.success(bookshelfService.updateLocation(request));
    }

    @Operation(summary = "删除库位")
    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Void> deleteLocation(@PathVariable Long id) {
        bookshelfService.deleteLocation(id);
        return Result.success();
    }

    @Operation(summary = "分配图书到库位")
    @PutMapping("/locations/{locationId}/assign/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> assignBook(@PathVariable Long locationId, @PathVariable Long bookId) {
        bookshelfService.assignBookToLocation(locationId, bookId);
        return Result.success();
    }

    @Operation(summary = "清空库位")
    @PutMapping("/locations/{locationId}/clear")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> clearLocation(@PathVariable Long locationId) {
        bookshelfService.clearLocation(locationId);
        return Result.success();
    }
}
