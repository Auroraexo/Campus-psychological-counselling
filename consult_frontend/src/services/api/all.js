// 统一导出所有API模块
export { default as userAPI } from './user';
export { default as recordAPI } from './record';
export { default as counselorAPI } from './counselor';
export { default as studentAPI } from './student';
export { default as appointmentAPI } from './appointment';
export { default as sessionAPI } from './session';
export { default as feedbackAPI } from './feedback';

// 导入所有API模块用于默认导出
import userAPI from './user';
import recordAPI from './record';
import counselorAPI from './counselor';
import studentAPI from './student';
import appointmentAPI from './appointment';
import sessionAPI from './session';
import feedbackAPI from './feedback';

// 默认导出所有API模块
export default {
  userAPI,
  recordAPI,
  counselorAPI,
  studentAPI,
  appointmentAPI,
  sessionAPI,
  feedbackAPI
};