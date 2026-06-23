-- ============================================
-- 云课堂项目 - 数据库初始化脚本
-- 完整版： 包含所有服务的数据库表
-- ============================================

-- ============================================
-- 1. 用户数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_user;

-- 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色 ADMIN-管理员 USER-普通用户',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户课程关联表
DROP TABLE IF EXISTS t_user_course;
CREATE TABLE t_user_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    status INT DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已取消',
    pay_type INT DEFAULT NULL COMMENT '支付方式：1-微信 2-支付宝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程关联表';

-- 插入用户选课测试数据
INSERT INTO t_user_course (user_id, course_id, status, pay_type) VALUES
(1, 1, 1, 1),
(1, 2, 1, 2),
(1, 3, 0, NULL),
(2, 1, 1, 1),
(2, 3, 1, 2);

-- 插入管理员账户（密码 123456 的 BCrypt 加密）
INSERT INTO t_user (username, password, nickname, email, phone, status, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@yunh.com', '13800000000', 1, 'ADMIN');

-- 插入测试用户数据
INSERT INTO t_user (username, password, nickname, email, phone, status, role) VALUES
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'zhangsan@example.com', '13800138001', 1, 'USER'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'lisi@example.com', '13800138002', 1, 'USER'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'wangwu@example.com', '13800138003', 1, 'USER'),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 'teacher1@yunh.com', '13800138010', 1, 'TEACHER');


-- ============================================
-- 2. 课程数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_course DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_course;

-- 课程表
DROP TABLE IF EXISTS t_course;
CREATE TABLE t_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程 ID',
    name VARCHAR(200) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程描述',
    teacher_id BIGINT DEFAULT NULL COMMENT '讲师 ID',
    teacher_name VARCHAR(50) DEFAULT NULL COMMENT '讲师名称',
    price DECIMAL(10,2) DEFAULT '0.00' COMMENT '价格',
    stock INT DEFAULT 100 COMMENT '库存',
    cover VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_teacher (teacher_id),
    KEY idx_status (status),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 插入测试课程数据
INSERT INTO t_course (name, description, teacher_id, teacher_name, price, status) VALUES
('Spring Cloud 微服务实战', '从零开始学习 Spring Cloud 微服务架构，掌握 Nacos、Gateway、Feign、Sentinel 等核心组件', 1, '张老师', 199.00, 1),
('Vue.js 前端开发入门', '快速掌握 Vue.js 前端框架，从基础到实战', 2, '李老师', 99.00, 1),
('MySQL 性能优化', '深入理解 MySQL 索引、锁、事务，掌握性能调优技巧', 1, '张老师', 159.00, 1);


-- ============================================
-- 3. 订单数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_order;

-- 订单表
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单 ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    user_name VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    course_name VARCHAR(200) DEFAULT NULL COMMENT '课程名称',
    original_price DECIMAL(10,2) DEFAULT '0.00' COMMENT '原价',
    pay_amount DECIMAL(10,2) DEFAULT '0.00' COMMENT '实付金额',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已完成 3-已取消',
    pay_type TINYINT DEFAULT NULL COMMENT '支付方式 1-微信 2-支付宝',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 购物车表
DROP TABLE IF EXISTS t_cart;
CREATE TABLE t_cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id),
    UNIQUE KEY uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 插入订单测试数据
INSERT INTO t_order (order_no, user_id, user_name, course_id, course_name, original_price, pay_amount, status, pay_type, pay_time) VALUES
('ORD20260401001', 1, '张三', 1, 'Spring Cloud 微服务实战', 199.00, 199.00, 1, 1, '2026-04-01 10:30:00'),
('ORD20260401002', 1, '张三', 2, 'Vue.js 前端开发入门', 99.00, 79.00, 1, 2, '2026-04-01 14:20:00'),
('ORD20260402001', 2, '李四', 1, 'Spring Cloud 微服务实战', 199.00, 199.00, 1, 1, '2026-04-02 09:00:00'),
('ORD20260403001', 1, '张三', 3, 'MySQL 性能优化', 159.00, 159.00, 0, NULL, NULL),
('ORD20260403002', 3, '王五', 2, 'Vue.js 前端开发入门', 99.00, 99.00, 1, 2, '2026-04-03 16:45:00'),
('ORD20260404001', 2, '李四', 3, 'MySQL 性能优化', 159.00, 129.00, 2, NULL, NULL);

-- 插入购物车测试数据
INSERT INTO t_cart (user_id, course_id) VALUES
(1, 3),
(2, 2),
(3, 1);


-- ============================================
-- 4. 支付数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_pay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_pay;

-- 支付记录表
DROP TABLE IF EXISTS t_payment;
CREATE TABLE t_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付 ID',
    payment_no VARCHAR(64) NOT NULL COMMENT '支付单号',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    amount DECIMAL(10,2) DEFAULT '0.00' COMMENT '支付金额',
    pay_type TINYINT DEFAULT 1 COMMENT '支付方式 1-微信 2-支付宝',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-失败',
    third_party_no VARCHAR(128) DEFAULT NULL COMMENT '第三方订单号',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 退款记录表
DROP TABLE IF EXISTS t_refund;
CREATE TABLE t_refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退款 ID',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款单号',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    payment_no VARCHAR(64) DEFAULT NULL COMMENT '支付单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    refund_amount DECIMAL(10,2) DEFAULT '0.00' COMMENT '退款金额',
    reason VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-成功 2-失败',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 插入支付记录测试数据
INSERT INTO t_payment (payment_no, order_no, user_id, amount, pay_type, status, third_party_no, pay_time) VALUES
('PAY20260401001', 'ORD20260401001', 1, 199.00, 1, 1, 'WX202604011030001', '2026-04-01 10:30:00'),
('PAY20260401002', 'ORD20260401002', 1, 79.00, 2, 1, 'ALI202604011420001', '2026-04-01 14:20:00'),
('PAY20260402001', 'ORD20260402001', 2, 199.00, 1, 1, 'WX202604020900001', '2026-04-02 09:00:00'),
('PAY20260403001', 'ORD20260403002', 3, 99.00, 2, 1, 'ALI202604031645001', '2026-04-03 16:45:00');

-- 插入退款记录测试数据
INSERT INTO t_refund (refund_no, order_no, payment_no, user_id, refund_amount, reason, status, refund_time) VALUES
('REF20260405001', 'ORD20260404001', NULL, 2, 129.00, '课程内容不符合预期', 1, '2026-04-05 11:00:00');


-- ============================================
-- 5. 视频数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_video DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_video;

-- 璇〓竟
DROP TABLE IF EXISTS t_chapter;
CREATE TABLE t_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

-- 视频表
DROP TABLE IF EXISTS t_video;
CREATE TABLE t_video (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '视频 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    chapter_id BIGINT DEFAULT NULL COMMENT '章节 ID',
    title VARCHAR(200) NOT NULL COMMENT '视频标题',
    description TEXT COMMENT '视频描述',
    video_url VARCHAR(500) DEFAULT NULL COMMENT '视频 URL',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面图 URL',
    duration INT DEFAULT 0 COMMENT '时长（秒）',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    play_count INT DEFAULT 0 COMMENT '播放次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_course_id (course_id),
    KEY idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- 插入章节测试数据
INSERT INTO t_chapter (course_id, title, sort) VALUES
(1, '第一章 微服务架构概述', 1),
(1, '第二章 Spring Cloud 入门', 2),
(1, '第三章 服务注册与发现', 3),
(2, '第一章 Vue.js 基础', 1),
(2, '第二章 组件化开发', 2),
(3, '第一章 MySQL 基础优化', 1),
(3, '第二章 索引优化', 2);

-- 插入视频测试数据
INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
(1, 1, '1.1 什么是微服务', '微服务架构的基本概念', '/videos/1-1.mp4', 1200, 1, 1, 256),
(1, 1, '1.2 微服务 vs 单体架构', '微服务与单体架构的对比分析', '/videos/1-2.mp4', 900, 2, 1, 180),
(1, 2, '2.1 Spring Cloud 简介', 'Spring Cloud 技术栈介绍', '/videos/2-1.mp4', 1500, 1, 1, 320),
(1, 3, '3.1 Nacos 注册中心', 'Nacos 服务注册与发现', '/videos/3-1.mp4', 1800, 1, 1, 210),
(2, 4, '1.1 Vue.js 环境搭建', 'Vue 开发环境配置', '/videos/vue-1-1.mp4', 800, 1, 1, 150),
(2, 5, '2.1 组件基础', 'Vue 组件化开发入门', '/videos/vue-2-1.mp4', 1100, 1, 1, 120),
(3, 6, '1.1 慢查询分析', 'MySQL 慢查询日志与分析', '/videos/mysql-1-1.mp4', 1300, 1, 1, 200),
(3, 7, '2.1 索引原理', 'B+ 树索引原理详解', '/videos/mysql-2-1.mp4', 1600, 1, 1, 280);


-- ============================================
-- 6. 互动数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_interaction DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_interaction;

-- 评论表
DROP TABLE IF EXISTS t_comment;
CREATE TABLE t_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    video_id BIGINT DEFAULT NULL COMMENT '视频 ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论 ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id),
    KEY idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 问答表
DROP TABLE IF EXISTS t_question;
CREATE TABLE t_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问题 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    video_id BIGINT DEFAULT NULL COMMENT '视频 ID',
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    content TEXT COMMENT '问题内容',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    answer_count INT DEFAULT 0 COMMENT '回答数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答表';

-- 回答表
DROP TABLE IF EXISTS t_answer;
CREATE TABLE t_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回答 ID',
    question_id BIGINT NOT NULL COMMENT '问题 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    content TEXT NOT NULL COMMENT '回答内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_accepted TINYINT DEFAULT 0 COMMENT '是否被采纳 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_question_id (question_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答表';

-- 笔记表
DROP TABLE IF EXISTS t_note;
CREATE TABLE t_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    video_id BIGINT DEFAULT NULL COMMENT '视频 ID',
    title VARCHAR(200) DEFAULT NULL COMMENT '笔记标题',
    content TEXT COMMENT '笔记内容',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开 0-否 1-是',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id),
    KEY idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 插入评论测试数据
INSERT INTO t_comment (user_id, course_id, video_id, content, parent_id, like_count, status) VALUES
(1, 1, 1, '讲得非常好，通俗易懂！', 0, 15, 1),
(2, 1, 1, '同意楼上，点赞！', 1, 5, 1),
(1, 1, 3, 'Nacos 这部分讲得很详细', 0, 8, 1),
(3, 2, 5, 'Vue 环境搭建终于搞明白了', 0, 3, 1),
(2, 3, 7, '慢查询优化很实用', 0, 12, 1),
(3, 1, 2, '期待更多 Spring Cloud 内容', 0, 6, 1);

-- 插入问答测试数据
INSERT INTO t_question (user_id, course_id, video_id, title, content, view_count, answer_count, status) VALUES
(1, 1, 3, 'Nacos 集群如何搭建？', '请问 Nacos 集群部署需要注意哪些问题？', 120, 2, 1),
(2, 1, 2, 'Spring Cloud 和 Dubbo 的区别？', '这两个框架各有什么优缺点？', 85, 1, 1),
(3, 2, 5, 'Vue3 和 Vue2 的主要区别？', '新手应该学 Vue2 还是 Vue3？', 200, 3, 1),
(1, 3, 8, '索引什么时候会失效？', '有哪些常见场景会导致索引失效？', 150, 2, 1);

-- 插入回答测试数据
INSERT INTO t_answer (question_id, user_id, content, like_count, is_accepted, status) VALUES
(1, 2, '建议至少 3 个节点，使用 MySQL 做数据持久化', 10, 1, 1),
(1, 3, '注意配置 cluster.conf 和 nginx 负载均衡', 5, 0, 1),
(2, 1, 'Spring Cloud 是全套微服务方案，Dubbo 更轻量级', 8, 1, 1),
(3, 1, '建议直接学 Vue3，Composition API 更灵活', 15, 1, 1),
(3, 2, 'Vue3 性能更好，生态也在快速完善', 7, 0, 1),
(4, 2, '常见的有：索引列使用函数、隐式类型转换、LIKE 前缀%', 12, 1, 1);

-- 插入笔记测试数据
INSERT INTO t_note (user_id, course_id, video_id, title, content, is_public, like_count, status) VALUES
(1, 1, 1, '微服务架构学习笔记', '微服务的核心思想是将单体应用拆分为多个小服务...', 1, 10, 1),
(1, 1, 3, 'Nacos 配置管理要点', 'Nacos 支持 namespace、group、dataId 三级隔离...', 1, 8, 1),
(2, 2, 5, 'Vue 环境搭建踩坑记录', '安装 Node.js 时注意版本兼容性...', 1, 5, 1),
(3, 3, 8, 'MySQL 索引优化总结', '索引失效的常见场景总结...', 1, 20, 1),
(1, 3, 7, '慢查询分析工具笔记', 'EXPLAIN 的使用方法和参数解读...', 0, 3, 1);


-- ============================================
-- 7. 统计数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS yunh_statistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunh_statistics;

-- 课程统计表
DROP TABLE IF EXISTS t_course_statistics;
CREATE TABLE t_course_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计 ID',
    course_id BIGINT NOT NULL COMMENT '课程 ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    buy_count INT DEFAULT 0 COMMENT '购买数',
    income DECIMAL(10,2) DEFAULT '0.00' COMMENT '收入',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    student_count INT DEFAULT 0 COMMENT '学员数',
    KEY idx_course_id (course_id),
    KEY idx_stat_date (stat_date),
    UNIQUE KEY uk_course_date (course_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程统计表';

-- 平台统计表
DROP TABLE IF EXISTS t_platform_statistics;
CREATE TABLE t_platform_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计 ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    new_user_count INT DEFAULT 0 COMMENT '新增用户数',
    active_user_count INT DEFAULT 0 COMMENT '活跃用户数',
    new_order_count INT DEFAULT 0 COMMENT '新增订单数',
    total_income DECIMAL(12,2) DEFAULT '0.00' COMMENT '总收入',
    new_course_count INT DEFAULT 0 COMMENT '新增课程数',
    total_video_views BIGINT DEFAULT 0 COMMENT '视频播放总数',
    UNIQUE KEY uk_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台统计表';

-- 插入课程统计测试数据
INSERT INTO t_course_statistics (course_id, stat_date, view_count, buy_count, income, comment_count, student_count) VALUES
(1, '2026-04-01', 50, 2, 398.00, 3, 2),
(1, '2026-04-02', 80, 1, 199.00, 1, 1),
(1, '2026-04-03', 65, 0, 0.00, 2, 0),
(2, '2026-04-01', 30, 1, 79.00, 1, 1),
(2, '2026-04-02', 45, 1, 99.00, 1, 1),
(2, '2026-04-03', 55, 1, 99.00, 0, 1),
(3, '2026-04-01', 40, 0, 0.00, 1, 0),
(3, '2026-04-02', 60, 0, 0.00, 0, 0),
(3, '2026-04-03', 70, 0, 0.00, 1, 0);

-- 插入平台统计测试数据
INSERT INTO t_platform_statistics (stat_date, new_user_count, active_user_count, new_order_count, total_income, new_course_count, total_video_views) VALUES
('2026-04-01', 3, 3, 2, 477.00, 3, 320),
('2026-04-02', 0, 2, 1, 199.00, 0, 250),
('2026-04-03', 0, 3, 2, 99.00, 0, 380);


-- ============================================
-- 8. Redis 缓存键值设计
-- ============================================
-- 缓存键前缀
-- yunh:user:{id}              用户信息
-- yunh:course:{id}             课程信息
-- yunh:course:list             课程列表
-- yunh:video:{id}              视频信息
-- yunh:order:{orderNo}         订单信息
-- yunh:search:hot              热门搜索词
-- yunh:lock:order:{orderNo}    订单锁

