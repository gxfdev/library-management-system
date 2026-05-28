package com.library.service;

import com.library.common.PageResult;
import com.library.entity.OperationLog;

import java.util.List;

public interface OperationLogService {

    PageResult<OperationLog> getPage(int page, int size, String keyword, String module, String startDate, String endDate);

    List<String> getModules();

    Object getSummary();

    void cleanup(int days);

    void save(OperationLog operationLog);
}
