import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * Credenciais da sessão, mantidas apenas em memória. No localStorage, a credencial de acesso
 * total à API ficaria exposta a qualquer XSS. A sessão se perde ao recarregar.
 */
export const useAuthStore = defineStore('auth', () => {
  const username = ref<string | null>(null)
  const password = ref<string | null>(null)
  const sessionConfirmed = ref(false)

  const hasCredentials = computed(() => username.value !== null && password.value !== null)

  const isAuthenticated = computed(() => hasCredentials.value && sessionConfirmed.value)

  const authorizationHeader = computed(() =>
    hasCredentials.value ? `Basic ${btoa(`${username.value}:${password.value}`)}` : null
  )

  function login(user: string, pass: string) {
    username.value = user
    password.value = pass
    sessionConfirmed.value = false
  }

  function confirmSession() {
    sessionConfirmed.value = true
  }

  function logout() {
    username.value = null
    password.value = null
    sessionConfirmed.value = false
  }

  return { username, isAuthenticated, authorizationHeader, login, confirmSession, logout }
})
