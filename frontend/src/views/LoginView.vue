<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { usersApi } from '../api/users'
import logoNayax from '../assets/logo-nayax.svg'

const auth = useAuthStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')
const loading = ref(false)

const isFormValid = computed(() => username.value.trim() !== '' && password.value !== '')

async function submit() {
  if (!isFormValid.value || loading.value) return

  error.value = ''
  loading.value = true
  auth.login(username.value.trim(), password.value)

  try {
    // O Basic não tem endpoint de login: a primeira chamada autenticada é o que valida a
    // credencial.
    await usersApi.list({ size: 1 })
    auth.confirmSession()
    router.push({ name: 'users' })
  } catch {
    auth.logout()
    error.value = 'Usuário ou senha inválidos'
    password.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div
      class="glow"
      aria-hidden="true"
    />

    <div class="container">
      <div class="card">
        <div class="card-header">
          <h1>
            <img
              :src="logoNayax"
              alt="Nayax"
              class="logo"
            >
          </h1>
          <p>Informe suas credenciais para acessar o gerenciamento de usuários.</p>
        </div>

        <form
          class="form"
          @submit.prevent="submit"
        >
          <p
            v-if="error"
            class="dark-alert"
            role="alert"
          >
            <svg
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM9 9a1 1 0 012 0v4a1 1 0 11-2 0V9zm1-4a1 1 0 100 2 1 1 0 000-2z"
                clip-rule="evenodd"
              />
            </svg>
            {{ error }}
          </p>

          <div class="field">
            <label for="username">Usuário</label>
            <div class="input-group">
              <svg
                class="icon"
                viewBox="0 0 20 20"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  d="M10 10a4 4 0 100-8 4 4 0 000 8zm0 2c-3.3 0-6 2-6 4.5V18h12v-1.5c0-2.5-2.7-4.5-6-4.5z"
                />
              </svg>
              <input
                id="username"
                v-model="username"
                class="dark-control"
                placeholder="admin"
                autocomplete="username"
                autofocus
                required
              >
            </div>
          </div>

          <div class="field">
            <label for="password">Senha</label>
            <div class="input-group">
              <svg
                class="icon"
                viewBox="0 0 20 20"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  fill-rule="evenodd"
                  d="M10 1a4 4 0 00-4 4v2H5a2 2 0 00-2 2v7a2 2 0 002 2h10a2 2 0 002-2V9a2 2 0 00-2-2h-1V5a4 4 0 00-4-4zm2 6V5a2 2 0 10-4 0v2h4z"
                  clip-rule="evenodd"
                />
              </svg>
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                class="dark-control with-action"
                placeholder="Sua senha"
                autocomplete="current-password"
                required
              >
              <button
                type="button"
                class="toggle"
                :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                @click="showPassword = !showPassword"
              >
                <svg
                  v-if="showPassword"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    d="M3.3 2.3a1 1 0 011.4 0l12 12a1 1 0 01-1.4 1.4l-1.9-1.8A8.9 8.9 0 0110 15c-3.8 0-7-2.4-8.4-5 .7-1.3 1.8-2.5 3.1-3.4L3.3 3.7a1 1 0 010-1.4zm3.9 5.3l4.8 4.8a3 3 0 01-4.8-4.8z"
                  />
                  <path d="M10 5c3.8 0 7 2.4 8.4 5-.5 1-1.3 2-2.3 2.8L10.6 7.2A3 3 0 007.2 3.8 8.9 8.9 0 0110 5z" />
                </svg>
                <svg
                  v-else
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                  <path
                    fill-rule="evenodd"
                    d="M1.6 10C3 7.4 6.2 5 10 5s7 2.4 8.4 5c-1.4 2.6-4.6 5-8.4 5s-7-2.4-8.4-5zm8.4 3.5a3.5 3.5 0 100-7 3.5 3.5 0 000 7z"
                    clip-rule="evenodd"
                  />
                </svg>
              </button>
            </div>
          </div>

          <button
            type="submit"
            class="button button-primary submit"
            :disabled="loading || !isFormValid"
          >
            <span
              v-if="loading"
              class="spinner"
              aria-hidden="true"
            />
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </div>

      <p class="footer">
        Gerenciamento de usuários &middot; Acesso restrito (Teste Nayax)
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  flex: 1;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background: var(--ink-950);
  overflow: hidden;
}

.glow {
  position: absolute;
  top: -30%;
  left: 50%;
  translate: -50% 0;
  width: min(52rem, 120vw);
  aspect-ratio: 1;
  background: radial-gradient(circle, rgb(255 201 0 / 0.09) 0%, transparent 62%);
  pointer-events: none;
}

.container {
  position: relative;
  width: 100%;
  max-width: 38rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.logo {
  width: min(15rem, 65vw);
  height: auto;
}

.card {
  width: 100%;
  padding: clamp(1.75rem, 5vw, 2.5rem);
  background: var(--ink-900);
  border: 1px solid var(--ink-700);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.card-header {
  margin-bottom: 1.5rem;
  text-align: center;
}

.card-header h1 {
  display: flex;
  justify-content: center;
  margin: 0;
  line-height: 0;
}

.card-header p {
  margin: 0.875rem 0 0;
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--text-inverse-muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.125rem;
}

.field label {
  color: var(--text-inverse);
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.icon {
  position: absolute;
  left: 0.75rem;
  width: 1.125rem;
  height: 1.125rem;
  color: var(--text-inverse-muted);
  pointer-events: none;
}

.dark-control {
  font: inherit;
  width: 100%;
  height: 2.875rem;
  padding: 0 0.875rem 0 2.5rem;
  color: var(--text-inverse);
  background: var(--ink-950);
  border: 1px solid var(--ink-700);
  border-radius: var(--radius);
  transition: border-color var(--duration) var(--ease), box-shadow var(--duration) var(--ease);
}

.dark-control.with-action {
  padding-right: 2.75rem;
}

.dark-control::placeholder {
  color: #7d7d86;
}

.dark-control:hover {
  border-color: var(--ink-600);
}

.dark-control:focus-visible {
  outline: none;
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgb(255 201 0 / 0.25);
}

.toggle {
  position: absolute;
  right: 0.5rem;
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  padding: 0;
  color: var(--text-inverse-muted);
  background: none;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: color var(--duration) var(--ease);
}

.toggle svg {
  width: 1.125rem;
  height: 1.125rem;
}

.toggle:hover {
  color: var(--text-inverse);
}

.toggle:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 1px;
}

.dark-alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  padding: 0.625rem 0.875rem;
  font-size: 0.875rem;
  color: var(--danger-on-dark);
  background: var(--danger-surface-dark);
  border: 1px solid rgb(220 38 38 / 0.3);
  border-radius: var(--radius);
}

.dark-alert svg {
  flex-shrink: 0;
  width: 1.125rem;
  height: 1.125rem;
}

.submit {
  width: 100%;
  height: 2.875rem;
  margin-top: 0.25rem;
  font-size: 0.9375rem;
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid rgb(20 20 20 / 0.25);
  border-top-color: var(--text-on-brand);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    rotate: 360deg;
  }
}

.footer {
  margin: 0;
  font-size: 0.75rem;
  color: #6b6b74;
}
</style>
