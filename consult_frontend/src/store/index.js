import Vue from 'vue'
import Vuex from 'vuex'
import { userAPI, recordAPI } from '../services/api'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    // 用户认证状态
    isAuthenticated: false,
    currentUser: null,
    token: null,
    
    // 加载状态
    loading: {
      users: false,
      records: false,
      auth: false
    },
    
    // 数据
    users: [],
    usersTotal: 0,
    usersPage: 0,
    usersSize: 10,
    records: []
  },
  
  getters: {
    // 认证相关
    isAuthenticated: state => state.isAuthenticated,
    currentUser: state => state.currentUser,
    token: state => state.token,
    isAdmin: state => state.currentUser && state.currentUser.role === 'ADMIN',
    isCounselor: state => state.currentUser && state.currentUser.role === 'COUNSELOR',
    isStudent: state => state.currentUser && state.currentUser.role === 'STUDENT',
    
    // 加载状态
    isLoading: state => type => state.loading[type] || false,
    
    // 数据
    getUsers: state => state.users,
    getUsersTotal: state => state.usersTotal,
    getUsersPage: state => state.usersPage,
    getUsersSize: state => state.usersSize,
    getRecords: state => state.records,
    getCounselingRecords: state => state.records,
    
    // 过滤后的数据
    getStudents: state => state.users.filter(user => user.role === 'STUDENT'),
    getCounselors: state => state.users.filter(user => user.role === 'COUNSELOR')
  },
  
  mutations: {
    // 认证相关
    SET_AUTHENTICATED(state, isAuthenticated) {
      state.isAuthenticated = isAuthenticated
    },
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
    },
    SET_TOKEN(state, token) {
      state.token = token
    },
    
    // 加载状态
    SET_LOADING(state, { type, status }) {
      state.loading[type] = status
    },
    
    // 用户数据
    SET_USERS(state, users) {
      state.users = users
    },
    SET_USERS_PAGINATION(state, { total, page, size }) {
      state.usersTotal = total
      state.usersPage = page
      state.usersSize = size
    },
    ADD_USER(state, user) {
      state.users.push(user)
    },
    UPDATE_USER(state, updatedUser) {
      const index = state.users.findIndex(user => user.id === updatedUser.id)
      if (index !== -1) {
        state.users.splice(index, 1, updatedUser)
      }
    },
    REMOVE_USER(state, userId) {
      state.users = state.users.filter(user => user.id !== userId)
    },
    
    // 咨询记录数据
    SET_RECORDS(state, records) {
      state.records = records
    },
    ADD_RECORD(state, record) {
      state.records.push(record)
    },
    UPDATE_RECORD(state, updatedRecord) {
      const index = state.records.findIndex(record => record.id === updatedRecord.id)
      if (index !== -1) {
        state.records.splice(index, 1, updatedRecord)
      }
    },
    REMOVE_RECORD(state, recordId) {
      state.records = state.records.filter(record => record.id !== recordId)
    }
  },
  
  actions: {
    // 登录
    login({ commit }, credentials) {
      commit('SET_LOADING', { type: 'auth', status: true })
      console.log('登录请求参数:', credentials)
      return userAPI.login(credentials)
        .then(response => {
          console.log('登录响应数据:', response)
          // 从后端响应中提取token和用户信息
          const token = response.token
          // 构建user对象，包含所需的用户信息
          const user = {
            id: response.id,
            username: response.username,
            email: response.email,
            realName: response.realName,
            role: response.role
          }
          // 保存token和用户信息到localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          // 更新Vuex状态
          commit('SET_TOKEN', token)
          commit('SET_CURRENT_USER', user)
          commit('SET_AUTHENTICATED', true)
          
          return response
        })
        .catch(error => {
          // 处理登录错误
          console.error('登录失败错误详情:', error)
          if (error.response) {
            console.error('响应状态:', error.response.status)
            console.error('响应数据:', error.response.data)
          }
          throw error.response ? error.response.data : new Error('未知错误')
        })
        .finally(() => {
          commit('SET_LOADING', { type: 'auth', status: false })
        })
    },
    
    // 注册
    register({ commit }, userData) {
      commit('SET_LOADING', { type: 'auth', status: true })
      return userAPI.register(userData)
        .finally(() => {
          commit('SET_LOADING', { type: 'auth', status: false })
        })
    },
    
    // 登出
    logout({ commit }) {
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      // 更新Vuex状态
      commit('SET_TOKEN', null)
      commit('SET_CURRENT_USER', null)
      commit('SET_AUTHENTICATED', false)
    },
    
    // 初始化认证状态（从localStorage恢复）
    initAuth({ commit }) {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      
      if (token && user) {
        commit('SET_TOKEN', token)
        commit('SET_CURRENT_USER', JSON.parse(user))
        commit('SET_AUTHENTICATED', true)
      }
    },
    
    // 获取用户（服务端分页/筛选）
    fetchUsers({ commit, state }, { page = state.usersPage, size = state.usersSize, role = '', q = '' } = {}) {
      commit('SET_LOADING', { type: 'users', status: true })
      return userAPI.getAll({ page, size, role: role || undefined, q: q || undefined })
        .then(pageData => {
          const users = pageData.content || []
          const total = pageData.totalElements || users.length
          const currentPage = pageData.number != null ? pageData.number : page
          const pageSize = pageData.size != null ? pageData.size : size
          commit('SET_USERS', users)
          commit('SET_USERS_PAGINATION', { total, page: currentPage, size: pageSize })
          return pageData
        })
        .catch(error => {
          console.error('获取用户列表失败:', error)
          // 如果API调用失败，使用模拟数据作为后备
          const mockUsers = [
            {
              id: 1,
              username: 'admin',
              email: 'admin@example.com',
              realName: '系统管理员',
              role: 'ADMIN'
            },
            {
              id: 2,
              username: 'counselor1',
              email: 'counselor1@example.com',
              realName: '张老师',
              role: 'COUNSELOR'
            },
            {
              id: 3,
              username: 'counselor2',
              email: 'counselor2@example.com',
              realName: '李老师',
              role: 'COUNSELOR'
            },
            {
              id: 4,
              username: 'student1',
              email: 'student1@example.com',
              realName: '王同学',
              role: 'STUDENT'
            },
            {
              id: 5,
              username: 'student2',
              email: 'student2@example.com',
              realName: '赵同学',
              role: 'STUDENT'
            },
            {
              id: 6,
              username: 'student3',
              email: 'student3@example.com',
              realName: '刘同学',
              role: 'STUDENT'
            }
          ]
          commit('SET_USERS', mockUsers)
          commit('SET_USERS_PAGINATION', { total: mockUsers.length, page: 0, size })
          return { content: mockUsers, totalElements: mockUsers.length, number: 0, size }
        })
        .finally(() => {
          commit('SET_LOADING', { type: 'users', status: false })
        })
    },
    
    // 根据用户名获取用户
    fetchUserByUsername(_, username) {
      return userAPI.getByUsername(username)
        .then(user => {
          return user
        })
    },
    
    // 创建用户
    createUser({ commit }, userData) {
      return userAPI.create(userData)
        .then(user => {
          commit('ADD_USER', user)
          return user
        })
    },
    
    // 更新用户
    updateUser({ commit }, { id, user }) {
      return userAPI.update(id, user)
        .then(updatedUser => {
          commit('UPDATE_USER', updatedUser)
          // 如果更新的是当前用户，也要更新当前用户信息
          if (updatedUser.id === this.getters.currentUser?.id) {
            localStorage.setItem('user', JSON.stringify(updatedUser))
            commit('SET_CURRENT_USER', updatedUser)
          }
          return updatedUser
        })
    },
    
    // 删除用户
    deleteUser({ commit }, userId) {
      return userAPI.delete(userId)
        .then(() => {
          commit('REMOVE_USER', userId)
        })
    },
    
    // 获取所有咨询记录
    fetchRecords({ commit }) {
      commit('SET_LOADING', { type: 'records', status: true })
      return recordAPI.getAll()
        .then(records => {
          commit('SET_RECORDS', records)
          return records
        })
        .catch(error => {
          console.error('获取咨询记录失败:', error)
          // 如果API调用失败，使用模拟数据作为后备
          const mockRecords = [
            {
              id: 1,
              studentId: 4,
              counselorId: 2,
              appointmentTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
              status: 'COMPLETED',
              summary: '学业压力大，情绪焦虑',
              content: '学生反映近期学业压力较大，出现焦虑情绪，建议适当放松，制定合理的学习计划。',
              createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
            },
            {
              id: 2,
              studentId: 5,
              counselorId: 3,
              appointmentTime: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000).toISOString(),
              status: 'PENDING',
              summary: '人际关系困扰',
              content: '学生表示在宿舍人际关系中遇到困扰，需要进一步沟通和指导。',
              createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
            },
            {
              id: 3,
              studentId: 6,
              counselorId: 2,
              appointmentTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
              status: 'IN_PROGRESS',
              summary: '职业规划迷茫',
              content: '学生对未来职业规划感到迷茫，需要进行职业兴趣测试和规划指导。',
              createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
            },
            {
              id: 4,
              studentId: 4,
              counselorId: 3,
              appointmentTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
              status: 'COMPLETED',
              summary: '家庭关系问题',
              content: '学生与父母沟通不畅，产生矛盾，建议加强家庭沟通，理解彼此的立场。',
              createdAt: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString()
            },
            {
              id: 5,
              studentId: 5,
              counselorId: 2,
              appointmentTime: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
              status: 'PENDING',
              summary: '情绪管理问题',
              content: '学生表示容易情绪波动，需要学习情绪管理技巧。',
              createdAt: new Date(Date.now()).toISOString()
            }
          ]
          commit('SET_RECORDS', mockRecords)
          return mockRecords
        })
        .finally(() => {
          commit('SET_LOADING', { type: 'records', status: false })
        })
    },
    
    // 创建咨询记录
    createRecord({ commit }, recordData) {
      return recordAPI.create(recordData)
        .then(record => {
          commit('ADD_RECORD', record)
          return record
        })
    },
    
    // 更新咨询记录
    updateRecord({ commit }, { id, record }) {
      return recordAPI.update(id, record)
        .then(updatedRecord => {
          commit('UPDATE_RECORD', updatedRecord)
          return updatedRecord
        })
    },
    
    // 删除咨询记录
    deleteRecord({ commit }, recordId) {
      return recordAPI.delete(recordId)
        .then(() => {
          commit('REMOVE_RECORD', recordId)
        })
    }
  }
})