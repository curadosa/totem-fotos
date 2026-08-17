import { markRaw, reactive } from 'vue'

// Estado simples compartilhado entre as telas - so o id da sessao atual
// e o preview da foto tirada/enviada. Sem Vuex/Pinia: nao ha necessidade
// pra um fluxo linear como esse.
export const sessao = reactive({
  id: null,
  fotoLocal: null,
  fotoPreviewUrl: null,
  produto: null
})

export function definirFotoLocal(arquivo) {
  limparFotoLocal()
  sessao.fotoLocal = markRaw(arquivo)
  sessao.fotoPreviewUrl = URL.createObjectURL(arquivo)
}

export function renovarFotoPreviewLocal() {
  if (!sessao.fotoLocal) return null
  if (sessao.fotoPreviewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(sessao.fotoPreviewUrl)
  }
  sessao.fotoPreviewUrl = URL.createObjectURL(sessao.fotoLocal)
  return sessao.fotoPreviewUrl
}

export function limparFotoLocal() {
  if (sessao.fotoPreviewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(sessao.fotoPreviewUrl)
  }
  sessao.fotoLocal = null
  sessao.fotoPreviewUrl = null
}

export function limparSessaoLocal() {
  limparFotoLocal()
  sessao.id = null
  sessao.produto = null
}
