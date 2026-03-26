USE counselling_db;

UPDATE users
SET password = '$2a$12$YB3xyg40MFaq6ql4tN8tAeB2viztbKXkuqHV8m5vAX/07Y09/qB62'
WHERE username = 'admin';

SELECT username, password FROM users WHERE username = 'admin';

