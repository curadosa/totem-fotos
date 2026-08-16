import { reactive } from 'vue'

// Estado simples compartilhado entre as telas - so o id da sessao atual
// e o preview da foto tirada/enviada. Sem Vuex/Pinia: nao ha necessidade
// pra um fluxo linear como esse.
export const sessao = reactive({
  id: null,
  fotoPreviewUrl: null,
  produto: null
})
