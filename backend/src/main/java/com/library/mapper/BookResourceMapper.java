package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookResource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookResourceMapper extends BaseMapper<BookResource> {
}
