# Campus Psychological Counselling

校园心理咨询系统项目仓库，包含前端界面、后端服务以及若干数据库和运维辅助脚本。

## 项目结构

```text
.
├─ consult_frontend/       # 前端项目
├─ counselling_backend/    # Spring Boot 后端服务
├─ MODULES.md              # 功能模块与代码位置说明
├─ *.sql                   # 数据修复、初始化与密码相关脚本
└─ test_login.js           # 登录测试脚本
```

## 核心功能

- 用户登录、注册与 JWT 认证
- 基于 Spring Security 的角色权限控制
- 心理咨询预约管理
- 咨询记录、咨询会话与反馈管理
- 密码加密、修改与历史密码校验

## 后端技术栈

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- Redis
- JWT

## 相关文档

- [MODULES.md](./MODULES.md): 功能模块与代码位置说明
- [counselling_backend/src/main/resources/database_structure.md](./counselling_backend/src/main/resources/database_structure.md): 数据库结构说明
- [counselling_backend/src/main/resources/counselling_db.sql](./counselling_backend/src/main/resources/counselling_db.sql): 数据库脚本

## 说明

当前仓库同时保留 `main` 与 `lime` 两个分支。若想查看最新代码，请优先切换到 `main` 分支。
