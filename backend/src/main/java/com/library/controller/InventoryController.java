package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "库存管理", description = "库存查询接口")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Operation(summary = "获取库存统计")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        Long totalBooks = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1)
        );
        Long availableBooks = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1).gt(Book::getStockAvailable, 0)
        );
        Long borrowedBooks = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "BORROWING")
        );
        Long lowStockBooks = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1).le(Book::getStockAvailable, 2)
        );
        stats.put("totalBooks", totalBooks);
        stats.put("availableBooks", availableBooks);
        stats.put("borrowedBooks", borrowedBooks);
        stats.put("lowStockBooks", lowStockBooks);
        return Result.success(stats);
    }

    @Operation(summary = "获取库存列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<Book>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Book> pageParam = new Page<>(page, size);
        IPage<Book> result = bookMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Book>().eq(Book::getStatus, 1)
        );
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size));
    }
}
