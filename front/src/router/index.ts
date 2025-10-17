import { createRouter, createWebHistory } from 'vue-router'

const HomePage = () => import('@/views/Home.vue')
const ChatPage = () => import('@/views/ChatSSE.vue')
const ManusPage = () => import('@/views/ChatManus.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/chat', component: ChatPage },
    { path: '/manus', component: ManusPage }
  ]
})

export default router

