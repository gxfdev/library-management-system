package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.OperationLog;
import com.library.mapper.OperationLogMapper;
import com.library.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public PageResult<OperationLog> getPage(int page, int size, String keyword, String module, String startDate, String endDate) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                    .or().like(OperationLog::getDescription, keyword)
                    .or().like(OperationLog::getAction, keyword));
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(OperationLog::getCreateTime, start);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            wrapper.le(OperationLog::getCreateTime, end);
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);
        IPage<OperationLog> result = operationLogMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public List<String> getModules() {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OperationLog::getModule).groupBy(OperationLog::getModule);
        return operationLogMapper.selectList(wrapper).stream()
                .map(OperationLog::getModule)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Object getSummary() {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        Long total = operationLogMapper.selectCount(wrapper);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<OperationLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(OperationLog::getCreateTime, todayStart);
        Long todayCount = operationLogMapper.selectCount(todayWrapper);

        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LambdaQueryWrapper<OperationLog> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(OperationLog::getCreateTime, weekStart);
        Long weekCount = operationLogMapper.selectCount(weekWrapper);

        LambdaQueryWrapper<OperationLog> errorWrapper = new LambdaQueryWrapper<>();
        errorWrapper.eq(OperationLog::getStatus, 0);
        Long errorCount = operationLogMapper.selectCount(errorWrapper);

        return java.util.Map.of(
                "total", total,
                "todayCount", todayCount,
                "weekCount", weekCount,
                "errorCount", errorCount
        );
    }

    @Override
    @Transactional
    public void cleanup(int days) {
        LocalDateTime cutoff = LocalDate.now().minusDays(days).atStartOfDay();
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreateTime, cutoff);
        int deleted = operationLogMapper.delete(wrapper);
        log.info("清理{}天前的操作日志: 删除{}条", days, deleted);
    }

    @Override
    public void save(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }
}
