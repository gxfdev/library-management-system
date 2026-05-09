package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.BookLocationRequest;
import com.library.dto.BookshelfRequest;
import com.library.dto.BookshelfStoreyRequest;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final BookshelfMapper bookshelfMapper;
    private final BookshelfStoreyMapper storeyMapper;
    private final BookLocationMapper locationMapper;
    private final DeptMapper deptMapper;
    private final BookMapper bookMapper;

    @Override
    public PageResult<Bookshelf> getPage(int page, int size, String keyword, Long deptId) {
        Page<Bookshelf> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Bookshelf> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Bookshelf::getName, keyword).or().like(Bookshelf::getCode, keyword);
        }
        if (deptId != null) {
            wrapper.eq(Bookshelf::getDeptId, deptId);
        }
        wrapper.orderByDesc(Bookshelf::getCreateTime);
        IPage<Bookshelf> result = bookshelfMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(b -> {
            if (b.getDeptId() != null) {
                Dept dept = deptMapper.selectById(b.getDeptId());
                if (dept != null) b.setDeptName(dept.getName());
            }
        });
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public Bookshelf getById(Long id) {
        Bookshelf bookshelf = bookshelfMapper.selectById(id);
        if (bookshelf == null) throw new RuntimeException("书架不存在");
        if (bookshelf.getDeptId() != null) {
            Dept dept = deptMapper.selectById(bookshelf.getDeptId());
            if (dept != null) bookshelf.setDeptName(dept.getName());
        }
        return bookshelf;
    }

    @Override
    @Transactional
    public Bookshelf create(BookshelfRequest request) {
        Long count = bookshelfMapper.selectCount(
                new LambdaQueryWrapper<Bookshelf>().eq(Bookshelf::getCode, request.getCode())
        );
        if (count > 0) throw new RuntimeException("书架编号已存在");
        Bookshelf bookshelf = new Bookshelf();
        BeanUtils.copyProperties(request, bookshelf);
        if (bookshelf.getStatus() == null) bookshelf.setStatus(1);
        bookshelfMapper.insert(bookshelf);
        return bookshelf;
    }

    @Override
    @Transactional
    public Bookshelf update(BookshelfRequest request) {
        if (request.getId() == null) throw new RuntimeException("书架ID不能为空");
        Bookshelf bookshelf = bookshelfMapper.selectById(request.getId());
        if (bookshelf == null) throw new RuntimeException("书架不存在");
        if (!bookshelf.getCode().equals(request.getCode())) {
            Long count = bookshelfMapper.selectCount(
                    new LambdaQueryWrapper<Bookshelf>().eq(Bookshelf::getCode, request.getCode())
            );
            if (count > 0) throw new RuntimeException("书架编号已存在");
        }
        BeanUtils.copyProperties(request, bookshelf);
        bookshelfMapper.updateById(bookshelf);
        return bookshelf;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Bookshelf bookshelf = bookshelfMapper.selectById(id);
        if (bookshelf == null) throw new RuntimeException("书架不存在");
        Long storeyCount = storeyMapper.selectCount(
                new LambdaQueryWrapper<BookshelfStorey>().eq(BookshelfStorey::getBookshelfId, id)
        );
        if (storeyCount > 0) throw new RuntimeException("该书架下存在书架层，无法删除");
        bookshelfMapper.deleteById(id);
    }

    @Override
    public List<BookshelfStorey> getStoreysByBookshelfId(Long bookshelfId) {
        List<BookshelfStorey> storeys = storeyMapper.selectList(
                new LambdaQueryWrapper<BookshelfStorey>()
                        .eq(BookshelfStorey::getBookshelfId, bookshelfId)
                        .orderByAsc(BookshelfStorey::getLevelNum)
        );
        storeys.forEach(s -> {
            List<BookLocation> locations = locationMapper.selectList(
                    new LambdaQueryWrapper<BookLocation>()
                            .eq(BookLocation::getStoreyId, s.getId())
                            .orderByAsc(BookLocation::getCode)
            );
            locations.forEach(loc -> {
                if (loc.getBookId() != null) {
                    Book book = bookMapper.selectById(loc.getBookId());
                    if (book != null) {
                        loc.setBookTitle(book.getTitle());
                        loc.setCoverImage(book.getCoverImage());
                    }
                }
            });
            s.setLocations(locations);
        });
        return storeys;
    }

    @Override
    @Transactional
    public BookshelfStorey createStorey(BookshelfStoreyRequest request) {
        BookshelfStorey storey = new BookshelfStorey();
        BeanUtils.copyProperties(request, storey);
        if (storey.getStatus() == null) storey.setStatus(1);
        storeyMapper.insert(storey);
        return storey;
    }

    @Override
    @Transactional
    public BookshelfStorey updateStorey(BookshelfStoreyRequest request) {
        if (request.getId() == null) throw new RuntimeException("书架层ID不能为空");
        BookshelfStorey storey = storeyMapper.selectById(request.getId());
        if (storey == null) throw new RuntimeException("书架层不存在");
        BeanUtils.copyProperties(request, storey);
        storeyMapper.updateById(storey);
        return storey;
    }

    @Override
    @Transactional
    public void deleteStorey(Long id) {
        BookshelfStorey storey = storeyMapper.selectById(id);
        if (storey == null) throw new RuntimeException("书架层不存在");
        Long locationCount = locationMapper.selectCount(
                new LambdaQueryWrapper<BookLocation>().eq(BookLocation::getStoreyId, id)
        );
        if (locationCount > 0) throw new RuntimeException("该层下存在库位，无法删除");
        storeyMapper.deleteById(id);
    }

    @Override
    public PageResult<BookLocation> getLocations(int page, int size, Long storeyId, String status) {
        Page<BookLocation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BookLocation> wrapper = new LambdaQueryWrapper<>();
        if (storeyId != null) wrapper.eq(BookLocation::getStoreyId, storeyId);
        if (StringUtils.hasText(status)) wrapper.eq(BookLocation::getStatus, status);
        wrapper.orderByAsc(BookLocation::getCode);
        IPage<BookLocation> result = locationMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(loc -> {
            if (loc.getBookId() != null) {
                Book book = bookMapper.selectById(loc.getBookId());
                if (book != null) {
                    loc.setBookTitle(book.getTitle());
                    loc.setCoverImage(book.getCoverImage());
                }
            }
        });
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    @Transactional
    public BookLocation createLocation(BookLocationRequest request) {
        Long count = locationMapper.selectCount(
                new LambdaQueryWrapper<BookLocation>().eq(BookLocation::getCode, request.getCode())
        );
        if (count > 0) throw new RuntimeException("库位编号已存在");
        BookLocation location = new BookLocation();
        BeanUtils.copyProperties(request, location);
        if (location.getStatus() == null) location.setStatus("EMPTY");
        locationMapper.insert(location);
        return location;
    }

    @Override
    @Transactional
    public BookLocation updateLocation(BookLocationRequest request) {
        if (request.getId() == null) throw new RuntimeException("库位ID不能为空");
        BookLocation location = locationMapper.selectById(request.getId());
        if (location == null) throw new RuntimeException("库位不存在");
        BeanUtils.copyProperties(request, location);
        locationMapper.updateById(location);
        return location;
    }

    @Override
    @Transactional
    public void deleteLocation(Long id) {
        BookLocation location = locationMapper.selectById(id);
        if (location == null) throw new RuntimeException("库位不存在");
        if ("OCCUPIED".equals(location.getStatus())) throw new RuntimeException("库位占用中，无法删除");
        locationMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignBookToLocation(Long locationId, Long bookId) {
        BookLocation location = locationMapper.selectById(locationId);
        if (location == null) throw new RuntimeException("库位不存在");
        if ("OCCUPIED".equals(location.getStatus())) throw new RuntimeException("库位已被占用");
        Book book = bookMapper.selectById(bookId);
        if (book == null) throw new RuntimeException("图书不存在");
        location.setBookId(bookId);
        location.setStatus("OCCUPIED");
        locationMapper.updateById(location);
    }

    @Override
    @Transactional
    public void clearLocation(Long locationId) {
        BookLocation location = locationMapper.selectById(locationId);
        if (location == null) throw new RuntimeException("库位不存在");
        location.setBookId(null);
        location.setStatus("EMPTY");
        locationMapper.updateById(location);
    }
}
