MERGE INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) KEY(`username`) VALUES
('admin', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '系统管理员', '13800138000', 'admin@library.com', 'ADMIN', 1),
('librarian', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '图书管理员', '13800138001', 'librarian@library.com', 'LIBRARIAN', 1),
('reader', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '张三', '13800138002', 'reader@library.com', 'READER', 1);

MERGE INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) KEY(`name`, `parent_id`) VALUES
('计算机科学', 0, 1, 1),
('文学', 0, 1, 2),
('自然科学', 0, 1, 3);

MERGE INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) KEY(`name`, `parent_id`) VALUES
('程序设计', 1, 2, 1),
('数据结构', 1, 2, 2),
('算法', 1, 2, 3);

MERGE INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category_id`, `price`, `pages`, `location`, `stock_total`, `stock_available`) KEY(`isbn`) VALUES
('9787111698809', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', '2022-01-01', 4, 149.00, 768, 'A区-3层-01架', 8, 5),
('9787111641247', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '2020-01-01', 6, 128.00, 1292, 'A区-3层-02架', 12, 10),
('9787111681238', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2021-01-01', 4, 139.00, 988, 'A区-3层-03架', 6, 4);

MERGE INTO `notice` (`title`, `content`, `type`, `status`, `publisher_id`) KEY(`title`) VALUES
('系统通知', '欢迎使用图书馆管理系统', 'SYSTEM', 'PUBLISHED', 1),
('借阅规则', '每人最多借阅5本，借期30天', 'SYSTEM', 'PUBLISHED', 1);

MERGE INTO `publisher` (`name`, `contact_phone`, `status`) KEY(`name`) VALUES
('机械工业出版社', '010-88379833', 1),
('清华大学出版社', '010-62770175', 1);

MERGE INTO `sys_dept` (`name`, `parent_id`, `sort_order`, `status`) KEY(`name`) VALUES
('总馆', 0, 1, 1),
('分馆', 0, 2, 1);
