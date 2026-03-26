# API模块重构说明

## 目录结构

```
src/services/
├── api.js                 # 主API入口文件（重新导出所有API模块）
└── api/                   # API模块目录
    ├── index.js          # 基础API配置（axios实例、拦截器等）
    ├── all.js            # 统一导出所有API模块
    ├── user.js           # 用户相关API
    ├── record.js         # 咨询记录相关API
    ├── counselor.js      # 咨询师相关API
    ├── student.js        # 学生相关API
    ├── appointment.js     # 预约相关API
    ├── session.js        # 会话相关API
    └── feedback.js       # 反馈相关API
```

## 使用方式

### 方式一：使用主API入口文件（推荐）
```javascript
import { userAPI, recordAPI, counselorAPI } from '@/services/api';

// 使用API
userAPI.login(credentials);
recordAPI.getAll();
counselorAPI.getAll();
```

### 方式二：直接导入特定API模块
```javascript
import userAPI from '@/services/api/user';
import recordAPI from '@/services/api/record';

// 使用API
userAPI.login(credentials);
recordAPI.getAll();
```

### 方式三：导入所有API模块
```javascript
import api from '@/services/api';

// 使用API
api.userAPI.login(credentials);
api.recordAPI.getAll();
```

## 优势

1. **模块化**：每个功能模块的API分离到独立文件，便于维护
2. **可扩展性**：新增API模块只需添加新文件，不影响现有代码
3. **代码复用**：基础配置（axios实例、拦截器等）统一管理
4. **向后兼容**：原有的导入方式仍然有效，无需修改现有代码
5. **错误处理**：所有API方法都添加了错误处理，避免应用崩溃

## 注意事项

1. 所有API模块都基于相同的axios实例和拦截器配置
2. 错误处理已统一添加，API调用失败时会返回空数组或抛出错误
3. 某些API（如appointment、session、feedback）是基于recordAPI的封装，通过状态过滤实现
4. 修改API配置时，请修改 `api/index.js` 文件