<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import logoNayax from './assets/logo-nayax.svg'

const auth = useAuthStore()
const router = useRouter()

function signOut() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header
    v-if="auth.isAuthenticated"
    class="header"
  >
    <div class="header-inner">
      <RouterLink
        :to="{ name: 'users' }"
        class="brand"
        aria-label="Ir para a lista de usuários"
      >
        <img
          :src="logoNayax"
          alt="Nayax"
          class="logo"
        >
        <span
          class="divider"
          aria-hidden="true"
        />
        <span class="product">User Manager</span>
      </RouterLink>

      <div class="session">
        <span class="username">{{ auth.username }}</span>
        <button
          type="button"
          class="sign-out"
          @click="signOut"
        >
          Sair
        </button>
      </div>
    </div>
  </header>

  <main :class="auth.isAuthenticated ? 'content' : 'content-full'">
    <RouterView />
  </main>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--ink-950);
  border-bottom: 1px solid var(--ink-800);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  height: 3.75rem;
  padding-inline: clamp(1rem, 3vw, 2.5rem);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  text-decoration: none;
  min-width: 0;
}

.logo {
  height: 1.25rem;
  width: auto;
  display: block;
}

.divider {
  width: 1px;
  height: 1.25rem;
  background: var(--ink-600);
}

.product {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-inverse-muted);
  white-space: nowrap;
}

.session {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.875rem;
}

.username {
  color: var(--text-inverse);
  font-weight: 500;
}

.sign-out {
  font: inherit;
  font-size: 0.875rem;
  color: var(--text-inverse-muted);
  background: none;
  border: 1px solid var(--ink-700);
  border-radius: var(--radius-sm);
  padding: 0.375rem 0.75rem;
  cursor: pointer;
  transition: color var(--duration) var(--ease), border-color var(--duration) var(--ease);
}

.sign-out:hover {
  color: var(--text-inverse);
  border-color: var(--ink-600);
}

.sign-out:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 2px;
}

.content {
  flex: 1;
  width: 100%;
  padding: clamp(1.25rem, 3vw, 2rem) clamp(1rem, 3vw, 2.5rem) 3rem;
}

.content-full {
  flex: 1;
  display: flex;
}

@media (max-width: 30rem) {
  .divider,
  .product {
    display: none;
  }
}
</style>
