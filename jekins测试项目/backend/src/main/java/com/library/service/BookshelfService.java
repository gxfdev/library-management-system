package com.library.service;

import com.library.entity.Bookshelf;
import com.library.entity.BookshelfStorey;
import com.library.entity.BookLocation;
import com.library.common.PageResult;

import java.util.List;

public interface BookshelfService {
    PageResult<Bookshelf> getPage(int page, int size, String keyword, Long deptId);
    Bookshelf getById(Long id);
    Bookshelf create(com.library.dto.BookshelfRequest request);
    Bookshelf update(com.library.dto.BookshelfRequest request);
    void delete(Long id);

    List<BookshelfStorey> getStoreysByBookshelfId(Long bookshelfId);
    BookshelfStorey createStorey(com.library.dto.BookshelfStoreyRequest request);
    BookshelfStorey updateStorey(com.library.dto.BookshelfStoreyRequest request);
    void deleteStorey(Long id);

    PageResult<BookLocation> getLocations(int page, int size, Long storeyId, String status);
    BookLocation createLocation(com.library.dto.BookLocationRequest request);
    BookLocation updateLocation(com.library.dto.BookLocationRequest request);
    void deleteLocation(Long id);
    void assignBookToLocation(Long locationId, Long bookId);
    void clearLocation(Long locationId);
}
