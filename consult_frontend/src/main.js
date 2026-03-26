import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false

// 使用Element UI
Vue.use(ElementUI)

// 初始化认证状态
store.dispatch('initAuth')

new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')
