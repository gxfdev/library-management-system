package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.PermissionLog;
import com.library.service.PermissionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "权限管理", description = "权限配置与变更记录接口")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionLogService permissionLogService;

    @Operation(summary = "获取角色权限矩阵")
    @GetMapping("/matrix")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getPermissionMatrix() {
        List<Map<String, Object>> matrix = new ArrayList<>();

        String[][] modules = {
                {"dashboard", "首页看板"},
                {"book_catalog", "图书借阅"},
                {"book_manage", "图书管理"},
                {"category_manage", "分类管理"},
                {"borrow_manage", "借阅管理"},
                {"self_borrow", "自助借阅"},
                {"self_return", "自助归还"},
                {"renew", "续借"},
                {"user_manage", "用户管理"},
                {"inventory", "库存管理"},
                {"statistics", "统计报表"},
                {"publisher", "出版社管理"},
                {"bookshelf", "书架管理"},
                {"purchase", "采购管理"},
                {"notice", "通知公告"},
                {"borrow_code", "借阅码"},
                {"profile", "个人中心"},
        };

        Map<String, Set<String>> rolePerms = new HashMap<>();
        rolePerms.put("ADMIN", new HashSet<>(Arrays.asList(
                "dashboard", "book_catalog", "book_manage", "category_manage",
                "borrow_manage", "self_borrow", "self_return", "renew",
                "user_manage", "inventory", "statistics", "publisher",
                "bookshelf", "purchase", "notice", "borrow_code", "profile")));
        rolePerms.put("LIBRARIAN", new HashSet<>(Arrays.asList(
                "dashboard", "book_catalog", "book_manage", "category_manage",
                "borrow_manage", "self_borrow", "self_return", "renew",
                "inventory", "statistics", "publisher",
                "bookshelf", "purchase", "notice", "borrow_code", "profile")));
        rolePerms.put("READER", new HashSet<>(Arrays.asList(
                "dashboard", "book_catalog", "self_borrow", "self_return",
                "renew", "notice", "borrow_code", "profile")));

        for (String[] mod : modules) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", mod[0]);
            item.put("name", mod[1]);
            item.put("ADMIN", rolePerms.get("ADMIN").contains(mod[0]));
            item.put("LIBRARIAN", rolePerms.get("LIBRARIAN").contains(mod[0]));
            item.put("READER", rolePerms.get("READER").contains(mod[0]));
            matrix.add(item);
        }

        return Result.success(matrix);
    }

    @Operation(summary = "查询权限变更记录")
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<PermissionLog>> getPermissionLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(permissionLogService.getPage(page, size, keyword));
    }
}
