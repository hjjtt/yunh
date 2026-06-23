-- ============================================
-- 云课堂项目 - 插入测试视频数据
-- 使用公开的 MP4 直链，可直接在小程序中播放
-- ============================================

-- ============================================
-- 1. 更新现有视频的 URL（原来都是假路径）
-- ============================================
USE yunh_video;

UPDATE t_video SET video_url = 'https://www.w3schools.com/html/mov_bbb.mp4', duration = 10 WHERE id = 1;
UPDATE t_video SET video_url = 'https://www.w3schools.com/html/movie.mp4', duration = 12 WHERE id = 2;
UPDATE t_video SET video_url = 'https://media.w3.org/2010/05/sintel/trailer.mp4', duration = 33 WHERE id = 3;
UPDATE t_video SET video_url = 'https://www.w3schools.com/html/mov_bbb.mp4', duration = 10 WHERE id = 4;
UPDATE t_video SET video_url = 'https://www.w3schools.com/html/movie.mp4', duration = 12 WHERE id = 5;
UPDATE t_video SET video_url = 'https://media.w3.org/2010/05/sintel/trailer.mp4', duration = 33 WHERE id = 6;
UPDATE t_video SET video_url = 'https://www.w3schools.com/html/mov_bbb.mp4', duration = 10 WHERE id = 7;
UPDATE t_video SET video_url = 'https://www.w3schools.com/html/movie.mp4', duration = 12 WHERE id = 8;

-- ============================================
-- 2. 新增课程（课程 ID 4、5）
-- ============================================
USE yunh_course;

INSERT INTO t_course (id, name, description, teacher_id, teacher_name, price, stock, status) VALUES
(4, 'Java 设计模式精讲', '深入理解 23 种设计模式，结合真实项目案例讲解', 1, '张老师', 129.00, 200, 1),
(5, 'Redis 从入门到精通', 'Redis 核心数据结构、持久化、集群、缓存实战', 2, '李老师', 149.00, 150, 1);

-- ============================================
-- 3. 新增章节
-- ============================================
USE yunh_video;

INSERT INTO t_chapter (course_id, title, sort) VALUES
-- 课程 4：Java 设计模式精讲
(4, '第一章 创建型模式', 1),
(4, '第二章 结构型模式', 2),
-- 课程 5：Redis 从入门到精通
(5, '第一章 Redis 基础', 1),
(5, '第二章 Redis 持久化与集群', 2);

-- ============================================
-- 4. 新增视频（使用公开 MP4 直链）
-- ============================================
INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
-- 课程 4 的视频（章节 8、9）
(4, 8, '1.1 单例模式', '饿汉式、懒汉式、枚举等多种实现方式', 'https://www.w3schools.com/html/mov_bbb.mp4', 10, 1, 1, 88),
(4, 8, '1.2 工厂方法模式', '简单工厂、工厂方法、抽象工厂对比', 'https://www.w3schools.com/html/movie.mp4', 12, 2, 1, 65),
(4, 9, '2.1 代理模式', '静态代理与 JDK 动态代理', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 33, 1, 1, 102),
-- 课程 5 的视频（章节 10、11）
(5, 10, '1.1 Redis 数据类型', 'String、Hash、List、Set、ZSet 详解', 'https://www.w3schools.com/html/movie.mp4', 12, 1, 1, 150),
(5, 10, '1.2 Redis 缓存实战', '缓存穿透、击穿、雪崩的解决方案', 'https://www.w3schools.com/html/mov_bbb.mp4', 10, 2, 1, 200),
(5, 11, '2.1 RDB 与 AOF', 'Redis 两种持久化方式的原理与选择', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 33, 1, 1, 130),
(5, 11, '2.2 Redis 集群搭建', '主从复制、哨兵模式、Cluster 集群', 'https://www.w3schools.com/html/mov_bbb.mp4', 10, 2, 1, 95);

-- ============================================
-- 5. 更新课程封面图（所有课程）
-- ============================================
USE yunh_course;

UPDATE t_course SET cover = 'https://picsum.photos/seed/course1/400/225' WHERE id = 1;
UPDATE t_course SET cover = 'https://picsum.photos/seed/course2/400/225' WHERE id = 2;
UPDATE t_course SET cover = 'https://picsum.photos/seed/course3/400/225' WHERE id = 3;
UPDATE t_course SET cover = 'https://picsum.photos/seed/course4/400/225' WHERE id = 4;
UPDATE t_course SET cover = 'https://picsum.photos/seed/course5/400/225' WHERE id = 5;
