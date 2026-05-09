package com.library.service;

import com.library.common.PageResult;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    PageResult<Book> getPage(int page, int size, String keyword, Long categoryId, Integer status);

    Book getById(Long id);

    Book create(BookRequest request);

    Book update(BookRequest request);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    String uploadBookCover(Long bookId, MultipartFile file);
}
