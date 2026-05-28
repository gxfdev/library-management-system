INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '系统管理员', '13800138000', 'admin@library.com', 'ADMIN', 1),
('librarian', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '图书管理员', '13800138001', 'librarian@library.com', 'LIBRARIAN', 1),
('reader', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '张三', '13800138002', 'reader@library.com', 'READER', 1);

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

INSERT INTO `borrow_record` (`user_id`, `book_id`, `borrow_date`, `due_date`, `status`) VALUES
(3, 1, '2024-01-15', '2024-02-15', 'BORROWING'),
(3, 2, '2024-01-20', '2024-02-20', 'RETURNED');

INSERT INTO `notice` (`title`, `content`, `type`, `status`, `publisher_id`, `publish_time`) VALUES
('图书馆开放时间调整通知', '自2024年1月1日起，图书馆开放时间调整为周一至周五8:00-22:00。', 'NOTICE', 'PUBLISHED', 1, CURRENT_TIMESTAMP()),
('2024年度读书月活动公告', '图书馆将于4月举办读书月活动，欢迎参加！', 'ANNOUNCEMENT', 'PUBLISHED', 1, CURRENT_TIMESTAMP());

INSERT INTO `publisher` (`name`, `address`, `contact_phone`, `website`) VALUES
('机械工业出版社', '北京市西城区百万庄大街22号', '010-88379833', 'www.cmpbook.com'),
('人民邮电出版社', '北京市丰台区成寿寺路11号', '010-81055552', 'www.ptpress.com.cn');

MERGE INTO `sys_dept` (`id`, `parent_id`, `ancestors`, `name`, `sort_order`, `leader`, `phone`, `email`) KEY(`id`) VALUES
(1, 0, '0', '图书馆总部', 1, '管理员', '13800138000', 'admin@library.com'),
(2, 1, '0,1', '采编部', 1, '张采编', '13800138001', 'caibian@library.com');
