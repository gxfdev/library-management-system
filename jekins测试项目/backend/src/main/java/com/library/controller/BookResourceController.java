package com.library.controller;

import com.library.common.Result;
import com.library.entity.BookResource;
import com.library.mapper.BookResourceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Tag(name = "图书资源", description = "图书附件/资源管理接口")
@RestController
@RequestMapping("/book-resources")
@RequiredArgsConstructor
public class BookResourceController {

    private final BookResourceMapper bookResourceMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    private static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".zip", ".rar", ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    private static final long MAX_RESOURCE_SIZE = 50 * 1024 * 1024;

    @Operation(summary = "获取图书资源列表")
    @GetMapping("/book/{bookId}")
    public Result<java.util.List<BookResource>> getByBookId(@PathVariable Long bookId) {
        return Result.success(bookResourceMapper.selectList(
                new LambdaQueryWrapper<BookResource>()
                        .eq(BookResource::getBookId, bookId)
                        .orderByAsc(BookResource::getSortOrder)
        ));
    }

    @Operation(summary = "上传图书资源")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BookResource> upload(@RequestParam Long bookId,
                                       @RequestParam String resourceType,
                                       @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        if (file.getSize() > MAX_RESOURCE_SIZE) {
            return Result.error(400, "文件大小不能超过50MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }

        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            log.warn("文件名包含非法字符: originalName={}", originalName);
            return Result.error(400, "文件名包含非法字符");
        }

        String ext = "";
        int lastDotIndex = originalName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            ext = originalName.substring(lastDotIndex).toLowerCase();
        }
        if (ext.isEmpty() || !ALLOWED_RESOURCE_EXTENSIONS.contains(ext)) {
            return Result.error(400, "不支持的文件类型");
        }

        Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        if (!Files.exists(baseDir)) Files.createDirectories(baseDir);

        String fileName = UUID.randomUUID().toString() + ext;
        Path filePath = baseDir.resolve(fileName).toAbsolutePath().normalize();
        if (!filePath.startsWith(baseDir)) {
            log.error("路径遍历攻击检测: filePath={}", filePath);
            return Result.error(400, "非法文件路径");
        }

        file.transferTo(filePath.toFile());

        BookResource resource = new BookResource();
        resource.setBookId(bookId);
        resource.setFileName(originalName);
        resource.setFilePath(fileName);
        resource.setFileType(ext.replace(".", "").toUpperCase());
        resource.setFileSize(file.getSize());
        resource.setResourceType(resourceType);
        resource.setSortOrder(0);
        bookResourceMapper.insert(resource);

        log.info("上传图书资源: bookId={}, resourceType={}, file={}", bookId, resourceType, originalName);
        return Result.success(resource);
    }

    @Operation(summary = "下载图书资源")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        BookResource resource = bookResourceMapper.selectById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(resource.getFilePath()).toAbsolutePath().normalize();

        if (!filePath.startsWith(baseDir)) {
            log.error("路径遍历攻击检测: filePath={}, baseDir={}", filePath, baseDir);
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource fileResource = new UrlResource(filePath.toUri());
        String encodedFileName = URLEncoder.encode(resource.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header("X-Content-Type-Options", "nosniff")
                .body(fileResource);
    }

    @Operation(summary = "删除图书资源")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> delete(@PathVariable Long id) throws IOException {
        BookResource resource = bookResourceMapper.selectById(id);
        if (resource == null) return Result.error(404, "资源不存在");

        Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(resource.getFilePath()).toAbsolutePath().normalize();

        if (!filePath.startsWith(baseDir)) {
            log.error("路径遍历攻击检测: filePath={}", filePath);
            return Result.error(400, "非法文件路径");
        }

        Files.deleteIfExists(filePath);
        bookResourceMapper.deleteById(id);
        log.info("删除图书资源: id={}, file={}", id, resource.getFileName());
        return Result.success();
    }
}
