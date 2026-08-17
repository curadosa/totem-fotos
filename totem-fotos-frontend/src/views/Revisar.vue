<template>
  <div class="tela" style="padding: 20px; gap: 12px;">
    <div style="flex: 1; width: 100%; background: #2c2c2a; border-radius: 16px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
      <img v-if="sessao.fotoPreviewUrl" :src="sessao.fotoPreviewUrl" style="width: 100%; height: 100%; object-fit: cover;" />
    </div>
    <p style="font-size: 16px; font-weight: 600;">Ficou boa?</p>
    <div style="display: flex; gap: 10px; width: 100%;">
      <button class="botao botao-secundario" @click="refazer">Refazer</button>
      <button class="botao botao-primario" @click="confirmar">Confirmar</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { limparFotoLocal, sessao } from '../services/sessaoState'

const router = useRouter()

function refazer() {
  limparFotoLocal()
  router.push('/capturar')
}

function confirmar() {
  router.push('/revisar-impressao')
}
</script>
