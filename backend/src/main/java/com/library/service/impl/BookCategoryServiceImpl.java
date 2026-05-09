package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.CategoryRequest;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.mapper.BookCategoryMapper;
import com.library.mapper.BookMapper;
import com.library.service.BookCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCategoryServiceImpl implements BookCategoryService {

    private final BookCategoryMapper categoryMapper;
    private final BookMapper bookMapper;

    @Override
    public List<BookCategory> getList() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<BookCategory>().orderByAsc(BookCategory::getSortOrder)
        );
    }

    @Override
    public BookCategory getById(Long id) {
        BookCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return category;
    }

    @Override
    @Transactional
    public BookCategory create(CategoryRequest request) {
        BookCategory category = new BookCategory();
        BeanUtils.copyProperties(request, category);

        if (request.getParentId() != null && request.getParentId() > 0) {
            BookCategory parent = categoryMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setParentId(0L);
            category.setLevel(1);
        }

        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public BookCategory update(CategoryRequest request) {
        if (request.getId() == null) {
            throw new RuntimeException("分类ID不能为空");
        }

        BookCategory category = categoryMapper.selectById(request.getId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        if (request.getName() != null) category.setName(request.getName());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) category.setStatus(request.getStatus());

        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BookCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<BookCategory>().eq(BookCategory::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        Long bookCount = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, id)
        );
        if (bookCount > 0) {
            throw new RuntimeException("该分类下存在图书，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
