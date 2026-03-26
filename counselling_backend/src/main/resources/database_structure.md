# 校园心理咨询系统数据库结构说明

本文档详细介绍了校园心理咨询系统的数据库结构设计，包括表结构、字段说明、表间关系以及索引优化。

## 数据库概述

数据库名称：`counselling_db`

编码格式：`utf8mb4`

排序规则：`utf8mb4_unicode_ci`

## 表结构设计

### 1. 学院/部门表（departments）

用于存储学校的学院和部门信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 部门ID |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 部门名称 |
| `code` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 部门代码 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 2. 咨询类型表（counseling_types）

用于存储心理咨询的分类信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 咨询类型ID |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 类型名称 |
| `description` | `TEXT` | | 类型描述 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 3. 用户表（users）

系统的核心表，存储所有用户信息（学生、咨询师和管理员）。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 用户ID |
| `username` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 用户名 |
| `password` | `VARCHAR(100)` | `NOT NULL` | 密码（加密存储） |
| `email` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 邮箱 |
| `phone` | `VARCHAR(20)` | `UNIQUE` | 手机号码 |
| `real_name` | `VARCHAR(50)` | `NOT NULL` | 真实姓名 |
| `avatar_url` | `VARCHAR(255)` | | 头像URL |
| `role` | `ENUM('STUDENT', 'COUNSELOR', 'ADMIN')` | `NOT NULL DEFAULT 'STUDENT'` | 用户角色 |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | 账户状态 |
| `last_login` | `TIMESTAMP` | `NULL` | 最后登录时间 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 4. 学生信息表（student_profiles）

存储学生的详细个人信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 学生信息ID |
| `user_id` | `BIGINT` | `NOT NULL UNIQUE` | 关联用户表ID |
| `student_id` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 学号 |
| `department_id` | `BIGINT` | `NOT NULL` | 所属学院/部门ID |
| `grade` | `VARCHAR(50)` | `NOT NULL` | 年级 |
| `major` | `VARCHAR(100)` | `NOT NULL` | 专业 |
| `gender` | `ENUM('MALE', 'FEMALE', 'OTHER')` | | 性别 |
| `date_of_birth` | `DATE` | | 出生日期 |
| `emergency_contact` | `VARCHAR(100)` | | 紧急联系人 |
| `emergency_phone` | `VARCHAR(20)` | | 紧急联系电话 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 5. 咨询师信息表（counselor_profiles）

存储咨询师的专业信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 咨询师信息ID |
| `user_id` | `BIGINT` | `NOT NULL UNIQUE` | 关联用户表ID |
| `employee_id` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 工号 |
| `department_id` | `BIGINT` | | 所属部门ID |
| `specialty` | `VARCHAR(255)` | | 专业特长 |
| `qualification` | `VARCHAR(255)` | | 资质证书 |
| `experience_years` | `INT` | | 从业年限 |
| `bio` | `TEXT` | | 个人简介 |
| `available_days` | `VARCHAR(50)` | | 可预约日期（逗号分隔的星期几） |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 6. 预约时间段表（appointment_slots）

存储咨询师的可预约时间段信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 预约时间段ID |
| `counselor_id` | `BIGINT` | `NOT NULL` | 咨询师用户ID |
| `slot_date` | `DATE` | `NOT NULL` | 预约日期 |
| `start_time` | `TIME` | `NOT NULL` | 开始时间 |
| `end_time` | `TIME` | `NOT NULL` | 结束时间 |
| `status` | `ENUM('AVAILABLE', 'BOOKED', 'CANCELLED')` | `NOT NULL DEFAULT 'AVAILABLE'` | 状态 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 7. 咨询记录表（counseling_records）

存储咨询记录的基本信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 咨询记录ID |
| `student_id` | `BIGINT` | `NOT NULL` | 学生用户ID |
| `counselor_id` | `BIGINT` | `NOT NULL` | 咨询师用户ID |
| `type_id` | `BIGINT` | | 咨询类型ID |
| `title` | `VARCHAR(100)` | `NOT NULL` | 咨询标题 |
| `summary` | `VARCHAR(255)` | | 咨询摘要 |
| `content` | `TEXT` | `NOT NULL` | 咨询内容描述 |
| `status` | `ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')` | `NOT NULL DEFAULT 'PENDING'` | 咨询状态 |
| `appointment_time` | `TIMESTAMP` | `NULL` | 预约时间 |
| `actual_start_time` | `TIMESTAMP` | `NULL` | 实际开始时间 |
| `actual_end_time` | `TIMESTAMP` | `NULL` | 实际结束时间 |
| `notes` | `TEXT` | | 备注信息 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 8. 咨询会话记录表（counseling_sessions）

存储每次咨询会话的详细记录。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 会话记录ID |
| `record_id` | `BIGINT` | `NOT NULL` | 关联咨询记录ID |
| `session_number` | `INT` | `NOT NULL` | 会话序号 |
| `session_date` | `TIMESTAMP` | `NOT NULL` | 会话日期时间 |
| `duration` | `INT` | | 会话时长（分钟） |
| `session_content` | `TEXT` | | 会话内容记录 |
| `next_plan` | `TEXT` | | 下一步计划 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 9. 咨询反馈表（counseling_feedback）

存储咨询后的反馈评价信息。

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | 反馈记录ID |
| `record_id` | `BIGINT` | `NOT NULL` | 关联咨询记录ID |
| `rating` | `INT` | `NOT NULL CHECK (rating >= 1 AND rating <= 5)` | 评分（1-5星） |
| `feedback` | `TEXT` | | 反馈内容 |
| `anonymous` | `BOOLEAN` | `DEFAULT FALSE` | 是否匿名 |
| `counselor_response` | `TEXT` | | 咨询师回复 |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | `NULL ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

## 表间关系

1. **用户与个人信息的关系**：
   - `users`表与`student_profiles`表：一对一关系
   - `users`表与`counselor_profiles`表：一对一关系

2. **部门与人员的关系**：
   - `departments`表与`student_profiles`表：一对多关系
   - `departments`表与`counselor_profiles`表：一对多关系

3. **咨询相关关系**：
   - `counseling_types`表与`counseling_records`表：一对多关系
   - `users`表（学生）与`counseling_records`表：一对多关系
   - `users`表（咨询师）与`counseling_records`表：一对多关系
   - `counseling_records`表与`counseling_sessions`表：一对多关系
   - `counseling_records`表与`counseling_feedback`表：一对一关系
   - `users`表（咨询师）与`appointment_slots`表：一对多关系

## 索引优化

为提高查询性能，系统建立了以下索引：

| 索引名 | 表名 | 字段 | 类型 | 目的 |
| :--- | :--- | :--- | :--- | :--- |
| `idx_users_role` | `users` | `role` | 普通索引 | 加速按角色查询用户 |
| `idx_users_active` | `users` | `active` | 普通索引 | 加速按状态查询用户 |
| `idx_records_student` | `counseling_records` | `student_id` | 普通索引 | 加速查询学生的咨询记录 |
| `idx_records_counselor` | `counseling_records` | `counselor_id` | 普通索引 | 加速查询咨询师的咨询记录 |
| `idx_records_status` | `counseling_records` | `status` | 普通索引 | 加速按状态筛选咨询记录 |
| `idx_appointment_slots_counselor` | `appointment_slots` | `counselor_id` | 普通索引 | 加速查询咨询师的预约时间段 |
| `idx_appointment_slots_date` | `appointment_slots` | `slot_date` | 普通索引 | 加速按日期筛选预约时间段 |
| `idx_appointment_slots_status` | `appointment_slots` | `status` | 普通索引 | 加速按状态筛选预约时间段 |
| `unique_slot` | `appointment_slots` | `counselor_id, slot_date, start_time, end_time` | 唯一索引 | 确保时间段不重叠 |
| `unique_session` | `counseling_sessions` | `record_id, session_number` | 唯一索引 | 确保每个记录的会话序号唯一 |
| `unique_record_feedback` | `counseling_feedback` | `record_id` | 唯一索引 | 确保每条咨询记录只有一条反馈 |

## 初始化数据

数据库初始化脚本提供了以下示例数据：

1. **5个学院/部门**：计算机科学与技术学院、心理学系、经济管理学院、外国语学院、文学院
2. **6种咨询类型**：学业压力、人际关系、情绪管理、职业规划、自我成长、危机干预
3. **1个管理员用户**：username=admin, password=123456
4. **3个咨询师用户**：zhang_counselor, li_counselor, wang_counselor（密码均为123456）
5. **5个学生用户**：student1至student5（密码均为123456）
6. **咨询师详细信息**：包括专业特长、资质、工作经验等
7. **学生详细信息**：包括学号、学院、专业、联系方式等
8. **预约时间段**：为咨询师创建了未来7天的可预约时间段
9. **5条咨询记录**：不同状态和类型的咨询记录
10. **咨询会话记录**：详细的咨询过程记录
11. **咨询反馈**：学生对咨询服务的评价和反馈

## 使用说明

1. **数据库初始化**：
   - 运行`init.sql`脚本将自动创建数据库、表结构并插入示例数据
   - 数据库连接配置在`application.properties`文件中

2. **默认登录账号**：
   - 管理员：admin / 123456
   - 咨询师：zhang_counselor / 123456、li_counselor / 123456、wang_counselor / 123456
   - 学生：student1 / 123456、student2 / 123456等

3. **安全提示**：
   - 生产环境中请修改默认密码
   - 密码使用BCrypt算法加密存储

## 扩展建议

1. **未来可能的扩展**：
   - 添加在线咨询会话记录表（在线聊天记录）
   - 添加通知消息表
   - 添加数据统计分析相关表
   - 添加文件存储记录表（用于存储咨询相关文档）

2. **性能优化建议**：
   - 根据实际查询需求调整索引
   - 定期归档历史咨询记录
   - 考虑对大数据量表进行分区

3. **安全建议**：
   - 实施数据访问权限控制
   - 对敏感字段进行额外加密
   - 定期进行数据库备份