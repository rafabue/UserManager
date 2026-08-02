<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usersApi } from '../api/users'
import { ApiError, type FieldError } from '../api/client'

const props = defineProps<{ id?: string }>()

const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const fieldErrors = ref<FieldError[]>([])
const saving = ref(false)
const loading = ref(false)

const isEdit = computed(() => props.id !== undefined)

const title = computed(() => (isEdit.value ? 'Editar usuário' : 'Novo usuário'))

const subtitle = computed(() =>
  isEdit.value
    ? 'Alterar nome ou e-mail dispara uma notificação para o usuário.'
    : 'O usuário recebe uma notificação por e-mail assim que o cadastro é concluído.'
)

function fieldError(field: string) {
  return fieldErrors.value.find((e) => e.field === field)?.message
}

function cancel() {
  router.push({ name: 'users' })
}

onMounted(async () => {
  if (!props.id) return
  loading.value = true
  try {
    const user = await usersApi.findById(Number(props.id))
    name.value = user.name
    email.value = user.email
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o usuário'
  } finally {
    loading.value = false
  }
})

async function save() {
  error.value = ''
  fieldErrors.value = []
  saving.value = true

  try {
    if (isEdit.value) {
      await usersApi.update(Number(props.id), { name: name.value, email: email.value })
    } else {
      await usersApi.create({ name: name.value, email: email.value, password: password.value })
    }
    router.push({ name: 'users' })
  } catch (e) {
    if (e instanceof ApiError) {
      error.value = e.message
      fieldErrors.value = e.fieldErrors
    } else {
      error.value = 'Não foi possível salvar o usuário'
    }
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="title">
      <h1>{{ title }}</h1>
      <p>{{ subtitle }}</p>
    </div>

    <form
      class="panel form"
      @submit.prevent="save"
    >
      <p
        v-if="error"
        class="alert"
        role="alert"
      >
        {{ error }}
      </p>

      <div class="grid">
        <div class="field">
          <label for="name">Nome</label>
          <input
            id="name"
            v-model="name"
            class="control"
            placeholder="Nome completo"
            :aria-invalid="fieldError('name') ? 'true' : undefined"
            :disabled="loading"
            required
          >
          <span
            v-if="fieldError('name')"
            class="field-error"
          >{{ fieldError('name') }}</span>
        </div>

        <div class="field">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="email"
            type="email"
            class="control"
            placeholder="nome@empresa.com"
            :aria-invalid="fieldError('email') ? 'true' : undefined"
            :disabled="loading"
            required
          >
          <span
            v-if="fieldError('email')"
            class="field-error"
          >{{ fieldError('email') }}</span>
        </div>

        <div
          v-if="!isEdit"
          class="field"
        >
          <label for="password">Senha</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="control"
            placeholder="Mínimo de 8 caracteres"
            minlength="8"
            :aria-invalid="fieldError('password') ? 'true' : undefined"
            required
          >
          <span
            v-if="fieldError('password')"
            class="field-error"
          >{{ fieldError('password') }}</span>
          <span
            v-else
            class="hint"
          >A senha é armazenada com hash e nunca retorna nas consultas.</span>
        </div>
      </div>

      <div class="actions">
        <button
          type="submit"
          class="button button-primary"
          :disabled="saving || loading"
        >
          {{ saving ? 'Salvando...' : 'Salvar' }}
        </button>
        <button
          type="button"
          class="button button-secondary"
          @click="cancel"
        >
          Cancelar
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.title h1 {
  font-size: 1.375rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.title p {
  margin: 0.375rem 0 0;
  font-size: 0.875rem;
  color: var(--text-muted);
  max-width: 65ch;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: clamp(1.25rem, 3vw, 1.75rem);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 18rem), 1fr));
  gap: 1.25rem;
}

.hint {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border);
}
</style>
