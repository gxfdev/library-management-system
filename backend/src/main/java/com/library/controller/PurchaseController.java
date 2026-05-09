package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.ApprovalActionRequest;
import com.library.dto.PurchaseOrderRequest;
import com.library.entity.ApprovalRecord;
import com.library.entity.PurchaseOrder;
import com.library.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "采购管理", description = "图书采购/审批接口")
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "分页查询采购单列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<PurchaseOrder>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long deptId) {
        return Result.success(purchaseService.getPage(page, size, status, deptId));
    }

    @Operation(summary = "根据ID查询采购单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        return Result.success(purchaseService.getById(id));
    }

    @Operation(summary = "创建采购单")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PurchaseOrder> create(@Valid @RequestBody PurchaseOrderRequest request,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(purchaseService.create(request, userId));
    }

    @Operation(summary = "更新采购单")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PurchaseOrder> update(@Valid @RequestBody PurchaseOrderRequest request) {
        return Result.success(purchaseService.update(request));
    }

    @Operation(summary = "删除采购单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.delete(id);
        return Result.success();
    }

    @Operation(summary = "提交采购单审批")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PurchaseOrder> submit(@PathVariable Long id) {
        return Result.success(purchaseService.submit(id));
    }

    @Operation(summary = "审批采购单")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<PurchaseOrder> approve(@PathVariable Long id,
                                         @Valid @RequestBody ApprovalActionRequest request,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(purchaseService.approve(id, request, userId));
    }

    @Operation(summary = "快速入库")
    @PutMapping("/{id}/instock")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> quickInstock(@PathVariable Long id) {
        purchaseService.quickInstock(id);
        return Result.success();
    }

    @Operation(summary = "获取审批记录")
    @GetMapping("/{id}/approvals")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<List<ApprovalRecord>> getApprovalRecords(@PathVariable Long id) {
        return Result.success(purchaseService.getApprovalRecords(id));
    }
}
