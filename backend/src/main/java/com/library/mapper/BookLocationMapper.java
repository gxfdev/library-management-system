package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookLocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookLocationMapper extends BaseMapper<BookLocation> {
}
