INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2b$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '系统管理员', '13800138000', 'admin@library.com', 'ADMIN', 1),
('librarian', '$2b$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '图书管理员', '13800138001', 'librarian@library.com', 'LIBRARIAN', 1),
('reader', '$2b$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '张三', '13800138002', 'reader@library.com', 'READER', 1);

INSERT INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('计算机科学', 0, 1, 1),
('文学', 0, 1, 2),
('自然科学', 0, 1, 3);

INSERT INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('程序设计', 1, 2, 1),
('数据结构', 1, 2, 2),
('算法', 1, 2, 3);

INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category_id`, `price`, `pages`, `location`, `stock_total`, `stock_available`) VALUES
('9787111698809', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', '2022-01-01', 4, 149.00, 768, 'A区-3层-01架', 8, 5),
('9787111641247', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '2020-01-01', 6, 128.00, 1292, 'A区-3层-02架', 12, 10),
('9787111681238', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2021-01-01', 4, 139.00, 988, 'A区-3层-03架', 6, 4);
