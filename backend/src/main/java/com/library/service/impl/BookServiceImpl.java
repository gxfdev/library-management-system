package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.BorrowRecord;
import com.library.mapper.BookCategoryMapper;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookCategoryMapper categoryMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public PageResult<Book> getPage(int page, int size, String keyword, Long categoryId, Integer status) {
        Page<Book> pageParam = new Page<>(page, size);
        IPage<Book> result = bookMapper.selectPageWithCategory(pageParam, keyword, categoryId, status);
        if (!result.getRecords().isEmpty()) {
            java.util.Set<Long> categoryIds = result.getRecords().stream()
                    .map(Book::getCategoryId)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            if (!categoryIds.isEmpty()) {
                java.util.Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(java.util.stream.Collectors.toMap(BookCategory::getId, BookCategory::getName));
                result.getRecords().forEach(b -> {
                    if (b.getCategoryId() != null) {
                        b.setCategoryName(categoryNameMap.get(b.getCategoryId()));
                    }
                });
            }
        }
        return new PageResult<>(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    public Book getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getCategoryId() != null) {
            BookCategory cat = categoryMapper.selectById(book.getCategoryId());
            if (cat != null) book.setCategoryName(cat.getName());
        }
        return book;
    }

    @Override
    @Transactional
    public Book create(BookRequest request) {
        Book book = new Book();
        BeanUtils.copyProperties(request, book);
        book.setStockAvailable(request.getStockTotal());
        book.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        bookMapper.insert(book);
        return book;
    }

    @Override
    @Transactional
    public Book update(BookRequest request) {
        if (request.getId() == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        Book book = bookMapper.selectById(request.getId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        int stockDiff = request.getStockTotal() - book.getStockTotal();
        BeanUtils.copyProperties(request, book);
        book.setStockAvailable(book.getStockAvailable() + stockDiff);
        if (book.getStockAvailable() < 0) book.setStockAvailable(0);

        bookMapper.updateById(book);
        return book;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        Long activeBorrows = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getBookId, id).eq(BorrowRecord::getStatus, "BORROWING")
        );
        if (activeBorrows > 0) {
            throw new RuntimeException("该图书有未归还的借阅记录，无法删除");
        }
        bookMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        book.setStatus(status);
        bookMapper.updateById(book);
    }

    @Override
    @Transactional
    public String uploadBookCover(Long bookId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("图片大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new RuntimeException("文件名包含非法字符");
        }
        int lastDotIndex = originalName.lastIndexOf(".");
        if (lastDotIndex <= 0) {
            throw new RuntimeException("文件扩展名无效");
        }
        String ext = originalName.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        try {
            String datePath = LocalDate.now().toString();
            String fileName = UUID.randomUUID() + ext;
            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path dirPath = baseDir.resolve(datePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName).toAbsolutePath().normalize();
            if (!filePath.startsWith(baseDir)) {
                throw new RuntimeException("非法文件路径");
            }
            file.transferTo(filePath.toFile());
            String relativePath = "/uploads/" + datePath + "/" + fileName;
            book.setCoverImage(relativePath);
            bookMapper.updateById(book);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败，请重试");
        }
    }
}
