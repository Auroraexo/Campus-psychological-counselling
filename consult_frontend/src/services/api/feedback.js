import api from './index';

// 反馈管理相关API
export const feedbackAPI = {
  // 获取所有反馈记录（支持分页、搜索、评分筛选、匿名筛选）
  getAll: (params = {}) => {
    const { page, size, rating, anonymous, q } = params;
    const query = { page, size, rating, anonymous, q };
    // 移除 undefined 参数
    Object.keys(query).forEach(key => {
      if (query[key] === undefined || query[key] === null || query[key] === '') {
        delete query[key];
      }
    });
    return api.get('/api/counseling/feedback', { params: query })
      .catch(error => {
        console.error('获取所有反馈记录失败:', error);
        if (page !== undefined && size !== undefined) {
          return { content: [], totalElements: 0, number: 0, size };
        }
        return [];
      });
  },
  
  // 根据ID获取反馈记录
  getById: (id) => api.get(`/api/counseling/feedback/${id}`),
  
  // 根据咨询记录ID获取反馈记录
  getByRecordId: (recordId) => api.get(`/api/counseling/feedback/record/${recordId}`),
  
  // 创建反馈记录
  create: (feedbackData) => api.post('/api/counseling/feedback', feedbackData),
  
  // 更新反馈记录
  update: (id, feedbackData) => api.put(`/api/counseling/feedback/${id}`, feedbackData),
  
  // 更新咨询师回复
  updateResponse: (id, counselorResponse) => api.put(`/api/counseling/feedback/${id}/response`, { counselorResponse }),
  
  // 删除反馈记录
  delete: (id) => api.delete(`/api/counseling/feedback/${id}`)
};

export default feedbackAPI;
