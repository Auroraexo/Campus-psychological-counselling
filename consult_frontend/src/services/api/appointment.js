import api from './index';

// 预约管理相关API (基于预约时间段表)
export const appointmentAPI = {
  // 获取所有预约时间段（支持筛选参数）
  getAll: (params = {}) => {
    // 移除空值参数
    const cleanParams = {}
    Object.keys(params).forEach(key => {
      if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
        cleanParams[key] = params[key]
      }
    })
    
    console.log('调用预约时间段API，参数:', cleanParams)
    return api.get('/api/appointment-slots', { params: cleanParams })
      .then(response => {
        console.log('预约时间段API成功响应:', response)
        return response
      })
      .catch(error => {
        console.error('获取预约时间段失败:', error);
        console.error('错误详情:', {
          response: error.response,
          data: error.response?.data,
          status: error.response?.status
        })
        throw error; // 抛出错误，让调用者处理
      });
  },
  
  // 根据ID获取预约时间段
  getById: (id) => api.get(`/api/appointment-slots/${id}`),
  
  // 创建预约时间段
  create: (appointmentData) => {
    // 确保状态为AVAILABLE
    appointmentData.status = 'AVAILABLE';
    return api.post('/api/appointment-slots', appointmentData);
  },
  
  // 更新预约时间段
  update: (id, appointmentData) => api.put(`/api/appointment-slots/${id}`, appointmentData),
  
  // 删除预约时间段
  delete: (id) => api.delete(`/api/appointment-slots/${id}`),
  
  // 预约时间段（学生预约）
  book: (id, studentId) => api.post(`/api/appointment-slots/${id}/book`, null, { params: { studentId } }),
  
  // 取消预约
  cancel: (id) => api.post(`/api/appointment-slots/${id}/cancel`)
};

export default appointmentAPI;