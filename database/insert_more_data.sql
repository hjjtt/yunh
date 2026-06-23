-- ============================================
-- 云课堂项目 - 补充大量测试数据
-- 目标：让课程列表、发现页、详情页内容充实
-- ============================================

-- ============================================
-- 1. 新增用户（充当讲师 + 学生）
-- ============================================
USE yunh_user;

INSERT INTO t_user (username, password, nickname, email, phone, status, role) VALUES
('teacher_wang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王老师', 'wang@example.com', '13800138004', 1, 'USER'),
('teacher_zhao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵老师', 'zhao@example.com', '13800138005', 1, 'USER'),
('teacher_liu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘老师', 'liu@example.com', '13800138006', 1, 'USER'),
('teacher_chen', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈老师', 'chen@example.com', '13800138007', 1, 'USER'),
('student_zhou', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周同学', 'zhou@example.com', '13800138008', 1, 'USER'),
('student_wu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴同学', 'wu@example.com', '13800138009', 1, 'USER'),
('student_sun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙同学', 'sun@example.com', '13800138010', 1, 'USER'),
('student_zheng', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郑同学', 'zheng@example.com', '13800138011', 1, 'USER');

-- ============================================
-- 2. 新增课程（ID 6-15）
-- ============================================
USE yunh_course;

INSERT INTO t_course (id, name, description, teacher_id, teacher_name, price, stock, cover, status) VALUES
(6,  'Docker 容器化实战', '从 Docker 基础到 Kubernetes 编排，掌握容器化部署全流程', 4, '王老师', 139.00, 180, 'https://picsum.photos/seed/course6/400/225', 1),
(7,  'Spring Boot 3 核心技术', '深入 Spring Boot 3 自动配置、Starter、Actuator 与测试', 1, '张老师', 169.00, 200, 'https://picsum.photos/seed/course7/400/225', 1),
(8,  'Python 数据分析入门', 'NumPy、Pandas、Matplotlib 数据分析三件套，零基础到实战', 5, '赵老师', 89.00, 250, 'https://picsum.photos/seed/course8/400/225', 1),
(9,  'Linux 运维基础', 'Linux 常用命令、Shell 脚本、服务部署与监控', 4, '王老师', 79.00, 300, 'https://picsum.photos/seed/course9/400/225', 1),
(10, 'Elasticsearch 全文检索', 'ES 索引设计、查询 DSL、聚合分析与 Spring Boot 整合', 6, '刘老师', 189.00, 120, 'https://picsum.photos/seed/course10/400/225', 1),
(11, 'RabbitMQ 消息队列实战', '消息模型、死信队列、延迟队列、集群搭建与 Spring AMQP 整合', 1, '张老师', 159.00, 160, 'https://picsum.photos/seed/course11/400/225', 1),
(12, 'TypeScript 从入门到进阶', '类型系统、泛型、装饰器、工程化配置，从 JS 平滑过渡到 TS', 7, '陈老师', 109.00, 220, 'https://picsum.photos/seed/course12/400/225', 1),
(13, 'MyBatis-Plus 深度剖析', 'CRUD 接口、代码生成器、分页插件、乐观锁、逻辑删除', 2, '李老师', 119.00, 200, 'https://picsum.photos/seed/course13/400/225', 1),
(14, 'Git 与团队协作开发', 'Git 工作流、分支策略、代码审查、CI/CD 集成', 5, '赵老师', 49.00, 500, 'https://picsum.photos/seed/course14/400/225', 1),
(15, '分布式系统设计', 'CAP 理论、一致性算法、分布式事务、服务治理', 6, '刘老师', 229.00, 80, 'https://picsum.photos/seed/course15/400/225', 1);

-- ============================================
-- 3. 新增章节
-- ============================================
USE yunh_video;

INSERT INTO t_chapter (course_id, title, sort) VALUES
-- 课程 6：Docker 容器化实战
(6, '第一章 Docker 基础入门', 1),
(6, '第二章 Dockerfile 与镜像', 2),
(6, '第三章 Docker Compose', 3),
-- 课程 7：Spring Boot 3 核心技术
(7, '第一章 自动配置原理', 1),
(7, '第二章 Starter 开发', 2),
(7, '第三章 Actuator 监控', 3),
-- 课程 8：Python 数据分析入门
(8, '第一章 NumPy 基础', 1),
(8, '第二章 Pandas 数据处理', 2),
(8, '第三章 数据可视化', 3),
-- 课程 9：Linux 运维基础
(9, '第一章 Linux 常用命令', 1),
(9, '第二章 Shell 脚本编程', 2),
-- 课程 10：Elasticsearch 全文检索
(10, '第一章 ES 核心概念', 1),
(10, '第二章 查询 DSL', 2),
(10, '第三章 聚合与实战', 3),
-- 课程 11：RabbitMQ 消息队列实战
(11, '第一章 RabbitMQ 基础', 1),
(11, '第二章 高级特性', 2),
(11, '第三章 集群与运维', 3),
-- 课程 12：TypeScript 从入门到进阶
(12, '第一章 类型系统基础', 1),
(12, '第二章 泛型与高级类型', 2),
(12, '第三章 工程化实战', 3),
-- 课程 13：MyBatis-Plus 深度剖析
(13, '第一章 核心 CRUD', 1),
(13, '第二章 插件与扩展', 2),
(13, '第三章 代码生成器', 3),
-- 课程 14：Git 与团队协作开发
(14, '第一章 Git 基础操作', 1),
(14, '第二章 分支与合并策略', 2),
(14, '第三章 CI/CD 流水线', 3),
-- 课程 15：分布式系统设计
(15, '第一章 分布式基础理论', 1),
(15, '第二章 分布式事务', 2),
(15, '第三章 服务治理', 3);

-- ============================================
-- 4. 新增视频（为每门新课程每章节添加 2 个视频）
-- ============================================
INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
-- 课程 6：Docker 容器化实战（章节 12-14）
(6, 12, '1.1 Docker 安装与环境配置', 'Windows / Mac / Linux 环境下 Docker 安装', 'https://www.w3schools.com/html/mov_bbb.mp4', 1200, 1, 1, 180),
(6, 12, '1.2 容器与镜像基础操作', 'run、exec、ps、images 等核心命令', 'https://www.w3schools.com/html/movie.mp4', 900, 2, 1, 150),
(6, 13, '2.1 Dockerfile 指令详解', 'FROM、RUN、COPY、ENTRYPOINT 等', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1500, 1, 1, 120),
(6, 13, '2.2 多阶段构建与优化', '减小镜像体积的最佳实践', 'https://www.w3schools.com/html/mov_bbb.mp4', 1100, 2, 1, 90),
(6, 14, '3.1 Docker Compose 编排', '多容器应用的编排与管理', 'https://www.w3schools.com/html/movie.mp4', 1400, 1, 1, 200),
(6, 14, '3.2 Compose 网络与数据卷', '服务间通信与数据持久化', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1000, 2, 1, 85),

-- 课程 7：Spring Boot 3（章节 15-17）
(7, 15, '1.1 自动配置原理剖析', '@Conditional 系列注解与自动配置报告', 'https://www.w3schools.com/html/movie.mp4', 1600, 1, 1, 260),
(7, 15, '1.2 自定义自动配置', '手写一个自定义 Spring Boot Starter', 'https://www.w3schools.com/html/mov_bbb.mp4', 1300, 2, 1, 220),
(7, 16, '2.1 Starter 开发规范', '命名规则、配置属性、条件装配', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1100, 1, 1, 180),
(7, 16, '2.2 实战：redis-spring-boot-starter', '从零开发一个 Redis Starter', 'https://www.w3schools.com/html/movie.mp4', 1500, 2, 1, 160),
(7, 17, '3.1 Actuator 端点详解', '健康检查、指标、环境信息端点', 'https://www.w3schools.com/html/mov_bbb.mp4', 1000, 1, 1, 140),
(7, 17, '3.2 自定义监控端点', '扩展 HealthIndicator 和 Endpoint', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 900, 2, 1, 95),

-- 课程 8：Python 数据分析（章节 18-20）
(8, 18, '1.1 NumPy 数组基础', 'ndarray 创建、索引、切片', 'https://www.w3schools.com/html/mov_bbb.mp4', 1100, 1, 1, 310),
(8, 18, '1.2 NumPy 数学运算', '广播机制、线性代数、随机数', 'https://www.w3schools.com/html/movie.mp4', 1000, 2, 1, 280),
(8, 19, '2.1 Pandas DataFrame', '数据读取、选择、过滤、排序', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1300, 1, 1, 350),
(8, 19, '2.2 数据清洗与处理', '缺失值、重复值、数据类型转换', 'https://www.w3schools.com/html/mov_bbb.mp4', 1200, 2, 1, 290),
(8, 20, '3.1 Matplotlib 基础绑图', '折线图、柱状图、散点图、饼图', 'https://www.w3schools.com/html/movie.mp4', 900, 1, 1, 240),
(8, 20, '3.2 Seaborn 高级可视化', '热力图、分布图、成对关系图', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1100, 2, 1, 190),

-- 课程 9：Linux 运维（章节 21-22）
(9, 21, '1.1 文件与目录操作', 'ls、cd、cp、mv、rm、find 等', 'https://www.w3schools.com/html/mov_bbb.mp4', 1000, 1, 1, 420),
(9, 21, '1.2 用户权限与进程管理', 'chmod、chown、ps、top、systemctl', 'https://www.w3schools.com/html/movie.mp4', 1200, 2, 1, 380),
(9, 22, '2.1 Shell 脚本基础', '变量、条件、循环、函数', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1400, 1, 1, 300),
(9, 22, '2.2 Shell 自动化运维脚本', '日志清理、备份、监控告警脚本', 'https://www.w3schools.com/html/mov_bbb.mp4', 1600, 2, 1, 270),

-- 课程 10：Elasticsearch（章节 23-25）
(10, 23, '1.1 ES 倒排索引原理', '分词、倒排索引、相关性评分', 'https://www.w3schools.com/html/movie.mp4', 1300, 1, 1, 170),
(10, 23, '1.2 索引与映射设计', 'Index、Mapping、Field Type 设计', 'https://www.w3schools.com/html/mov_bbb.mp4', 1100, 2, 1, 140),
(10, 24, '2.1 Query DSL 查询语法', 'match、term、bool、range 查询', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1500, 1, 1, 160),
(10, 24, '2.2 聚合查询与分析', 'terms、histogram、stats 聚合', 'https://www.w3schools.com/html/movie.mp4', 1200, 2, 1, 130),
(10, 25, '3.1 Spring Boot 整合 ES', 'ElasticsearchRestTemplate 使用', 'https://www.w3schools.com/html/mov_bbb.mp4', 1400, 1, 1, 200),
(10, 25, '3.2 电商搜索实战', '多条件筛选、高亮、分页实现', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1800, 2, 1, 185),

-- 课程 11：RabbitMQ（章节 26-28）
(11, 26, '1.1 消息模型与 Exchange', 'direct、topic、fanout、headers', 'https://www.w3schools.com/html/mov_bbb.mp4', 1200, 1, 1, 190),
(11, 26, '1.2 消息确认与持久化', 'ACK 机制、消息持久化、镜像队列', 'https://www.w3schools.com/html/movie.mp4', 1000, 2, 1, 160),
(11, 27, '2.1 死信队列与延迟消息', 'DLX + TTL 实现延迟队列', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1400, 1, 1, 220),
(11, 27, '2.2 消息幂等性设计', '全局唯一 ID + 去重表方案', 'https://www.w3schools.com/html/mov_bbb.mp4', 900, 2, 1, 130),
(11, 28, '3.1 集群搭建与高可用', '普通集群、镜像队列、仲裁队列', 'https://www.w3schools.com/html/movie.mp4', 1600, 1, 1, 110),
(11, 28, '3.2 监控与性能调优', 'Management Plugin、Prometheus 监控', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1100, 2, 1, 95),

-- 课程 12：TypeScript（章节 29-31）
(12, 29, '1.1 基础类型与接口', 'string、number、interface、type', 'https://www.w3schools.com/html/movie.mp4', 1000, 1, 1, 250),
(12, 29, '1.2 枚举与联合类型', 'enum、union、intersection、literal', 'https://www.w3schools.com/html/mov_bbb.mp4', 800, 2, 1, 210),
(12, 30, '2.1 泛型入门到精通', '泛型函数、泛型类、泛型约束', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1300, 1, 1, 190),
(12, 30, '2.2 条件类型与映射类型', 'infer、keyof、in、Partial、Required', 'https://www.w3schools.com/html/movie.mp4', 1200, 2, 1, 170),
(12, 31, '3.1 tsconfig 配置详解', 'strict、paths、baseUrl、target', 'https://www.w3schools.com/html/mov_bbb.mp4', 900, 1, 1, 140),
(12, 31, '3.2 实战：TS + Vue3 项目', '在 Vue3 中使用 TypeScript 最佳实践', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1500, 2, 1, 230),

-- 课程 13：MyBatis-Plus（章节 32-34）
(13, 32, '1.1 BaseMapper CRUD 接口', 'insert、deleteById、updateById、selectById', 'https://www.w3schools.com/html/mov_bbb.mp4', 1000, 1, 1, 270),
(13, 32, '1.2 条件构造器 Wrapper', 'QueryWrapper、LambdaQueryWrapper', 'https://www.w3schools.com/html/movie.mp4', 1200, 2, 1, 240),
(13, 33, '2.1 分页插件与乐观锁', 'PaginationInnerInterceptor、OptimisticLocker', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1100, 1, 1, 200),
(13, 33, '2.2 逻辑删除与自动填充', 'TableLogic、TableField(fill)', 'https://www.w3schools.com/html/mov_bbb.mp4', 900, 2, 1, 175),
(13, 34, '3.1 代码生成器配置', 'AutoGenerator 自定义模板与策略', 'https://www.w3schools.com/html/movie.mp4', 1300, 1, 1, 160),
(13, 34, '3.2 多数据源与动态表名', 'DynamicDatasource、动态 SQL', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1000, 2, 1, 120),

-- 课程 14：Git 与团队协作（章节 35-37）
(14, 35, '1.1 Git 核心概念与安装', '工作区、暂存区、提交历史', 'https://www.w3schools.com/html/mov_bbb.mp4', 800, 1, 1, 580),
(14, 35, '1.2 常用操作：add/commit/push', '日常开发的 Git 操作流程', 'https://www.w3schools.com/html/movie.mp4', 700, 2, 1, 520),
(14, 36, '2.1 分支管理策略', 'Git Flow、GitHub Flow、Trunk Based', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1100, 1, 1, 440),
(14, 36, '2.2 代码审查与合并', 'Pull Request、Code Review、冲突解决', 'https://www.w3schools.com/html/mov_bbb.mp4', 900, 2, 1, 390),
(14, 37, '3.1 CI/CD 基础概念', '持续集成、持续交付、持续部署', 'https://www.w3schools.com/html/movie.mp4', 1000, 1, 1, 350),
(14, 37, '3.2 GitHub Actions 实战', '自动构建、测试、部署流水线', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1300, 2, 1, 310),

-- 课程 15：分布式系统设计（章节 38-40）
(15, 38, '1.1 CAP 与 BASE 理论', '一致性、可用性、分区容错的权衡', 'https://www.w3schools.com/html/movie.mp4', 1400, 1, 1, 150),
(15, 38, '1.2 一致性算法：Raft 与 Paxos', '分布式共识算法原理与对比', 'https://www.w3schools.com/html/mov_bbb.mp4', 1600, 2, 1, 130),
(15, 39, '2.1 2PC 与 TCC 模式', '两阶段提交与补偿事务', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1200, 1, 1, 170),
(15, 39, '2.2 Seata AT 模式实战', 'Seata 在 Spring Cloud 中的集成', 'https://www.w3schools.com/html/movie.mp4', 1500, 2, 1, 190),
(15, 40, '3.1 服务注册与发现', 'Nacos、Eureka、Consul 对比', 'https://www.w3schools.com/html/mov_bbb.mp4', 1100, 1, 1, 140),
(15, 40, '3.2 熔断、限流与降级', 'Sentinel、Resilience4j 实战', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1300, 2, 1, 160);

-- ============================================
-- 5. 给原有课程（1-5）补充更多章节和视频
-- ============================================

-- 补充课程 1 的章节和视频
INSERT INTO t_chapter (course_id, title, sort) VALUES
(1, '第四章 Gateway 网关', 4),
(1, '第五章 Feign 远程调用', 5);

INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
(1, 4, '4.1 Gateway 路由配置', '路由规则、断言、过滤器', 'https://www.w3schools.com/html/movie.mp4', 1400, 1, 1, 280),
(1, 4, '4.2 全局过滤器与鉴权', 'AuthGlobalFilter 实现统一鉴权', 'https://www.w3schools.com/html/mov_bbb.mp4', 1200, 2, 1, 240),
(1, 5, '5.1 Feign 声明式调用', 'Feign Client 配置与使用', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1000, 1, 1, 195),
(1, 5, '5.2 Feign 降级与重试', 'Fallback、Retry、超时配置', 'https://www.w3schools.com/html/movie.mp4', 1100, 2, 1, 165);

-- 补充课程 2 的章节和视频
INSERT INTO t_chapter (course_id, title, sort) VALUES
(2, '第三章 路由与状态管理', 3),
(2, '第四章 项目实战', 4);

INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
(2, 4, '3.1 Vue Router 路由配置', '动态路由、嵌套路由、导航守卫', 'https://www.w3schools.com/html/mov_bbb.mp4', 1200, 1, 1, 200),
(2, 4, '3.2 Pinia 状态管理', 'Store 定义、Action、Getter', 'https://www.w3schools.com/html/movie.mp4', 1000, 2, 1, 170),
(2, 5, '4.1 Element Plus 后台实战', '布局、表格、表单、权限管理', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1800, 1, 1, 260),
(2, 5, '4.2 项目打包与部署', 'Vite 构建、Nginx 部署、环境配置', 'https://www.w3schools.com/html/mov_bbb.mp4', 900, 2, 1, 180);

-- 补充课程 3 的章节和视频
INSERT INTO t_chapter (course_id, title, sort) VALUES
(3, '第三章 事务与锁机制', 3),
(3, '第四章 性能调优实战', 4);

INSERT INTO t_video (course_id, chapter_id, title, description, video_url, duration, sort, status, play_count) VALUES
(3, 6, '3.1 事务隔离级别详解', 'READ UNCOMMITTED 到 SERIALIZABLE', 'https://www.w3schools.com/html/movie.mp4', 1300, 1, 1, 220),
(3, 6, '3.2 锁机制：表锁、行锁、间隙锁', 'InnoDB 锁类型与死锁排查', 'https://www.w3schools.com/html/mov_bbb.mp4', 1500, 2, 1, 190),
(3, 7, '4.1 EXPLAIN 执行计划分析', 'type、key、Extra 字段深度解读', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 1400, 1, 1, 310),
(3, 7, '4.2 分库分表与读写分离', 'ShardingSphere 实战与主从配置', 'https://www.w3schools.com/html/movie.mp4', 1600, 2, 1, 250);

-- ============================================
-- 6. 补充互动数据（评论、问答、笔记）
-- ============================================
USE yunh_interaction;

INSERT INTO t_comment (user_id, course_id, video_id, content, parent_id, like_count, status) VALUES
(4, 6, 16, 'Docker 讲得非常清楚，终于理解容器了！', 0, 22, 1),
(5, 7, 22, '自动配置原理讲得很透彻', 0, 18, 1),
(6, 8, 28, 'Pandas 这节课太实用了', 0, 30, 1),
(7, 9, 34, 'Linux 命令总结得很全面', 0, 15, 1),
(8, 10, 40, 'ES 查询语法讲得好', 0, 12, 1),
(1, 11, 46, 'RabbitMQ 延迟队列方案很实用', 0, 25, 1),
(2, 12, 52, 'TypeScript 泛型终于搞懂了', 0, 20, 1),
(3, 13, 58, 'MyBatis-Plus 条件构造器太好用了', 0, 16, 1),
(4, 14, 64, 'Git 分支策略讲得很清晰', 0, 35, 1),
(5, 15, 70, '分布式事务终于理解了', 0, 28, 1),
(6, 6, 17, '跟着做了一遍，成功了！', 9, 8, 1),
(7, 8, 29, '请问 Matplotlib 中文显示怎么设置？', 0, 5, 1),
(8, 11, 47, '消息幂等性这个方案很赞', 0, 14, 1),
(1, 14, 64, 'GitHub Actions 部分能不能再详细一些？', 0, 10, 1),
(2, 10, 40, '有没有 ES 和 Solr 的对比分析？', 0, 7, 1);

INSERT INTO t_question (user_id, course_id, video_id, title, content, view_count, answer_count, status) VALUES
(4, 6, 16, 'Docker 和虚拟机有什么区别？', '两者在资源占用和性能上的差异？', 200, 3, 1),
(5, 7, 23, 'Spring Boot 3 和 2 的主要区别？', '迁移时需要注意哪些问题？', 180, 2, 1),
(6, 8, 28, 'Pandas 处理大数据集内存不够怎么办？', '几百万行数据时内存溢出的解决方案', 250, 4, 1),
(7, 9, 34, 'Shell 脚本调试有什么好方法？', '除了 echo 还有什么调试手段？', 120, 2, 1),
(8, 10, 41, 'ES 集群分片数怎么规划？', '节点数、分片数、副本数的最佳实践', 160, 3, 1),
(1, 11, 46, 'RabbitMQ 和 Kafka 怎么选？', '不同场景下消息队列的选型建议', 300, 5, 1),
(2, 12, 52, 'TypeScript 编译很慢怎么办？', '大型项目中 TS 编译性能优化', 140, 2, 1),
(3, 13, 58, 'MyBatis-Plus 多表联查怎么做？', '复杂 SQL 场景下 MP 的使用建议', 190, 3, 1);

INSERT INTO t_answer (question_id, user_id, content, like_count, is_accepted, status) VALUES
(5, 1, 'Docker 是进程级隔离，虚拟机是系统级隔离，Docker 更轻量', 18, 1, 1),
(5, 2, '简单说：Docker 共享内核，虚拟机有独立内核', 10, 0, 1),
(6, 2, '主要是 JDK 17 基线、GraalVM 原生支持、配置属性变化', 12, 1, 1),
(7, 3, '用 chunksize 分块读取，或者用 Dask 库', 20, 1, 1),
(7, 4, '也可以考虑用 SQL 数据库做预处理再导入 Pandas', 8, 0, 1),
(8, 1, 'set -x 开启调试模式，配合 VS Code 的 Shell 扩展', 15, 1, 1),
(9, 4, '一般按数据量：1TB 以下 5 分片，1TB 以上 10 分片，副本 1-2 个', 12, 1, 1),
(10, 5, 'RabbitMQ 适合业务消息，Kafka 适合大数据流，看你的场景', 25, 1, 1),
(10, 6, '如果是微服务间通信选 RabbitMQ，日志采集选 Kafka', 18, 0, 1),
(11, 7, '开启 incremental 编译，或者用 esbuild-loader 替代', 10, 1, 1),
(12, 3, '简单联查用 @Select 注解写 XML，复杂的用 MP 的 join 扩展', 14, 1, 1);

INSERT INTO t_note (user_id, course_id, video_id, title, content, is_public, like_count, status) VALUES
(4, 6, 16, 'Docker 命令速查表', 'docker run、build、compose 常用命令汇总...', 1, 18, 1),
(5, 7, 23, 'Spring Boot Starter 开发笔记', '自动配置类编写、spring.factories 配置...', 1, 15, 1),
(6, 8, 29, 'Matplotlib 常用图表模板', '折线图、柱状图、散点图的代码模板...', 1, 22, 1),
(7, 9, 34, 'Shell 脚本常用片段', '循环、条件、字符串处理、文件操作...', 1, 12, 1),
(8, 10, 41, 'ES 查询 DSL 备忘录', 'bool 查询、聚合、排序的常用写法...', 1, 16, 1),
(1, 11, 46, 'RabbitMQ 六种消息模型对比', '简单队列、工作队列、发布订阅...', 1, 20, 1),
(2, 12, 53, 'TypeScript 类型体操练习', 'Partial、Required、Pick、Omit 等工具类型...', 1, 14, 1),
(3, 13, 59, 'MyBatis-Plus 插件配置清单', '分页、乐观锁、性能分析、非法 SQL 拦截...', 1, 11, 1);

-- ============================================
-- 7. 补充订单数据
-- ============================================
USE yunh_order;

INSERT INTO t_order (order_no, user_id, user_name, course_id, course_name, original_price, pay_amount, status, pay_type, pay_time) VALUES
('ORD20260405001', 4, '王五', 6, 'Docker 容器化实战', 139.00, 119.00, 1, 1, '2026-04-05 09:20:00'),
('ORD20260405002', 5, '赵六', 7, 'Spring Boot 3 核心技术', 169.00, 169.00, 1, 2, '2026-04-05 10:15:00'),
('ORD20260406001', 6, '刘七', 8, 'Python 数据分析入门', 89.00, 79.00, 1, 1, '2026-04-06 14:30:00'),
('ORD20260406002', 7, '陈八', 9, 'Linux 运维基础', 79.00, 59.00, 1, 2, '2026-04-06 16:45:00'),
('ORD20260407001', 8, '周九', 10, 'Elasticsearch 全文检索', 189.00, 169.00, 1, 1, '2026-04-07 08:30:00'),
('ORD20260407002', 1, '张三', 11, 'RabbitMQ 消息队列实战', 159.00, 139.00, 1, 2, '2026-04-07 11:00:00'),
('ORD20260408001', 2, '李四', 12, 'TypeScript 从入门到进阶', 109.00, 89.00, 1, 1, '2026-04-08 09:30:00'),
('ORD20260408002', 3, '王五', 13, 'MyBatis-Plus 深度剖析', 119.00, 99.00, 1, 2, '2026-04-08 14:20:00'),
('ORD20260409001', 4, '赵六', 14, 'Git 与团队协作开发', 49.00, 39.00, 1, 1, '2026-04-09 10:00:00'),
('ORD20260409002', 5, '刘七', 15, '分布式系统设计', 229.00, 199.00, 0, NULL, NULL),
('ORD20260410001', 6, '陈八', 6, 'Docker 容器化实战', 139.00, 139.00, 1, 1, '2026-04-10 08:15:00'),
('ORD20260410002', 7, '周九', 7, 'Spring Boot 3 核心技术', 169.00, 149.00, 2, 2, '2026-04-10 15:30:00');

-- ============================================
-- 8. 补充用户选课数据
-- ============================================
USE yunh_user;

INSERT INTO t_user_course (user_id, course_id, status, pay_type) VALUES
(4, 6, 1, 1),
(5, 7, 1, 2),
(6, 8, 1, 1),
(7, 9, 1, 2),
(8, 10, 1, 1),
(1, 11, 1, 2),
(2, 12, 1, 1),
(3, 13, 1, 2),
(4, 14, 1, 1),
(5, 15, 0, NULL),
(6, 6, 1, 1),
(7, 7, 1, 2),
(1, 8, 1, 1),
(2, 9, 1, 2),
(3, 10, 1, 1);
