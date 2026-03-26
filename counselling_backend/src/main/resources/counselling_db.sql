/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : counselling_db

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 18/11/2025 15:37:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for appointment_slots
-- ----------------------------
DROP TABLE IF EXISTS `appointment_slots`;
CREATE TABLE `appointment_slots`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `counselor_id` bigint(20) NOT NULL,
  `slot_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `status` enum('AVAILABLE','BOOKED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE',
  `student_id` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_slot`(`counselor_id`, `slot_date`, `start_time`, `end_time`) USING BTREE,
  INDEX `student_id`(`student_id`) USING BTREE,
  INDEX `idx_appointment_slots_counselor`(`counselor_id`) USING BTREE,
  INDEX `idx_appointment_slots_date`(`slot_date`) USING BTREE,
  INDEX `idx_appointment_slots_status`(`status`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Fixed;

-- ----------------------------
-- Records of appointment_slots
-- ----------------------------
INSERT INTO `appointment_slots` VALUES (1, 2, '2025-11-19', '09:00:00', '10:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (2, 2, '2025-11-19', '10:00:00', '11:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (3, 2, '2025-11-20', '14:00:00', '15:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (4, 2, '2025-11-20', '15:00:00', '16:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (5, 2, '2025-11-22', '09:00:00', '10:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (6, 2, '2025-11-23', '14:00:00', '15:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (7, 3, '2025-11-19', '14:00:00', '15:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (8, 3, '2025-11-19', '15:00:00', '16:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (9, 3, '2025-11-21', '09:00:00', '10:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (10, 3, '2025-11-21', '10:00:00', '11:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (11, 3, '2025-11-22', '14:00:00', '15:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `appointment_slots` VALUES (12, 3, '2025-11-24', '09:00:00', '10:00:00', 'AVAILABLE', NULL, '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for counseling_feedback
-- ----------------------------
DROP TABLE IF EXISTS `counseling_feedback`;
CREATE TABLE `counseling_feedback`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `rating` int(11) NOT NULL,
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `anonymous` tinyint(1) NULL DEFAULT 0,
  `counselor_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_record_feedback`(`record_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counseling_feedback
-- ----------------------------
INSERT INTO `counseling_feedback` VALUES (1, 1, 5, '张老师非常专业，教会了我很多实用的减压技巧，现在我感觉轻松多了，对考试也更有信心了。', 0, '很高兴能够帮助到你，继续保持积极的心态，相信你一定能够取得好成绩。', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_feedback` VALUES (2, 5, 4, '李老师很耐心，给了我很多有用的建议。我正在尝试按照老师的方法去做，希望能够逐渐克服社交恐惧。', 0, '看到你的进步我很欣慰，改变需要时间，慢慢来，你已经做得很好了。', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for counseling_records
-- ----------------------------
DROP TABLE IF EXISTS `counseling_records`;
CREATE TABLE `counseling_records`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `counselor_id` bigint(20) NOT NULL,
  `type_id` bigint(20) NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `appointment_time` timestamp NULL DEFAULT NULL,
  `actual_start_time` timestamp NULL DEFAULT NULL,
  `actual_end_time` timestamp NULL DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `type_id`(`type_id`) USING BTREE,
  INDEX `idx_records_student`(`student_id`) USING BTREE,
  INDEX `idx_records_counselor`(`counselor_id`) USING BTREE,
  INDEX `idx_records_status`(`status`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counseling_records
-- ----------------------------
INSERT INTO `counseling_records` VALUES (1, 5, 2, 1, '期末考试压力大', '临近考试，感到压力大，无法集中注意力', '最近期末考试临近，我感到压力非常大，晚上经常失眠，白天上课无法集中注意力。我担心自己考不好，对不起父母的期望。希望能够得到一些调整心态的建议。', 'COMPLETED', '2025-01-10 10:00:00', NULL, NULL, NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_records` VALUES (2, 6, 3, 2, '室友关系紧张', '与室友在生活习惯上存在分歧，导致关系紧张', '我和室友在作息时间上有很大差异，我习惯早睡早起，但她经常熬夜，影响我休息。我们已经发生过几次争执，现在关系很紧张，不知道该如何处理。', 'IN_PROGRESS', '2025-01-12 15:00:00', NULL, NULL, NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_records` VALUES (3, 7, 4, 3, '情绪低落', '近期情绪持续低落，对事物提不起兴趣', '最近一个月，我总是感到情绪低落，对以前感兴趣的事情也提不起精神，学习效率下降。我尝试过自我调节，但效果不明显，希望能得到专业的帮助。', 'PENDING', '2025-01-15 09:00:00', NULL, NULL, NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_records` VALUES (4, 8, 2, 4, '职业规划迷茫', '对未来职业发展方向感到迷茫', '我是英语专业的学生，但不确定毕业后是继续深造还是直接就业。如果就业，我也不确定应该选择什么行业。希望能够得到一些职业规划的指导和建议。', 'PENDING', '2025-01-16 14:00:00', NULL, NULL, NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_records` VALUES (5, 9, 3, 5, '自信心不足', '在社交场合感到紧张，缺乏自信', '我性格比较内向，在公共场合发言或与陌生人交流时会感到非常紧张，总是担心自己表现不好。希望能够提升自己的社交能力和自信心。', 'COMPLETED', '2025-01-08 10:00:00', NULL, NULL, NULL, '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_records` VALUES (6, 5, 2, NULL, 'wfgvsafvsa', '', 'wfgvsafvsa', 'PENDING', '2025-11-11 16:00:00', NULL, NULL, NULL, '2025-11-18 07:34:27', '2025-11-18 07:34:27');

-- ----------------------------
-- Table structure for counseling_sessions
-- ----------------------------
DROP TABLE IF EXISTS `counseling_sessions`;
CREATE TABLE `counseling_sessions`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` bigint(20) NOT NULL,
  `session_number` int(11) NOT NULL,
  `session_date` timestamp NOT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `session_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `next_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_session`(`record_id`, `session_number`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counseling_sessions
-- ----------------------------
INSERT INTO `counseling_sessions` VALUES (1, 1, 1, '2025-01-10 10:00:00', 60, '1. 了解学生压力来源\n2. 教授呼吸放松技巧\n3. 制定合理的学习计划\n4. 调整对考试的认知', '1. 按照学习计划执行\n2. 每天进行15分钟的放松练习\n3. 下次咨询时间：2025-01-17', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_sessions` VALUES (2, 1, 2, '2025-01-17 10:00:00', 60, '1. 回顾上周学习计划执行情况\n2. 强化放松技巧练习\n3. 进一步调整考试认知\n4. 建立积极的自我暗示', '继续保持良好的学习节奏，考试前一周再次进行咨询', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_sessions` VALUES (3, 5, 1, '2025-01-08 10:00:00', 60, '1. 了解自信心不足的具体表现\n2. 分析产生自卑的原因\n3. 教授积极心理暗示方法\n4. 设计简单的社交练习任务', '1. 完成3次简单的社交练习\n2. 每天记录1件自己的优点\n3. 下次咨询时间：2025-01-15', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for counseling_types
-- ----------------------------
DROP TABLE IF EXISTS `counseling_types`;
CREATE TABLE `counseling_types`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counseling_types
-- ----------------------------
INSERT INTO `counseling_types` VALUES (1, '学业压力', '针对学习压力、考试焦虑等问题的咨询', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_types` VALUES (2, '人际关系', '处理同学、室友、家庭等人际关系问题', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_types` VALUES (3, '情绪管理', '帮助管理抑郁、焦虑、愤怒等情绪', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_types` VALUES (4, '职业规划', '提供职业方向、就业准备等指导', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_types` VALUES (5, '自我成长', '促进个人认知、自信提升等方面的发展', '2025-11-18 15:32:24', NULL);
INSERT INTO `counseling_types` VALUES (6, '危机干预', '处理紧急心理危机情况', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for counselor_profiles
-- ----------------------------
DROP TABLE IF EXISTS `counselor_profiles`;
CREATE TABLE `counselor_profiles`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `employee_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `department_id` bigint(20) NULL DEFAULT NULL,
  `specialty` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `qualification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `experience_years` int(11) NULL DEFAULT NULL,
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `available_days` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`) USING BTREE,
  UNIQUE INDEX `employee_id`(`employee_id`) USING BTREE,
  INDEX `department_id`(`department_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counselor_profiles
-- ----------------------------
INSERT INTO `counselor_profiles` VALUES (1, 2, 'EMP001', 2, '青少年心理、情绪管理', '国家二级心理咨询师', 8, '专注于青少年心理健康教育，擅长情绪管理和压力疏导', 'MON,TUE,THU,FRI', '2025-11-18 15:32:24', NULL);
INSERT INTO `counselor_profiles` VALUES (2, 3, 'EMP002', 2, '人际关系、家庭治疗', '国家二级心理咨询师', 5, '擅长处理人际关系问题和家庭关系调解', 'MON,WED,THU,SAT', '2025-11-18 15:32:24', NULL);
INSERT INTO `counselor_profiles` VALUES (3, 4, 'EMP003', 2, '职业规划、自我成长', '国家三级心理咨询师', 3, '致力于帮助学生进行职业规划和个人成长', 'TUE,WED,FRI,SAT', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE,
  UNIQUE INDEX `code`(`code`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, '计算机科学与技术学院', 'CS', '2025-11-18 15:32:24', NULL);
INSERT INTO `departments` VALUES (2, '心理学系', 'PSY', '2025-11-18 15:32:24', NULL);
INSERT INTO `departments` VALUES (3, '经济管理学院', 'ECON', '2025-11-18 15:32:24', NULL);
INSERT INTO `departments` VALUES (4, '外国语学院', 'FOREIGN', '2025-11-18 15:32:24', NULL);
INSERT INTO `departments` VALUES (5, '文学院', 'LITERATURE', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for student_profiles
-- ----------------------------
DROP TABLE IF EXISTS `student_profiles`;
CREATE TABLE `student_profiles`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `department_id` bigint(20) NOT NULL,
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `date_of_birth` date NULL DEFAULT NULL,
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id`) USING BTREE,
  INDEX `department_id`(`department_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_profiles
-- ----------------------------
INSERT INTO `student_profiles` VALUES (1, 5, '20220001', 1, '大三', '计算机科学与技术', 'MALE', '2003-05-12', '王建国', '13812345678', '2025-11-18 15:32:24', NULL);
INSERT INTO `student_profiles` VALUES (2, 6, '20220002', 3, '大三', '工商管理', 'FEMALE', '2003-08-20', '李梅', '13887654321', '2025-11-18 15:32:24', NULL);
INSERT INTO `student_profiles` VALUES (3, 7, '20220003', 1, '大二', '软件工程', 'MALE', '2004-02-15', '张明', '13912345678', '2025-11-18 15:32:24', NULL);
INSERT INTO `student_profiles` VALUES (4, 8, '20220004', 4, '大二', '英语', 'FEMALE', '2004-06-30', '刘芳', '13987654321', '2025-11-18 15:32:24', NULL);
INSERT INTO `student_profiles` VALUES (5, 9, '20220005', 5, '大一', '汉语言文学', 'FEMALE', '2005-01-18', '陈强', '13712345678', '2025-11-18 15:32:24', NULL);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role` enum('STUDENT','COUNSELOR','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STUDENT',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `last_login` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE,
  UNIQUE INDEX `phone`(`phone`) USING BTREE,
  INDEX `idx_users_role`(`role`) USING BTREE,
  INDEX `idx_users_active`(`active`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$Y3v/wfX5NghxgBicEYfMNOSbL.HWBCFSi/aouOAprKECPAifHqwFi', 'admin@example.com', NULL, '系统管理员', NULL, 'ADMIN', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:36:38');
INSERT INTO `users` VALUES (2, 'zhang_counselor', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'zhang@example.com', '13800138001', '张老师', NULL, 'COUNSELOR', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:34:04');
INSERT INTO `users` VALUES (3, 'li_counselor', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'li@example.com', '13800138002', '李老师', NULL, 'COUNSELOR', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:35:40');
INSERT INTO `users` VALUES (4, 'wang_counselor', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'wang@example.com', '13800138003', '王老师', NULL, 'COUNSELOR', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:35:53');
INSERT INTO `users` VALUES (5, 'student1', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'student1@example.com', '13900139001', '王小明', NULL, 'STUDENT', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:35:55');
INSERT INTO `users` VALUES (6, 'student2', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'student2@example.com', '13900139002', '李红', NULL, 'STUDENT', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:35:57');
INSERT INTO `users` VALUES (7, 'student3', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'student3@example.com', '13900139003', '张伟', NULL, 'STUDENT', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:36:00');
INSERT INTO `users` VALUES (8, 'student4', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'student4@example.com', '13900139004', '刘洋', NULL, 'STUDENT', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:36:02');
INSERT INTO `users` VALUES (9, 'student5', '$2a$10$jlAu6eLe3ZU/tFCIn5VCxui59mz7jNy1NTV8Gde6WKcOQ4uVEMpDW', 'student5@example.com', '13900139005', '陈静', NULL, 'STUDENT', 1, NULL, '2025-11-18 15:32:24', '2025-11-18 15:36:05');

SET FOREIGN_KEY_CHECKS = 1;
