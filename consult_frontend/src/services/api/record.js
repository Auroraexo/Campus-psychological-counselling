import api from './index';

// 咨询记录相关API
export const recordAPI = {
  // 获取所有咨询记录（支持分页、搜索、状态筛选）
  getAll: (params = {}) => {
    const { page, size, status, q } = params;
    const query = { page, size, status, q };
    // 移除 undefined 参数
    Object.keys(query).forEach(key => {
      if (query[key] === undefined || query[key] === null || query[key] === '') {
        delete query[key];
      }
    });
    return api.get('/api/counseling/records', { params: query })
      .catch(error => {
        console.error('获取所有咨询记录失败:', error);
        // 如果请求失败，返回空的分页结构
        if (page !== undefined && size !== undefined) {
          return { content: [], totalElements: 0, number: 0, size };
        }
        return []; // 出错时返回空数组（向后兼容）
      });
  },
  
  // 根据ID获取咨询记录
  getById: (id) => api.get(`/api/counseling/records/${id}`),
  
  // 根据学生ID获取咨询记录
  getByStudentId: (studentId) => api.get(`/api/counseling/records/student/${studentId}`),
  
  // 根据咨询师ID获取咨询记录
  getByCounselorId: (counselorId) => api.get(`/api/counseling/records/counselor/${counselorId}`),
  
  // 根据状态获取咨询记录
  getByStatus: (status) => {
    // 获取所有咨询记录，然后过滤出指定状态的记录
    return api.get('/api/counseling/records')
      .then(records => records.filter(record => record.status === status))
      .catch(error => {
        console.error('获取指定状态的咨询记录失败:', error);
        return []; // 出错时返回空数组
      });
  },
  
  // 创建咨询记录
  create: (recordData) => api.post('/api/counseling/records', recordData),
  
  // 更新咨询记录
  update: (id, recordData) => api.put(`/api/counseling/records/${id}`, recordData),
  
  // 删除咨询记录
  delete: (id) => api.delete(`/api/counseling/records/${id}`)
};

export default recordAPI;