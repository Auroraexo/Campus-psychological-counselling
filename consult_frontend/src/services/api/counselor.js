import api from './index';

const buildPayload = (data = {}) => {
  const payload = {
    username: data.username,
    realName: data.realName,
    email: data.email,
    phone: data.phone,
    role: 'COUNSELOR',
    active: typeof data.active === 'boolean' ? data.active : true
  };
  
  if (data.password) {
    payload.password = data.password;
  }
  
  return payload;
};

// 咨询师管理相关API (基于用户API)
export const counselorAPI = {
  // 获取咨询师列表（分页、搜索、状态过滤）
  getAll: (params = {}) => {
    const { page, size, q, active } = params;
    const query = {
      role: 'COUNSELOR',
      page,
      size,
      q,
      active
    };
    // 移除 undefined 参数
    Object.keys(query).forEach(key => {
      if (query[key] === undefined || query[key] === null || query[key] === '') {
        delete query[key];
      }
    });
    return api.get('/api/users', { params: query });
  },
  
  // 根据ID获取咨询师
  getById: (id) => api.get(`/api/users/${id}`),
  
  // 创建咨询师
  create: (counselorData) => api.post('/api/users', buildPayload(counselorData)),
  
  // 更新咨询师
  update: (id, counselorData) => api.put(`/api/users/${id}`, buildPayload(counselorData)),
  
  // 更新咨询师状态（启用/禁用）
  updateStatus: (id, counselorData) => api.put(`/api/users/${id}`, buildPayload(counselorData))
};

export default counselorAPI;