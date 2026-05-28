package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.PublisherRequest;
import com.library.entity.Publisher;
import com.library.mapper.PublisherMapper;
import com.library.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherMapper publisherMapper;

    @Override
    public PageResult<Publisher> getPage(int page, int size, String keyword) {
        Page<Publisher> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Publisher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Publisher::getName, keyword)
                    .or().like(Publisher::getAddress, keyword);
        }
        wrapper.orderByDesc(Publisher::getCreateTime);
        IPage<Publisher> result = publisherMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public Publisher getById(Long id) {
        Publisher publisher = publisherMapper.selectById(id);
        if (publisher == null) {
            throw new RuntimeException("出版社不存在");
        }
        return publisher;
    }

    @Override
    @Transactional
    public Publisher create(PublisherRequest request) {
        Long count = publisherMapper.selectCount(
                new LambdaQueryWrapper<Publisher>().eq(Publisher::getName, request.getName())
        );
        if (count > 0) {
            throw new RuntimeException("出版社名称已存在");
        }
        Publisher publisher = new Publisher();
        BeanUtils.copyProperties(request, publisher);
        if (publisher.getStatus() == null) publisher.setStatus(1);
        publisherMapper.insert(publisher);
        return publisher;
    }

    @Override
    @Transactional
    public Publisher update(PublisherRequest request) {
        if (request.getId() == null) {
            throw new RuntimeException("出版社ID不能为空");
        }
        Publisher publisher = publisherMapper.selectById(request.getId());
        if (publisher == null) {
            throw new RuntimeException("出版社不存在");
        }
        if (!publisher.getName().equals(request.getName())) {
            Long count = publisherMapper.selectCount(
                    new LambdaQueryWrapper<Publisher>().eq(Publisher::getName, request.getName())
            );
            if (count > 0) {
                throw new RuntimeException("出版社名称已存在");
            }
        }
        BeanUtils.copyProperties(request, publisher);
        publisherMapper.updateById(publisher);
        return publisher;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Publisher publisher = publisherMapper.selectById(id);
        if (publisher == null) {
            throw new RuntimeException("出版社不存在");
        }
        publisherMapper.deleteById(id);
    }
}
