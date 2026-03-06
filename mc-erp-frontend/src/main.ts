import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { setupVueErrorHandler } from './utils/globalErrorHandler'

const app = createApp(App)

// Register Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(router)
app.use(ElementPlus, { locale: zhCn })
setupVueErrorHandler(app, {
    env: import.meta.env.DEV ? 'development' : 'production',
    useModal: true
})
app.mount('#app')
