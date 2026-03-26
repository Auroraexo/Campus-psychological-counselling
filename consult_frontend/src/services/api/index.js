import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8082', // 后端API的基础URL，注意不包含/api前缀，因为控制器已包含
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加token
api.interceptors.request.use(
  config => {
    console.log('API请求配置:', config.method?.toUpperCase(), config.url);
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('已添加token到请求头');
    }
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => {
    console.log('API响应成功:', response.config?.method?.toUpperCase(), response.config?.url, '状态码:', response.status);
    return response.data;
  },
  error => {
    console.error('API响应错误:', error.config?.method?.toUpperCase(), error.config?.url);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误响应数据:', error.response.data);
      
      // 特别处理400错误，提供更详细的信息
      if (error.response.status === 400) {
        const errorData = error.response.data;
        const errorMessage = typeof errorData === 'string' ? errorData : errorData?.message || errorData?.error || '请求参数错误';
        console.error('400错误详细信息:', errorMessage);
      }
    } else if (error.request) {
      console.error('请求已发送但没有收到响应:', error.request);
    } else {
      console.error('请求配置错误:', error.message);
    }
    
    // 如果是401错误，说明token过期或无效，清除本地存储的token并跳转到登录页
    if (error.response && error.response.status === 401) {
      const errorMessage = error.response.data?.error || error.response.data?.message || '认证失败，请重新登录'
      console.error('认证失败:', errorMessage)
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // 跳转到登录页
      window.location.href = '/login';
    }
    // 确保错误对象始终包含message属性
    let errorData = error.response ? error.response.data : { message: '网络错误' };
    // 如果错误数据是字符串，将其转换为包含message的对象
    if (typeof errorData === 'string') {
      errorData = { message: errorData };
    }
    // 如果错误数据没有message属性，添加一个默认的错误消息
    if (!errorData.message && typeof errorData === 'object') {
      // 直接使用error或error.error作为消息，避免添加前缀
      errorData.message = errorData.error || error.message || '请求失败';
    }
    // 移除可能的重复前缀
    if (errorData.message && errorData.message.startsWith('登录失败:')) {
      errorData.message = errorData.message.substring(5); // 移除"登录失败:"前缀
    }
    return Promise.reject(errorData);
  }
);

export default api;