import pluginVue from 'eslint-plugin-vue'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'

export default defineConfigWithVueTs(
  {
    name: 'app/arquivos-analisados',
    files: ['**/*.ts', '**/*.vue']
  },
  {
    name: 'app/ignorados',
    ignores: ['dist/**', 'node_modules/**']
  },
  pluginVue.configs['flat/recommended'],
  vueTsConfigs.recommended
)
