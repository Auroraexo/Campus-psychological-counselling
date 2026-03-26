-- 更新所有用户的密码哈希，使用新生成的哈希值
-- 原密码: 123456
-- 新哈希: $2a$10$joNxLxsmUAiAda5KUrMo7.FNoc8pBvq/fTI/vZCt2.EhwPAB9dWcq

USE counselling_db;

-- 更新管理员密码
UPDATE users SET password = '$2a$10$joNxLxsmUAiAda5KUrMo7.FNoc8pBvq/fTI/vZCt2.EhwPAB9dWcq' WHERE username = 'admin';

-- 更新咨询师密码
UPDATE users SET password = '$2a$10$joNxLxsmUAiAda5KUrMo7.FNoc8pBvq/fTI/vZCt2.EhwPAB9dWcq' WHERE username IN ('zhang_counselor', 'li_counselor', 'wang_counselor');

-- 更新学生密码
UPDATE users SET password = '$2a$10$joNxLxsmUAiAda5KUrMo7.FNoc8pBvq/fTI/vZCt2.EhwPAB9dWcq' WHERE username IN ('student1', 'student2', 'student3', 'student4', 'student5');