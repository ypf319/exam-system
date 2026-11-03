CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE exam_system;

DROP TABLE IF EXISTS `exam_record`;
DROP TABLE IF EXISTS `paper_question`;
DROP TABLE IF EXISTS `paper`;
DROP TABLE IF EXISTS `question`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `real_name` VARCHAR(20),
  `role` TINYINT NOT NULL COMMENT '1管理员 2教师 3学生',
  `status` TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `course` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `course_name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `question` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `course_id` INT NOT NULL,
  `title` TEXT NOT NULL,
  `question_type` TINYINT NOT NULL COMMENT '1单选 2多选 3判断',
  `option_a` VARCHAR(255),
  `option_b` VARCHAR(255),
  `option_c` VARCHAR(255),
  `option_d` VARCHAR(255),
  `answer` VARCHAR(50) NOT NULL COMMENT '答案：A;AB;true',
  `score` INT NOT NULL,
  `difficulty` TINYINT COMMENT '1简单 2中等 3困难',
  `create_by` INT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_type` (`question_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `paper` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `paper_name` VARCHAR(100) NOT NULL,
  `course_id` INT NOT NULL,
  `total_score` INT NOT NULL,
  `duration` INT NOT NULL,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `create_by` INT NOT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0未发布 1启用 2关闭',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `paper_question` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `paper_id` INT NOT NULL,
  `question_id` INT NOT NULL,
  `score` INT NOT NULL,
  INDEX `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exam_record` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `paper_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `student_answer` TEXT,
  `score` INT DEFAULT 0,
  `submit_time` DATETIME,
  `status` TINYINT DEFAULT 0 COMMENT '0未提交 1已提交',
  INDEX `idx_paper_id` (`paper_id`),
  INDEX `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 1),
('teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 2, 1),
('teacher2', 'e10adc3949ba59abbe56e057f20f883e', '刘老师', 2, 1),
('student1', 'e10adc3949ba59abbe56e057f20f883e', '李同学', 3, 1),
('student2', 'e10adc3949ba59abbe56e057f20f883e', '王同学', 3, 1),
('student3', 'e10adc3949ba59abbe56e057f20f883e', '赵同学', 3, 1);

INSERT INTO `course` (`course_name`, `description`) VALUES
('JavaEE 应用开发', 'JavaEE 框架与 Web 应用开发课程'),
('数据结构', '线性表、树、图与常用算法'),
('计算机网络', '网络体系结构与协议');

INSERT INTO `question` (`course_id`, `title`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `answer`, `score`, `difficulty`, `create_by`) VALUES
(1, 'SpringBoot 默认内嵌的 Web 容器是？', 1, 'Tomcat', 'Jetty', 'Undertow', 'WebLogic', 'A', 5, 1, 2),
(1, '下列属于 SpringBoot 核心特性的是？', 2, '自动配置', '起步依赖', '内嵌容器', '必须编写 web.xml', 'ABC', 5, 2, 2),
(1, 'SpringBoot 项目默认的配置文件名称是 application.yml 或 application.properties。', 3, NULL, NULL, NULL, NULL, 'true', 5, 1, 2),
(1, 'MyBatis 中用于执行查询的标签是？', 1, 'select', 'insert', 'update', 'delete', 'A', 5, 1, 2),
(1, '关于 RESTful 风格，下列说法正确的是？', 2, 'GET 用于查询', 'POST 用于新增', 'PUT 用于更新', 'DELETE 用于删除', 'ABCD', 5, 2, 2),
(1, 'Spring 的两大核心是 IoC 和 AOP。', 3, NULL, NULL, NULL, NULL, 'true', 5, 1, 2),
(1, 'MySQL 中用于随机排序的函数是？', 1, 'RAND()', 'NOW()', 'COUNT()', 'SUM()', 'A', 5, 3, 2),
(2, '栈的特点是？', 1, '先进先出', '后进先出', '随机存取', '顺序存取', 'B', 5, 1, 2),
(2, '下列属于线性结构的是？', 2, '数组', '链表', '栈', '二叉树', 'ABC', 5, 2, 2),
(2, '二叉搜索树的中序遍历结果是有序序列。', 3, NULL, NULL, NULL, NULL, 'true', 5, 2, 2),
(2, '快速排序的平均时间复杂度是？', 1, 'O(n)', 'O(nlogn)', 'O(n^2)', 'O(logn)', 'B', 5, 3, 2),
(3, 'HTTP 协议默认端口是？', 1, '21', '25', '80', '443', 'C', 5, 1, 3),
(3, '下列属于应用层协议的是？', 2, 'HTTP', 'FTP', 'TCP', 'DNS', 'ABD', 5, 1, 3),
(3, 'TCP 是面向连接的可靠传输协议。', 3, NULL, NULL, NULL, NULL, 'true', 5, 1, 3),
(3, 'IP 地址 192.168.1.1 属于哪一类地址？', 1, 'A 类', 'B 类', 'C 类', 'D 类', 'C', 5, 2, 3);

INSERT INTO `paper` (`paper_name`, `course_id`, `total_score`, `duration`, `start_time`, `end_time`, `create_by`, `status`) VALUES
('JavaEE 应用开发期中测试', 1, 40, 60, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 2, 1);

INSERT INTO `paper_question` (`paper_id`, `question_id`, `score`) VALUES
(1, 1, 5), (1, 2, 5), (1, 3, 5), (1, 4, 5), (1, 5, 5), (1, 6, 5), (1, 7, 5), (1, 8, 5);
