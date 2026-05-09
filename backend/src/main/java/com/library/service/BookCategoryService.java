package com.library.service;

import com.library.dto.CategoryRequest;
import com.library.entity.BookCategory;

import java.util.List;

public interface BookCategoryService {

    List<BookCategory> getList();

    BookCategory getById(Long id);

    BookCategory create(CategoryRequest request);

    BookCategory update(CategoryRequest request);

    void delete(Long id);
}
