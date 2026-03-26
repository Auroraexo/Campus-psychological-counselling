import api from './index';

const buildPayload = (data = {}) => {
  const payload = {
    username: data.username,
    realName: data.realName,
    email: data.email,
    phone: data.phone,
    role: 'STUDENT',
    active: typeof data.active === 'boolean' ? data.active : true
  };

  if (data.password) {
    payload.password = data.password;
  }

  return payload;
};

// 学生管理相关API (基于用户API)
export const studentAPI = {
  // 获取学生列表
  getAll: (params = {}) => {
    const { page, size, q, active } = params;
    const query = {
      page,
      size,
      q,
      active
    };
    Object.keys(query).forEach(key => {
      if (query[key] === undefined || query[key] === null || query[key] === '') {
        delete query[key];
      }
    });
    return api.get('/api/users/students', { params: query });
  },

  // 根据ID获取学生
  getById: (id) => api.get(`/api/users/${id}`),

  // 创建学生
  create: (studentData) => api.post('/api/users', buildPayload(studentData)),

  // 更新学生
  update: (id, studentData) => api.put(`/api/users/${id}`, buildPayload(studentData)),

  // 更新学生状态
  updateStatus: (id, studentData) => api.put(`/api/users/${id}`, buildPayload(studentData))
};

export default studentAPI;