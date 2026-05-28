package com.library.service;

import com.library.common.PageResult;
import com.library.entity.PermissionLog;

public interface PermissionLogService {
    void log(Long operatorId, String operatorName, Long targetUserId, String targetUserName,
             String action, String oldRole, String newRole, String detail);

    PageResult<PermissionLog> getPage(int page, int size, String keyword);
}
