// 导入所有API模块
import apiModules from './api/all';

// 重新导出所有API模块，保持向后兼容性
export const userAPI = apiModules.userAPI;
export const recordAPI = apiModules.recordAPI;
export const counselorAPI = apiModules.counselorAPI;
export const studentAPI = apiModules.studentAPI;
export const appointmentAPI = apiModules.appointmentAPI;
export const sessionAPI = apiModules.sessionAPI;
export const feedbackAPI = apiModules.feedbackAPI;

// 默认导出所有API模块
export default apiModules;