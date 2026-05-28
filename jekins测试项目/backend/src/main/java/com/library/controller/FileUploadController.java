package com.library.controller;

import com.library.common.Result;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "图书封面图片上传接口")
public class FileUploadController {

    private final BookService bookService;

    @Operation(summary = "上传图书封面图片")
    @PostMapping("/book-cover/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<String> uploadBookCover(@PathVariable Long bookId,
                                         @RequestParam("file") MultipartFile file) {
        String relativePath = bookService.uploadBookCover(bookId, file);
        log.info("上传图书封面: bookId={}, path={}", bookId, relativePath);
        return Result.success(relativePath);
    }
}
