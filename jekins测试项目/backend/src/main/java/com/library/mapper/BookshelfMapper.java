package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Bookshelf;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookshelfMapper extends BaseMapper<Bookshelf> {
}
