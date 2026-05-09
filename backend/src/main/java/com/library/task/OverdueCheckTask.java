package com.library.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.library.entity.BorrowRecord;
import com.library.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueCheckTask {

    private final BorrowRecordMapper borrowRecordMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkOverdueRecords() {
        log.info("开始检查逾期借阅记录...");

        Long count = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, "BORROWING")
                        .lt(BorrowRecord::getDueDate, LocalDate.now())
        );

        if (count > 0) {
            borrowRecordMapper.update(
                    null,
                    new LambdaUpdateWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getStatus, "BORROWING")
                            .lt(BorrowRecord::getDueDate, LocalDate.now())
                            .set(BorrowRecord::getStatus, "OVERDUE")
            );
            log.info("已将 {} 条借阅记录标记为逾期", count);
        } else {
            log.info("暂无逾期记录");
        }
    }
}
