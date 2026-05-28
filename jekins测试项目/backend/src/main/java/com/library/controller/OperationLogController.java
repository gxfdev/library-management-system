package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.OperationLog;
import com.library.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "操作日志", description = "系统操作日志审计接口")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OperationLog>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(operationLogService.getPage(page, size, keyword, module, startDate, endDate));
    }

    @Operation(summary = "获取日志模块列表")
    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getModules() {
        return Result.success(operationLogService.getModules());
    }

    @Operation(summary = "获取日志摘要统计")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> getSummary() {
        return Result.success(operationLogService.getSummary());
    }

    @Operation(summary = "清理历史日志")
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> cleanup(@RequestParam(defaultValue = "90") int days) {
        operationLogService.cleanup(days);
        return Result.success();
    }
}
