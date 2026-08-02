<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usersApi, type User, type Page } from '../api/users'
import { ApiError } from '../api/client'

const page = ref<Page<User> | null>(null)
const search = ref('')
const appliedSearch = ref('')
const currentPage = ref(0)
const loading = ref(false)
const error = ref('')

const PAGE_SIZE = 10

const isEmpty = computed(() => !loading.value && page.value?.content.length === 0)
const isFirstLoad = computed(() => loading.value && page.value === null)

const totalLabel = computed(() => {
  const total = page.value?.page.totalElements ?? 0
  return `${total} ${total === 1 ? 'usuário cadastrado' : 'usuários cadastrados'}`
})

const positionLabel = computed(() =>
  page.value ? `Página ${page.value.page.number + 1} de ${page.value.page.totalPages}` : ''
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    page.value = await usersApi.list({
      name: appliedSearch.value || undefined,
      page: currentPage.value,
      size: PAGE_SIZE
    })
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os usuários'
  } finally {
    loading.value = false
  }
}

function applySearch() {
  // Sem voltar para a primeira página, um resultado com menos páginas que o anterior
  // exibiria uma página vazia.
  appliedSearch.value = search.value.trim()
  currentPage.value = 0
  load()
}

function clearSearch() {
  search.value = ''
  applySearch()
}

function goToPage(number: number) {
  currentPage.value = number
  load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="top">
      <div class="title">
        <h1>Usuários</h1>
        <p v-if="page">
          {{ totalLabel }}
        </p>
      </div>

      <div class="tools">
        <form
          class="search"
          role="search"
          @submit.prevent="applySearch"
        >
          <div class="input-group">
            <svg
              class="icon"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fill-rule="evenodd"
                d="M9 3.5a5.5 5.5 0 103.4 9.8l3.4 3.4a1 1 0 001.4-1.4l-3.4-3.4A5.5 5.5 0 009 3.5zm-3.5 5.5a3.5 3.5 0 117 0 3.5 3.5 0 01-7 0z"
                clip-rule="evenodd"
              />
            </svg>
            <input
              v-model="search"
              class="control with-icon"
              type="search"
              placeholder="Filtrar por parte do nome"
              aria-label="Filtrar por parte do nome"
            >
          </div>
          <button
            type="submit"
            class="button button-secondary"
            :disabled="loading"
          >
            Buscar
          </button>
        </form>

        <RouterLink
          :to="{ name: 'user-new' }"
          class="button button-primary new-user"
        >
          <svg
            viewBox="0 0 20 20"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M10 4a1 1 0 011 1v4h4a1 1 0 110 2h-4v4a1 1 0 11-2 0v-4H5a1 1 0 110-2h4V5a1 1 0 011-1z" />
          </svg>
          Novo usuário
        </RouterLink>
      </div>
    </div>

    <p
      v-if="error"
      class="alert"
      role="alert"
    >
      {{ error }}
    </p>

    <div class="panel">
      <div class="scroll">
        <table>
          <thead>
            <tr>
              <th class="col-id">
                Id
              </th>
              <th>Nome</th>
              <th>E-mail</th>
              <th class="col-actions">
                <span class="visually-hidden">Ações</span>
              </th>
            </tr>
          </thead>

          <tbody
            v-if="isFirstLoad"
            aria-hidden="true"
          >
            <tr
              v-for="row in 5"
              :key="row"
            >
              <td class="col-id">
                <span
                  class="skeleton"
                  style="width: 1.5rem"
                />
              </td>
              <td>
                <span
                  class="skeleton"
                  style="width: 60%"
                />
              </td>
              <td>
                <span
                  class="skeleton"
                  style="width: 75%"
                />
              </td>
              <td class="col-actions">
                <span
                  class="skeleton"
                  style="width: 3rem"
                />
              </td>
            </tr>
          </tbody>

          <tbody v-else>
            <tr
              v-for="user in page?.content ?? []"
              :key="user.id"
            >
              <td class="col-id">
                {{ user.id }}
              </td>
              <td class="name">
                {{ user.name }}
              </td>
              <td class="email">
                {{ user.email }}
              </td>
              <td class="col-actions">
                <RouterLink
                  :to="{ name: 'user-edit', params: { id: user.id } }"
                  class="edit"
                >
                  Editar
                </RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-if="isEmpty"
        class="empty-state"
      >
        <template v-if="appliedSearch">
          <p class="empty-title">
            Nenhum usuário corresponde a "{{ appliedSearch }}"
          </p>
          <p class="empty-text">
            O filtro busca qualquer parte do nome e ignora maiúsculas. Tente um trecho menor.
          </p>
          <button
            type="button"
            class="button button-secondary"
            @click="clearSearch"
          >
            Limpar filtro
          </button>
        </template>
        <template v-else>
          <p class="empty-title">
            Nenhum usuário cadastrado
          </p>
          <p class="empty-text">
            Ao cadastrar o primeiro usuário, ele recebe uma notificação por e-mail confirmando o
            registro.
          </p>
          <RouterLink
            :to="{ name: 'user-new' }"
            class="button button-primary"
          >
            Cadastrar usuário
          </RouterLink>
        </template>
      </div>

      <div
        v-if="page && page.page.totalPages > 1"
        class="pagination"
      >
        <span class="position">{{ positionLabel }}</span>
        <div class="navigation">
          <button
            type="button"
            class="button button-secondary"
            :disabled="currentPage === 0 || loading"
            @click="goToPage(currentPage - 1)"
          >
            Anterior
          </button>
          <button
            type="button"
            class="button button-secondary"
            :disabled="currentPage >= page.page.totalPages - 1 || loading"
            @click="goToPage(currentPage + 1)"
          >
            Próxima
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.top {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
}

.title h1 {
  font-size: 1.375rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.title p {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.tools {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.search {
  display: flex;
  gap: 0.5rem;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.icon {
  position: absolute;
  left: 0.625rem;
  width: 1.125rem;
  height: 1.125rem;
  color: var(--text-muted);
  pointer-events: none;
}

.with-icon {
  width: min(18rem, 50vw);
  padding-left: 2.25rem;
}

.new-user {
  text-decoration: none;
}

.new-user svg {
  width: 1.125rem;
  height: 1.125rem;
}

.scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--text-muted);
  background: var(--surface-sunken);
  border-bottom: 1px solid var(--border);
  white-space: nowrap;
}

td {
  padding: 0.875rem 1rem;
  border-bottom: 1px solid var(--border);
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr {
  transition: background-color var(--duration) var(--ease);
}

tbody tr:hover {
  background: var(--surface-hover);
}

.col-id {
  width: 1%;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

.name {
  color: var(--text-strong);
  font-weight: 500;
}

.email {
  color: var(--text);
}

.col-actions {
  width: 1%;
  text-align: right;
  white-space: nowrap;
}

.edit {
  color: var(--text-strong);
  font-weight: 500;
  text-decoration: none;
  border-bottom: 1px solid var(--border-strong);
  padding-bottom: 1px;
  transition: border-color var(--duration) var(--ease);
}

.edit:hover {
  border-color: var(--brand);
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
}

.skeleton {
  display: block;
  height: 0.875rem;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, #ececef 25%, #f6f6f7 50%, #ececef 75%);
  background-size: 200% 100%;
  animation: slide 1.4s ease-in-out infinite;
}

@keyframes slide {
  to {
    background-position: -200% 0;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 3rem 1.5rem;
  text-align: center;
}

.empty-title {
  margin: 0;
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-strong);
}

.empty-text {
  margin: 0 0 0.75rem;
  font-size: 0.875rem;
  color: var(--text-muted);
  max-width: 42ch;
  text-wrap: pretty;
}

.empty-state .button {
  text-decoration: none;
}

.pagination {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border-top: 1px solid var(--border);
}

.position {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.navigation {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 40rem) {
  .tools,
  .search {
    width: 100%;
  }

  .input-group {
    flex: 1;
  }

  .with-icon {
    width: 100%;
  }

  .col-id {
    display: none;
  }
}
</style>
