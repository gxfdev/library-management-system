package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.PermissionLog;
import com.library.mapper.PermissionLogMapper;
import com.library.service.PermissionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionLogServiceImpl implements PermissionLogService {

    private final PermissionLogMapper permissionLogMapper;

    @Override
    public void log(Long operatorId, String operatorName, Long targetUserId, String targetUserName,
                    String action, String oldRole, String newRole, String detail) {
        PermissionLog log = new PermissionLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setTargetUserId(targetUserId);
        log.setTargetUserName(targetUserName);
        log.setAction(action);
        log.setOldRole(oldRole);
        log.setNewRole(newRole);
        log.setDetail(detail);
        permissionLogMapper.insert(log);
    }

    @Override
    public PageResult<PermissionLog> getPage(int page, int size, String keyword) {
        Page<PermissionLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PermissionLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(PermissionLog::getOperatorName, keyword)
                    .or().like(PermissionLog::getTargetUserName, keyword)
                    .or().like(PermissionLog::getAction, keyword));
        }
        wrapper.orderByDesc(PermissionLog::getCreateTime);
        IPage<PermissionLog> result = permissionLogMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }
}
