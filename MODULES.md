## 校园心理咨询系统功能模块与代码对应说明

本文档汇总课程设计报告中提到的几个核心功能模块，并给出在本项目中的**具体代码位置**，方便撰写文档或查阅源码。

---

## 1. 混合访问控制模块实现

综合使用：**JWT + Spring Security 角色 + 方法级 @PreAuthorize 注解** 实现精细化访问控制。

- **安全配置与过滤器**
  - 后端目录：`counselling_backend/src/main/java/com/example/counselling_backend`
  - 关键文件：
    - `config/SecurityConfig.java`  
      - 注册 `BCryptPasswordEncoder`、`AuthenticationManager`  
      - 配置哪些接口匿名访问，哪些需要登录/指定角色  
      - 将 `JwtAuthenticationFilter` 加入过滤器链  
    - `security/JwtAuthenticationFilter.java`  
      - 从 `Authorization: Bearer <token>` 中解析 JWT  
      - 校验用户状态（是否禁用）、加载用户详情，写入 `SecurityContext`

- **方法级权限控制（基于角色的访问控制）**
  - 使用 `@PreAuthorize` 实现细粒度权限：
    - `controller/UserController.java`  
      - 用户列表、创建/修改用户、查询学生列表等接口  
      - 示例：`@PreAuthorize("hasRole('ADMIN') or hasRole('COUNSELOR') or hasRole('STUDENT')")`
    - `controller/AppointmentSlotController.java`  
      - 学生、咨询师、管理员对预约时间段的不同操作权限  
    - `controller/CounselingRecordController.java`  
      - 咨询记录的查询、创建、更新、删除权限控制  
    - `controller/CounselingSessionController.java`  
      - 咨询会话记录的增删改查与角色限制  
    - `controller/CounselingFeedbackController.java`  
      - 学生填写反馈、咨询师回复反馈、管理员查看统计等

---

## 2. 数据加密模块实现（密码加密与历史密码策略）

主要完成：**密码加密存储 + 历史密码校验 + 密码变更记录**。

- **密码加密算法配置**
  - `config/SecurityConfig.java`
    - 定义 `BCryptPasswordEncoder` Bean（强度 12），全局统一使用。

- **用户注册 / 修改密码流程中的加密逻辑**
  - `service/UserService.java`
    - 注册用户时使用 `passwordEncoder.encode(rawPassword)` 进行加密后再写入 `users.password`。  
    - 修改密码时，先按策略校验，再加密新密码并保存。

- **密码历史记录与策略**
  - 模型与仓库：
    - `model/PasswordHistory.java`
    - `repository/PasswordHistoryRepository.java`
  - 策略服务：
    - `service/PasswordPolicyService.java`
      - `validatePasswordHistory(...)`：校验新密码是否与最近历史密码重复。  
      - `savePasswordHistory(user, hashedPassword)`：在用户注册或修改密码后保存一条历史记录。
  - 相关调用点：
    - `service/AuthService.java`：注册时保存初始密码历史。  
    - `service/UserService.java`：密码修改时校验并写入历史。

- **辅助控制器（演示与测试）**
  - `controller/PasswordUpdateController.java`：提供修改密码接口。  
  - `controller/PasswordTestController.java`：用于测试或演示密码加密结果。

---

## 3. 预约管理模块实现

实现学生网上预约、咨询师配置可预约时间段、管理员管理预约等功能。

- **后端：预约时间段业务**
  - 实体与仓库：
    - `model/AppointmentSlot.java`
    - `repository/AppointmentSlotRepository.java`  
      - 根据咨询师 ID、日期、状态等多条件查询预约时间段。
  - 业务服务：
    - `service/AppointmentSlotService.java`  
      - 获取全部/单个预约时间段  
      - 管理员 / 咨询师创建和修改时间段  
      - 学生预约、取消预约
  - 控制器：
    - `controller/AppointmentSlotController.java`  
      - `/api/appointments/...` 系列接口  
      - 使用 `@PreAuthorize` 区分管理员、咨询师、学生可执行的操作。

- **前端：预约管理界面**
  - 视图页面：
    - `consult_frontend/src/views/Appointments.vue`  
      - 标题为“预约管理”  
      - 条件筛选：咨询师、日期、状态（可预约 / 已预约）  
      - 列表展示：咨询师姓名、学生姓名、日期、时间段、状态等  
      - 对话框：创建 / 编辑预约时间段  
  - 导航入口：
    - `consult_frontend/src/App.vue`  
      - 侧边栏中“预约管理”菜单项  
    - `consult_frontend/src/views/Home.vue`  
      - 首页卡片中进入预约管理的入口。

---

## 4. 登录与权限验证模块实现

功能：**用户登录 / 注册、Token 签发与校验、前后端会话维护**。

- **后端：认证与授权**
  - 控制器：
    - `controller/AuthController.java`  
      - `/api/auth/login`：用户名 + 密码登录，返回 JWT 与用户信息。  
      - `/api/auth/register`：用户注册（学生 / 咨询师）。
  - 服务：
    - `service/AuthService.java`  
      - 校验用户名密码（结合 `UserDetailsService` 与 `BCryptPasswordEncoder`）  
      - 登录成功后生成 JWT，封装为 `AuthResponse` 返回给前端。
    - `service/CustomUserDetailsService`（实际类名以代码为准）  
      - 实现 `UserDetailsService` 接口，从 `UserRepository` 加载用户、角色和状态。
  - 安全基础设施：
    - `config/SecurityConfig.java`：配置 Spring Security 及过滤器链。  
    - `security/JwtAuthenticationFilter.java`：校验 Token、处理过期或禁用用户。

- **前端：登录 / 注册 / 会话管理**
  - 页面：
    - `consult_frontend/src/views/Login.vue`  
      - 登录表单，调用后端 `/api/auth/login`，保存返回的 Token 和用户信息。  
    - `consult_frontend/src/views/Register.vue`  
      - 注册表单，支持选择角色（学生 / 咨询师），成功后跳转登录页。
  - 布局与退出：
    - `consult_frontend/src/App.vue`  
      - 登录 / 注册页使用全屏布局，其它页使用带侧边栏的主布局。  
      - 提供“退出登录”按钮，清除本地 Token 并跳回登录页。  
      - 一般配合路由守卫控制未登录用户不能访问业务页面（可在 `router` 中查看实际逻辑）。

---

## 5. 咨询师管理模块实现

实现对咨询师基本信息、所属学院、专业方向等的管理和展示，并在预约、记录、反馈等模块中复用。

- **后端**
  - 实体与仓库：
    - `model/CounselorProfile.java`
    - `repository/CounselorProfileRepository.java`
  - 相关业务逻辑：
    - `controller/UserController.java`  
      - 获取用户列表，支持按角色过滤（如只查询 `COUNSELOR`）。  
    - `service/CounselingRecordService.java`、`AppointmentSlotService.java`  
      - 多处包含“如果当前用户是咨询师，只能操作自己的记录 / 时间段”的权限校验逻辑。

- **前端**
  - 菜单与页面：
    - `consult_frontend/src/App.vue`  
      - 侧边栏中“咨询师管理”菜单项。  
    - `consult_frontend/src/views/Users.vue`  
      - 用户管理界面，可筛选角色为“咨询师”查看咨询师列表及基本信息。
  - 业务页面中的咨询师信息：
    - `consult_frontend/src/views/Appointments.vue`  
      - 预约管理界面中的“选择咨询师”、“咨询师姓名”列。  
    - `consult_frontend/src/views/Records.vue`、`UserDetail.vue`、`Feedback.vue`、`Sessions.vue`  
      - 搜索或展示咨询师姓名、专业方向、回复内容等信息。

---

## 6. 系统管理界面与系统界面展示

整体 UI 使用 Vue 2 + Element UI 实现，前端负责系统界面展示和模块入口导航。

- **全局布局与导航**
  - `consult_frontend/src/App.vue`  
    - 顶部标题：`校园心理咨询系统`。  
    - 左侧菜单：预约管理、咨询师管理、用户 / 记录 / 反馈等子模块入口。  
    - 根据当前路由判断是否显示主布局（登录 / 注册页使用全屏布局）。

- **首页与系统说明**
  - `consult_frontend/src/views/Home.vue`  
    - 副标题：`校园心理咨询管理系统`。  
    - 展示系统简介，常用功能入口卡片（如预约管理、咨询记录、咨询师管理等）。

- **其他业务视图**
  - 具体功能模块页面：
    - `Appointments.vue`、`Records.vue`、`Sessions.vue`、`Feedback.vue`、`Users.vue`、`UserDetail.vue` 等  
    - 共同构成系统的业务界面展示，用于在报告中配合截图说明各模块实现效果。

---

## 使用建议

- 撰写课程设计报告时，可以直接引用本文件的模块划分和“代码位置”说明。  
- 如需补充每个模块的**关键代码片段**，可在报告中从对应文件中截取控制器方法、Service 核心逻辑或前端组件模板 / 脚本片段进行说明。  
- 如果后续对代码结构有较大调整，建议同步更新本 `MODULES.md`，保持文档与实现一致。

