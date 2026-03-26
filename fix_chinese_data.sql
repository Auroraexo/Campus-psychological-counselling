SET NAMES utf8mb4;
USE counselling_db;

-- 修复 users.real_name
UPDATE users SET real_name = '系统管理员' WHERE username = 'admin';
UPDATE users SET real_name = '张老师'       WHERE username = 'zhang_counselor';
UPDATE users SET real_name = '李老师'       WHERE username = 'li_counselor';
UPDATE users SET real_name = '王老师'       WHERE username = 'wang_counselor';
UPDATE users SET real_name = '王小明'       WHERE username = 'student1';
UPDATE users SET real_name = '李红'         WHERE username = 'student2';
UPDATE users SET real_name = '张伟'         WHERE username = 'student3';
UPDATE users SET real_name = '刘洋'         WHERE username = 'student4';
UPDATE users SET real_name = '陈静'         WHERE username = 'student5';

-- 修复 departments.name
UPDATE departments SET name = '计算机科学与技术学院' WHERE code = 'CS';
UPDATE departments SET name = '心理学系'           WHERE code = 'PSY';
UPDATE departments SET name = '经济管理学院'       WHERE code = 'ECON';
UPDATE departments SET name = '外国语学院'         WHERE code = 'FOREIGN';
UPDATE departments SET name = '文学院'             WHERE code = 'LITERATURE';

-- 修复 counseling_types.name 和 description
UPDATE counseling_types
SET name = '学业压力',
    description = '针对学习压力、考试焦虑等问题的咨询'
WHERE id = 1;

UPDATE counseling_types
SET name = '人际关系',
    description = '处理同学、室友、家庭等人际关系问题'
WHERE id = 2;

UPDATE counseling_types
SET name = '情绪管理',
    description = '帮助管理抑郁、焦虑、愤怒等情绪'
WHERE id = 3;

UPDATE counseling_types
SET name = '职业规划',
    description = '提供职业方向、就业准备等指导'
WHERE id = 4;

UPDATE counseling_types
SET name = '自我成长',
    description = '促进个人认知、自信提升等方面的发展'
WHERE id = 5;

UPDATE counseling_types
SET name = '危机干预',
    description = '处理紧急心理危机情况'
WHERE id = 6;

