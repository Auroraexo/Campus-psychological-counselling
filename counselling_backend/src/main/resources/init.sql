-- 创建数据库
CREATE DATABASE IF NOT EXISTS counselling_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE counselling_db;

-- 删除现有表（如果存在），确保重新创建
DROP TABLE IF EXISTS counseling_feedback;
DROP TABLE IF EXISTS counseling_sessions;
DROP TABLE IF EXISTS counseling_records;
DROP TABLE IF EXISTS appointment_slots;
DROP TABLE IF EXISTS student_profiles;
DROP TABLE IF EXISTS counselor_profiles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS counseling_types;
DROP TABLE IF EXISTS departments;

-- 学院/部门表
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- 咨询类型表
CREATE TABLE counseling_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    real_name VARCHAR(50) NOT NULL,
    avatar_url VARCHAR(255),
    role ENUM('STUDENT', 'COUNSELOR', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- 学生信息表
CREATE TABLE student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    grade VARCHAR(50) NOT NULL,
    major VARCHAR(100) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 咨询师信息表
CREATE TABLE counselor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    department_id BIGINT,
    specialty VARCHAR(255),
    qualification VARCHAR(255),
    experience_years INT,
    bio TEXT,
    available_days VARCHAR(50), -- 逗号分隔的星期几，如'MON,TUE,WED'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 咨询师预约时间段表
CREATE TABLE appointment_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counselor_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'CANCELLED') NOT NULL DEFAULT 'AVAILABLE',
    student_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (counselor_id) REFERENCES users(id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_slot (counselor_id, slot_date, start_time, end_time)
);

-- 咨询记录表
CREATE TABLE counseling_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    counselor_id BIGINT NOT NULL,
    type_id BIGINT,
    title VARCHAR(100) NOT NULL,
    summary VARCHAR(255),
    content TEXT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    appointment_time TIMESTAMP NULL,
    actual_start_time TIMESTAMP NULL,
    actual_end_time TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (counselor_id) REFERENCES users(id),
    FOREIGN KEY (type_id) REFERENCES counseling_types(id)
);

-- 咨询会话记录表
CREATE TABLE counseling_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    session_number INT NOT NULL,
    session_date TIMESTAMP NOT NULL,
    duration INT, -- 分钟
    session_content TEXT,
    next_plan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES counseling_records(id) ON DELETE CASCADE,
    UNIQUE KEY unique_session (record_id, session_number)
);

-- 咨询反馈表
CREATE TABLE counseling_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT,
    anonymous BOOLEAN DEFAULT FALSE,
    counselor_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES counseling_records(id) ON DELETE CASCADE,
    UNIQUE KEY unique_record_feedback (record_id) -- 每条记录只能有一个反馈
);

-- 创建索引以提高查询性能
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_records_student ON counseling_records(student_id);
CREATE INDEX idx_records_counselor ON counseling_records(counselor_id);
CREATE INDEX idx_records_status ON counseling_records(status);
CREATE INDEX idx_appointment_slots_counselor ON appointment_slots(counselor_id);
CREATE INDEX idx_appointment_slots_date ON appointment_slots(slot_date);
CREATE INDEX idx_appointment_slots_status ON appointment_slots(status);

-- 插入基础数据

-- 插入学院/部门数据
INSERT INTO departments (name, code) VALUES
('计算机科学与技术学院', 'CS'),
('心理学系', 'PSY'),
('经济管理学院', 'ECON'),
('外国语学院', 'FOREIGN'),
('文学院', 'LITERATURE')
ON DUPLICATE KEY UPDATE name = name;

-- 插入咨询类型数据
INSERT INTO counseling_types (name, description) VALUES
('学业压力', '针对学习压力、考试焦虑等问题的咨询'),
('人际关系', '处理同学、室友、家庭等人际关系问题'),
('情绪管理', '帮助管理抑郁、焦虑、愤怒等情绪'),
('职业规划', '提供职业方向、就业准备等指导'),
('自我成长', '促进个人认知、自信提升等方面的发展'),
('危机干预', '处理紧急心理危机情况')
ON DUPLICATE KEY UPDATE name = name;

-- 密码历史表
CREATE TABLE password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hashed_password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建密码历史表索引
CREATE INDEX idx_password_history_user ON password_history(user_id);

-- 插入管理员用户 (密码: 123456)
INSERT INTO users (username, password, email, real_name, role, active) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', '系统管理员', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- 插入咨询师用户 (密码: 123456)
INSERT INTO users (username, password, email, phone, real_name, role, active) VALUES
('zhang_counselor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'zhang@example.com', '13800138001', '张老师', 'COUNSELOR', TRUE),
('li_counselor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'li@example.com', '13800138002', '李老师', 'COUNSELOR', TRUE),
('wang_counselor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'wang@example.com', '13800138003', '王老师', 'COUNSELOR', TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- 插入学生用户 (密码: 123456)
INSERT INTO users (username, password, email, phone, real_name, role, active) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'student1@example.com', '13900139001', '王小明', 'STUDENT', TRUE),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'student2@example.com', '13900139002', '李红', 'STUDENT', TRUE),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'student3@example.com', '13900139003', '张伟', 'STUDENT', TRUE),
('student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'student4@example.com', '13900139004', '刘洋', 'STUDENT', TRUE),
('student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'student5@example.com', '13900139005', '陈静', 'STUDENT', TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- 插入咨询师信息
INSERT INTO counselor_profiles (user_id, employee_id, department_id, specialty, qualification, experience_years, bio, available_days) VALUES
(2, 'EMP001', 2, '青少年心理、情绪管理', '国家二级心理咨询师', 8, '专注于青少年心理健康教育，擅长情绪管理和压力疏导', 'MON,TUE,THU,FRI'),
(3, 'EMP002', 2, '人际关系、家庭治疗', '国家二级心理咨询师', 5, '擅长处理人际关系问题和家庭关系调解', 'MON,WED,THU,SAT'),
(4, 'EMP003', 2, '职业规划、自我成长', '国家三级心理咨询师', 3, '致力于帮助学生进行职业规划和个人成长', 'TUE,WED,FRI,SAT')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 插入学生信息
INSERT INTO student_profiles (user_id, student_id, department_id, grade, major, gender, date_of_birth, emergency_contact, emergency_phone) VALUES
(5, '20220001', 1, '大三', '计算机科学与技术', 'MALE', '2003-05-12', '王建国', '13812345678'),
(6, '20220002', 3, '大三', '工商管理', 'FEMALE', '2003-08-20', '李梅', '13887654321'),
(7, '20220003', 1, '大二', '软件工程', 'MALE', '2004-02-15', '张明', '13912345678'),
(8, '20220004', 4, '大二', '英语', 'FEMALE', '2004-06-30', '刘芳', '13987654321'),
(9, '20220005', 5, '大一', '汉语言文学', 'FEMALE', '2005-01-18', '陈强', '13712345678')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 插入预约时间段
SET @counselor_id = 2;
SET @start_date = CURDATE();

-- 为张老师创建未来7天的预约时间段
INSERT INTO appointment_slots (counselor_id, slot_date, start_time, end_time, status) VALUES
(@counselor_id, DATE_ADD(@start_date, INTERVAL 1 DAY), '09:00:00', '10:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 1 DAY), '10:00:00', '11:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 2 DAY), '14:00:00', '15:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 2 DAY), '15:00:00', '16:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 4 DAY), '09:00:00', '10:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 5 DAY), '14:00:00', '15:00:00', 'AVAILABLE');

-- 为李老师创建未来7天的预约时间段
SET @counselor_id = 3;
INSERT INTO appointment_slots (counselor_id, slot_date, start_time, end_time, status) VALUES
(@counselor_id, DATE_ADD(@start_date, INTERVAL 1 DAY), '14:00:00', '15:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 1 DAY), '15:00:00', '16:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 3 DAY), '09:00:00', '10:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 3 DAY), '10:00:00', '11:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 4 DAY), '14:00:00', '15:00:00', 'AVAILABLE'),
(@counselor_id, DATE_ADD(@start_date, INTERVAL 6 DAY), '09:00:00', '10:00:00', 'AVAILABLE');

-- 插入示例咨询记录
INSERT INTO counseling_records (student_id, counselor_id, type_id, title, summary, content, status, appointment_time) VALUES
(5, 2, 1, '期末考试压力大', '临近考试，感到压力大，无法集中注意力', '最近期末考试临近，我感到压力非常大，晚上经常失眠，白天上课无法集中注意力。我担心自己考不好，对不起父母的期望。希望能够得到一些调整心态的建议。', 'COMPLETED', '2025-01-10 10:00:00'),
(6, 3, 2, '室友关系紧张', '与室友在生活习惯上存在分歧，导致关系紧张', '我和室友在作息时间上有很大差异，我习惯早睡早起，但她经常熬夜，影响我休息。我们已经发生过几次争执，现在关系很紧张，不知道该如何处理。', 'IN_PROGRESS', '2025-01-12 15:00:00'),
(7, 4, 3, '情绪低落', '近期情绪持续低落，对事物提不起兴趣', '最近一个月，我总是感到情绪低落，对以前感兴趣的事情也提不起精神，学习效率下降。我尝试过自我调节，但效果不明显，希望能得到专业的帮助。', 'PENDING', '2025-01-15 09:00:00'),
(8, 2, 4, '职业规划迷茫', '对未来职业发展方向感到迷茫', '我是英语专业的学生，但不确定毕业后是继续深造还是直接就业。如果就业，我也不确定应该选择什么行业。希望能够得到一些职业规划的指导和建议。', 'PENDING', '2025-01-16 14:00:00'),
(9, 3, 5, '自信心不足', '在社交场合感到紧张，缺乏自信', '我性格比较内向，在公共场合发言或与陌生人交流时会感到非常紧张，总是担心自己表现不好。希望能够提升自己的社交能力和自信心。', 'COMPLETED', '2025-01-08 10:00:00')
ON DUPLICATE KEY UPDATE id = id;

-- 插入咨询会话记录
INSERT INTO counseling_sessions (record_id, session_number, session_date, duration, session_content, next_plan) VALUES
(1, 1, '2025-01-10 10:00:00', 60, '1. 了解学生压力来源\n2. 教授呼吸放松技巧\n3. 制定合理的学习计划\n4. 调整对考试的认知', '1. 按照学习计划执行\n2. 每天进行15分钟的放松练习\n3. 下次咨询时间：2025-01-17'),
(1, 2, '2025-01-17 10:00:00', 60, '1. 回顾上周学习计划执行情况\n2. 强化放松技巧练习\n3. 进一步调整考试认知\n4. 建立积极的自我暗示', '继续保持良好的学习节奏，考试前一周再次进行咨询'),
(5, 1, '2025-01-08 10:00:00', 60, '1. 了解自信心不足的具体表现\n2. 分析产生自卑的原因\n3. 教授积极心理暗示方法\n4. 设计简单的社交练习任务', '1. 完成3次简单的社交练习\n2. 每天记录1件自己的优点\n3. 下次咨询时间：2025-01-15')
ON DUPLICATE KEY UPDATE id = id;

-- 插入咨询反馈
INSERT INTO counseling_feedback (record_id, rating, feedback, anonymous, counselor_response) VALUES
(1, 5, '张老师非常专业，教会了我很多实用的减压技巧，现在我感觉轻松多了，对考试也更有信心了。', FALSE, '很高兴能够帮助到你，继续保持积极的心态，相信你一定能够取得好成绩。'),
(5, 4, '李老师很耐心，给了我很多有用的建议。我正在尝试按照老师的方法去做，希望能够逐渐克服社交恐惧。', FALSE, '看到你的进步我很欣慰，改变需要时间，慢慢来，你已经做得很好了。')
ON DUPLICATE KEY UPDATE id = id;

-- 完成初始化
SELECT '数据库初始化完成！' AS message;