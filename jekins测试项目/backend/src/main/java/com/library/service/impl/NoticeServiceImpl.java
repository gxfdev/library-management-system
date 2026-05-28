package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.NoticeRequest;
import com.library.entity.Notice;
import com.library.entity.User;
import com.library.mapper.NoticeMapper;
import com.library.mapper.UserMapper;
import com.library.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<Notice> getPage(int page, int size, String type, String status) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) wrapper.eq(Notice::getType, type);
        if (StringUtils.hasText(status)) wrapper.eq(Notice::getStatus, status);
        wrapper.orderByDesc(Notice::getCreateTime);
        IPage<Notice> result = noticeMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillPublisherName);
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public Notice getById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new RuntimeException("通知公告不存在");
        fillPublisherName(notice);
        return notice;
    }

    @Override
    @Transactional
    public Notice create(NoticeRequest request, Long publisherId) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        notice.setPublisherId(publisherId);
        if (notice.getType() == null) notice.setType("NOTICE");
        if (notice.getStatus() == null) notice.setStatus("DRAFT");
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    @Transactional
    public Notice update(NoticeRequest request) {
        if (request.getId() == null) throw new RuntimeException("通知ID不能为空");
        Notice notice = noticeMapper.selectById(request.getId());
        if (notice == null) throw new RuntimeException("通知公告不存在");
        BeanUtils.copyProperties(request, notice);
        noticeMapper.updateById(notice);
        return notice;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new RuntimeException("通知公告不存在");
        noticeMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Notice publish(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new RuntimeException("通知公告不存在");
        if ("PUBLISHED".equals(notice.getStatus())) throw new RuntimeException("已发布，请勿重复操作");
        notice.setStatus("PUBLISHED");
        notice.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return notice;
    }

    private void fillPublisherName(Notice notice) {
        if (notice.getPublisherId() != null) {
            User user = userMapper.selectById(notice.getPublisherId());
            if (user != null) notice.setPublisherName(user.getRealName());
        }
    }
}
