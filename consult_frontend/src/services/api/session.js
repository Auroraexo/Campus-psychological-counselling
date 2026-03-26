import api from './index';

// 会话管理相关API
export const sessionAPI = {
  // 获取所有会话记录（支持分页、搜索、记录筛选）
  getAll: (params = {}) => {
    const { page, size, recordId, q } = params;
    const query = { page, size, recordId, q };
    // 移除 undefined 参数
    Object.keys(query).forEach(key => {
      if (query[key] === undefined || query[key] === null || query[key] === '') {
        delete query[key];
      }
    });
    return api.get('/api/counseling/sessions', { params: query })
      .catch(error => {
        console.error('获取所有会话记录失败:', error);
        if (page !== undefined && size !== undefined) {
          return { content: [], totalElements: 0, number: 0, size };
        }
        return [];
      });
  },
  
  // 根据ID获取会话记录
  getById: (id) => api.get(`/api/counseling/sessions/${id}`),
  
  // 根据咨询记录ID获取会话记录
  getByRecordId: (recordId) => api.get(`/api/counseling/sessions/record/${recordId}`),
  
  // 创建会话记录
  create: (sessionData) => api.post('/api/counseling/sessions', sessionData),
  
  // 更新会话记录
  update: (id, sessionData) => api.put(`/api/counseling/sessions/${id}`, sessionData),
  
  // 删除会话记录
  delete: (id) => api.delete(`/api/counseling/sessions/${id}`)
};

export default sessionAPI;
