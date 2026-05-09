package com.library.service;

import com.library.common.PageResult;
import com.library.dto.NoticeRequest;
import com.library.entity.Notice;

public interface NoticeService {
    PageResult<Notice> getPage(int page, int size, String type, String status);
    Notice getById(Long id);
    Notice create(NoticeRequest request, Long publisherId);
    Notice update(NoticeRequest request);
    void delete(Long id);
    Notice publish(Long id);
}
