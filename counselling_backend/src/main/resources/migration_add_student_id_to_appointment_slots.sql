-- Migration script to add student_id column to appointment_slots table
-- Run this if your database already exists and doesn't have the student_id column
-- Note: If the column already exists, this will fail with an error, which is safe to ignore

ALTER TABLE appointment_slots 
ADD COLUMN student_id BIGINT NULL AFTER status,
ADD FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL;

