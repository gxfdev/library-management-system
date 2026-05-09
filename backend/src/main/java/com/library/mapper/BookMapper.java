package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    IPage<Book> selectPageWithCategory(Page<Book> page,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("status") Integer status);
}
