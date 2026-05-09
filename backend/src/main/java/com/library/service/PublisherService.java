package com.library.service;

import com.library.common.PageResult;
import com.library.entity.Publisher;

public interface PublisherService {
    PageResult<Publisher> getPage(int page, int size, String keyword);
    Publisher getById(Long id);
    Publisher create(com.library.dto.PublisherRequest request);
    Publisher update(com.library.dto.PublisherRequest request);
    void delete(Long id);
}
