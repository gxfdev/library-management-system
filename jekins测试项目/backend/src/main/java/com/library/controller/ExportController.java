package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.PurchaseItem;
import com.library.entity.PurchaseOrder;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.PurchaseItemMapper;
import com.library.mapper.PurchaseOrderMapper;
import com.library.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "数据导出", description = "数据导出接口")
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final UserMapper userMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseItemMapper purchaseItemMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "导出图书Excel")
    @GetMapping("/books/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportBooksExcel(HttpServletResponse response,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Long categoryId) throws IOException {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        List<Book> books = bookMapper.selectList(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("图书列表.xlsx", StandardCharsets.UTF_8));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("图书列表");
            Row header = sheet.createRow(0);
            String[] headers = {"ISBN", "书名", "作者", "出版社", "分类ID", "价格", "总库存", "可借库存", "状态"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(book.getIsbn());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getAuthor() != null ? book.getAuthor() : "");
                row.createCell(3).setCellValue(book.getPublisher() != null ? book.getPublisher() : "");
                row.createCell(4).setCellValue(book.getCategoryId() != null ? book.getCategoryId().toString() : "");
                row.createCell(5).setCellValue(book.getPrice() != null ? book.getPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(book.getStockTotal());
                row.createCell(7).setCellValue(book.getStockAvailable());
                row.createCell(8).setCellValue(book.getStatus() == 1 ? "在馆" : "下架");
            }
            workbook.write(response.getOutputStream());
        }
    }

    @Operation(summary = "导出图书CSV")
    @GetMapping("/books/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportBooksCsv(HttpServletResponse response,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Long categoryId) throws IOException {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        List<Book> books = bookMapper.selectList(wrapper);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("图书列表.csv", StandardCharsets.UTF_8));

        PrintWriter writer = response.getWriter();
        writer.write("\uFEFF");
        writer.println("ISBN,书名,作者,出版社,价格,总库存,可借库存,状态");
        for (Book book : books) {
            writer.println(String.format("%s,%s,%s,%s,%s,%d,%d,%s",
                    escapeCsv(book.getIsbn()),
                    escapeCsv(book.getTitle()),
                    escapeCsv(book.getAuthor()),
                    escapeCsv(book.getPublisher()),
                    book.getPrice() != null ? book.getPrice().toString() : "0",
                    book.getStockTotal(),
                    book.getStockAvailable(),
                    book.getStatus() == 1 ? "在馆" : "下架"));
        }
        writer.flush();
    }

    @Operation(summary = "导出借阅记录Excel")
    @GetMapping("/borrows/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportBorrowsExcel(HttpServletResponse response,
                                    @RequestParam(required = false) String status) throws IOException {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("借阅记录.xlsx", StandardCharsets.UTF_8));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("借阅记录");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "用户ID", "图书ID", "借阅日期", "应还日期", "归还日期", "续借次数", "状态"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < records.size(); i++) {
                BorrowRecord record = records.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getUserId());
                row.createCell(2).setCellValue(record.getBookId());
                row.createCell(3).setCellValue(record.getBorrowDate() != null ? record.getBorrowDate().format(DATE_FMT) : "");
                row.createCell(4).setCellValue(record.getDueDate() != null ? record.getDueDate().format(DATE_FMT) : "");
                row.createCell(5).setCellValue(record.getReturnDate() != null ? record.getReturnDate().format(DATE_FMT) : "");
                row.createCell(6).setCellValue(record.getRenewCount());
                row.createCell(7).setCellValue(record.getStatus());
            }
            workbook.write(response.getOutputStream());
        }
    }

    @Operation(summary = "导出用户Excel")
    @GetMapping("/users/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportUsersExcel(HttpServletResponse response) throws IOException {
        List<User> users = userMapper.selectList(null);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("用户列表.xlsx", StandardCharsets.UTF_8));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "用户名", "真实姓名", "手机号", "邮箱", "角色", "状态"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getRealName() != null ? user.getRealName() : "");
                row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(4).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(5).setCellValue(user.getRole());
                row.createCell(6).setCellValue(user.getStatus() == 1 ? "启用" : "禁用");
            }
            workbook.write(response.getOutputStream());
        }
    }

    @Operation(summary = "导出采购单Excel")
    @GetMapping("/purchases/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportPurchasesExcel(HttpServletResponse response,
                                      @RequestParam(required = false) String status) throws IOException {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("采购单列表.xlsx", StandardCharsets.UTF_8));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("采购单列表");
            Row header = sheet.createRow(0);
            String[] headers = {"采购单号", "部门", "申请人", "状态", "截止日期", "创建时间", "备注"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < orders.size(); i++) {
                PurchaseOrder order = orders.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(order.getOrderNo() != null ? order.getOrderNo() : "");
                row.createCell(1).setCellValue(order.getDeptName() != null ? order.getDeptName() : "");
                row.createCell(2).setCellValue(order.getApplicantName() != null ? order.getApplicantName() : "");
                row.createCell(3).setCellValue(getStatusLabel(order.getStatus()));
                row.createCell(4).setCellValue(order.getDeadline() != null ? order.getDeadline().format(DATE_FMT) : "");
                row.createCell(5).setCellValue(order.getCreateTime() != null ? order.getCreateTime().format(DATE_FMT) : "");
                row.createCell(6).setCellValue(order.getRemark() != null ? order.getRemark() : "");

                LambdaQueryWrapper<PurchaseItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(PurchaseItem::getOrderId, order.getId());
                List<PurchaseItem> items = purchaseItemMapper.selectList(itemWrapper);

                if (!items.isEmpty()) {
                    Sheet itemSheet = workbook.createSheet("采购项目-" + (i + 1));
                    Row itemHeader = itemSheet.createRow(0);
                    String[] itemHeaders = {"书名", "作者", "ISBN", "出版社", "分类ID", "数量", "已入库数量", "单价", "备注"};
                    for (int j = 0; j < itemHeaders.length; j++) {
                        itemHeader.createCell(j).setCellValue(itemHeaders[j]);
                    }

                    for (int j = 0; j < items.size(); j++) {
                        PurchaseItem item = items.get(j);
                        Row itemRow = itemSheet.createRow(j + 1);
                        itemRow.createCell(0).setCellValue(item.getBookTitle() != null ? item.getBookTitle() : "");
                        itemRow.createCell(1).setCellValue(item.getAuthor() != null ? item.getAuthor() : "");
                        itemRow.createCell(2).setCellValue(item.getIsbn() != null ? item.getIsbn() : "");
                        itemRow.createCell(3).setCellValue(item.getPublisher() != null ? item.getPublisher() : "");
                        itemRow.createCell(4).setCellValue(item.getCategoryId() != null ? item.getCategoryId().toString() : "");
                        itemRow.createCell(5).setCellValue(item.getQuantity() != null ? item.getQuantity() : 0);
                        itemRow.createCell(6).setCellValue(item.getInstockQuantity() != null ? item.getInstockQuantity() : 0);
                        itemRow.createCell(7).setCellValue(item.getPrice() != null ? item.getPrice().doubleValue() : 0);
                        itemRow.createCell(8).setCellValue(item.getRemark() != null ? item.getRemark() : "");
                    }
                }
            }

            workbook.write(response.getOutputStream());
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "DRAFT": return "草稿";
            case "PENDING": return "审批中";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            case "COMPLETED": return "已完成";
            default: return status;
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
