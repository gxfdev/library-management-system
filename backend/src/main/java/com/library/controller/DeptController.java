package com.library.controller;

import com.library.common.Result;
import com.library.entity.Dept;
import com.library.mapper.DeptMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理", description = "部门列表接口")
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptMapper deptMapper;

    @Operation(summary = "获取部门列表")
    @GetMapping
    public Result<List<Dept>> getList() {
        List<Dept> list = deptMapper.selectList(
                new LambdaQueryWrapper<Dept>().eq(Dept::getStatus, 1).orderByAsc(Dept::getSortOrder)
        );
        return Result.success(list);
    }
}
