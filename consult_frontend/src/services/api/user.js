import api from './index';

// 用户相关API
export const userAPI = {
  // 登录
  login: (credentials) => api.post('/api/auth/login', credentials),
  
  // 注册
  register: (userData) => api.post('/api/auth/register', userData),
  
  // 获取用户（分页/搜索/角色过滤）
  // params: { page, size, role, q }
  getAll: (params = {}) => api.get('/api/users', { params }),
  
  // 根据ID获取用户
  getById: (id) => api.get(`/api/users/${id}`),
  
  // 创建用户
  create: (userData) => api.post('/api/users', userData),
  
  // 更新用户
  update: (id, userData) => api.put(`/api/users/${id}`, userData),
  
  // 删除用户
  delete: (id) => api.delete(`/api/users/${id}`),
  
  // 根据角色获取用户
  getByRole: (role) => api.get(`/api/users/role/${role}`),
  
  // 根据用户名获取用户
  getByUsername: (username) => api.get(`/api/users/username/${username}`)
};

export default userAPI;