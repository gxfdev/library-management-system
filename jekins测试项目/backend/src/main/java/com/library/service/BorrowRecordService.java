package com.library.service;

import com.library.common.PageResult;
import com.library.dto.BorrowRequest;
import com.library.entity.BorrowRecord;

public interface BorrowRecordService {

    PageResult<BorrowRecord> getPage(int page, int size, String keyword, String status, Long userId);

    BorrowRecord getById(Long id);

    BorrowRecord borrow(BorrowRequest request);

    BorrowRecord returnBook(Long id);

    BorrowRecord renew(Long id);

    PageResult<BorrowRecord> getMyBorrows(int page, int size, Long userId, String status);
}
