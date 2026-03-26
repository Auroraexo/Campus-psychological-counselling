USE counselling_db;

-- 更新counselor1用户的密码为'password'的BCrypt哈希值
UPDATE users SET password = '$2a$10$u9xUcM9.1y8/i7IiJpwbFOjSIGa/HNgEcC26vZALqy1ve5uEnigmy' WHERE username = 'counselor1';

-- 确保用户处于激活状态
UPDATE users SET active = true WHERE username = 'counselor1';

-- 查看更新后的用户信息
SELECT id, username, active FROM users WHERE username = 'counselor1';