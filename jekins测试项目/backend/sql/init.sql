-- 图书馆管理系统数据库初始化脚本
-- Database: library_db

CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'READER' COMMENT '角色: ADMIN/LIBRARIAN/READER',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 图书分类表
CREATE TABLE IF NOT EXISTS `book_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID, 0表示顶级分类',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN编号',
    `title` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(200) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(200) DEFAULT NULL COMMENT '出版社',
    `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `pages` INT DEFAULT NULL COMMENT '页数',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `description` TEXT DEFAULT NULL COMMENT '简介',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '馆藏位置',
    `stock_total` INT NOT NULL DEFAULT 1 COMMENT '总库存',
    `stock_available` INT NOT NULL DEFAULT 1 COMMENT '可借库存',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-在馆 0-下架',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_isbn` (`isbn`),
    KEY `idx_title` (`title`),
    KEY `idx_author` (`author`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS `borrow_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '借阅用户ID',
    `book_id` BIGINT NOT NULL COMMENT '图书ID',
    `borrow_date` DATE NOT NULL COMMENT '借阅日期',
    `due_date` DATE NOT NULL COMMENT '应还日期',
    `return_date` DATE DEFAULT NULL COMMENT '实际归还日期',
    `renew_count` TINYINT NOT NULL DEFAULT 0 COMMENT '续借次数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'BORROWING' COMMENT '状态: BORROWING-借阅中 RETURNED-已归还 OVERDUE-已逾期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_status` (`status`),
    KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 罚款记录表
CREATE TABLE IF NOT EXISTS `fine_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `borrow_id` BIGINT NOT NULL COMMENT '借阅记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '罚款金额',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '罚款原因',
    `status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '状态: UNPAID-未缴 PAID-已缴',
    `pay_time` DATETIME DEFAULT NULL COMMENT '缴纳时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_borrow_id` (`borrow_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款记录表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户',
    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长(ms)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 插入初始管理员账号 (密码: 123456, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '系统管理员', '13800138000', 'admin@library.com', 'ADMIN', 1),
('librarian', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '图书管理员', '13800138001', 'librarian@library.com', 'LIBRARIAN', 1),
('reader', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '张三', '13800138002', 'reader@library.com', 'READER', 1),
('gxf', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '郭小峰', '13912345678', 'gxf@example.com', 'READER', 1),
('wangwei', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '王伟', '13766668888', 'wangwei@example.com', 'READER', 1),
('liming', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '李明', '13655557777', 'liming@example.com', 'READER', 1),
('zhaoyue', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '赵月', '13544446666', 'zhaoyue@example.com', 'READER', 1),
('chenxi', '$2a$10$WFJvStr2.OtGuzIOcWb2C.cOgELgzzsQATtI0YO0Gop4IGYhMGCEu', '陈曦', '13333332222', 'chenxi@example.com', 'LIBRARIAN', 1);

-- 插入图书分类
INSERT INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('计算机科学', 0, 1, 1),
('文学', 0, 1, 2),
('自然科学', 0, 1, 3),
('社会科学', 0, 1, 4),
('历史地理', 0, 1, 5);

INSERT INTO `book_category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('程序设计', 1, 2, 1),
('数据结构', 1, 2, 2),
('算法', 1, 2, 3),
('软件工程', 1, 2, 4),
('Web开发', 1, 2, 5),
('数据库', 1, 2, 6),
('中国文学', 2, 2, 1),
('外国文学', 2, 2, 2),
('数学', 3, 2, 1),
('物理学', 3, 2, 2),
('化学', 3, 2, 3),
('生物学', 3, 2, 4);

-- 插入示例图书
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category_id`, `price`, `pages`, `description`, `location`, `stock_total`, `stock_available`) VALUES
('9787111698809', 'Java核心技术 卷I', 'Cay S. Horstmann', '机械工业出版社', '2022-01-01', 6, 149.00, 768, 'Java领域经典著作，全面覆盖Java核心技术', 'A区-3层-01架', 8, 5),
('9787111641247', '算法导论（第3版）', 'Thomas H. Cormen', '机械工业出版社', '2020-01-01', 8, 128.00, 1292, '计算机算法领域的权威教材，涵盖基础算法与高级主题', 'A区-3层-02架', 12, 10),
('9787111681238', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2021-01-01', 6, 139.00, 988, '从程序员角度深入理解计算机系统', 'A区-3层-03架', 6, 4),
('9787115428926', '设计模式：可复用面向对象软件的基础', 'Erich Gamma', '人民邮电出版社', '2019-01-01', 9, 69.00, 416, 'GoF经典设计模式，软件工程师必读', 'A区-3层-04架', 15, 12),
('9787121390332', '代码整洁之道', 'Robert C. Martin', '电子工业出版社', '2020-01-01', 9, 59.00, 388, '编写整洁代码的敏捷实践指南', 'A区-3层-05架', 9, 7),
('9787115523802', 'JavaScript高级程序设计（第4版）', 'Matt Frisbie', '人民邮电出版社', '2022-01-01', 10, 129.00, 978, 'JavaScript圣经级参考书', 'A区-3层-06架', 10, 8),
('9787115508379', 'Python编程从入门到实践（第2版）', 'Eric Matthes', '人民邮电出版社', '2021-01-01', 6, 89.00, 458, 'Python入门首选，项目驱动学习', 'A区-3层-07架', 14, 11),
('9787302517523', '数据结构与算法分析：C语言描述', 'Mark Allen Weiss', '清华大学出版社', '2019-01-01', 7, 79.00, 528, '数据结构经典教材', 'A区-3层-08架', 7, 5),
('9787115585839', 'Vue.js设计与实现', '霍春阳', '人民邮电出版社', '2022-01-01', 10, 99.00, 488, '深入Vue.js源码，理解框架设计思想', 'A区-3层-09架', 11, 9),
('9787111698801', 'Spring Boot实战（第3版）', 'Craig Walls', '机械工业出版社', '2021-01-01', 9, 79.00, 328, 'Spring Boot实战指南', 'A区-3层-10架', 6, 3),
('9787508694770', '活着', '余华', '中信出版社', '2018-05-01', 13, 35.00, 280, '当代文学经典，讲述生命的坚韧与温情', 'B区-2层-01架', 20, 15),
('9787020024759', '红楼梦', '曹雪芹', '人民文学出版社', '1996-07-01', 13, 59.80, 1600, '中国古典四大名著之首', 'B区-2层-02架', 12, 10),
('9787544270878', '百年孤独', '加西亚·马尔克斯', '南海出版公司', '2017-08-01', 14, 49.50, 360, '魔幻现实主义文学的代表作', 'B区-2层-03架', 8, 6),
('9787208061644', '三体', '刘慈欣', '重庆出版社', '2008-01-01', 13, 23.00, 302, '中国科幻巅峰之作，雨果奖获奖作品', 'B区-2层-04架', 18, 14),
('9787532754680', '小王子', '圣埃克苏佩里', '上海译文出版社', '2003-08-01', 14, 22.00, 97, '写给大人的童话，关于爱与责任的寓言', 'B区-2层-05架', 25, 20),
('9787108033883', '围城', '钱钟书', '生活·读书·新知三联书店', '2002-04-01', 13, 28.00, 368, '中国现代讽刺小说的杰作', 'B区-2层-06架', 10, 8),
('9787544253994', '解忧杂货店', '东野圭吾', '南海出版公司', '2014-05-01', 14, 39.50, 291, '温暖治愈系推理小说', 'B区-2层-07架', 16, 13),
('9787506379568', '平凡的世界', '路遥', '北京十月文艺出版社', '2012-03-01', 13, 88.00, 1250, '茅盾文学奖获奖作品', 'B区-2层-08架', 9, 7),
('9787111699001', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '2021-06-01', 11, 49.90, 320, 'MySQL快速入门教程', 'C区-1层-01架', 10, 8),
('9787115489346', 'Redis设计与实现', '黄健宏', '机械工业出版社', '2020-02-01', 11, 89.00, 480, '深入理解Redis内部实现原理', 'C区-1层-02架', 5, 3);
