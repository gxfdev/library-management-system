package com.library.service;

import com.library.common.PageResult;
import com.library.entity.BorrowCode;

public interface BorrowCodeService {
    BorrowCode generateCode(Long userId, Long bookId);
    BorrowCode verifyAndBorrow(String code, Long userId);
    PageResult<BorrowCode> getMyCodes(int page, int size, Long userId);
    PageResult<BorrowCode> getPage(int page, int size, String status);
    void invalidate(Long id);
}
