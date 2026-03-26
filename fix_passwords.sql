-- Update passwords for all users to use correct BCrypt hash for "123456"
-- The correct hash for "123456" is: $2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a

UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'admin';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'zhang_counselor';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'li_counselor';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'wang_counselor';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'student1';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'student2';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'student3';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'student4';
UPDATE users SET password = '$2a$10$YFPZf8hJlXahnFB2dGKXBezX99yx2.8ucL5qhqvUla9Q5UCNQEI.a' WHERE username = 'student5';

-- Verify the update
SELECT username, password FROM users;