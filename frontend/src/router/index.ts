import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import UserListView from '../views/UserListView.vue'
import UserFormView from '../views/UserFormView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    { path: '/', name: 'users', component: UserListView },
    { path: '/users/new', name: 'user-new', component: UserFormView },
    { path: '/users/:id/edit', name: 'user-edit', component: UserFormView, props: true }
  ]
})

// sem credencial em memória toda chamada retorna 401, o guard vai evitar tela vazia levando direto ao login
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.name !== 'login' && !auth.isAuthenticated) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'users' }
  }
})

export default router
