<template>
  <div class="tela revisao-impressao">
    <header>
      <h1>Assim será impresso</h1>
      <p>{{ sessao.produto?.nome }}</p>
    </header>

    <div class="area-preview">
      <div v-if="sessao.produto?.id === 'POLAROID'" class="polaroid">
        <img :src="sessao.fotoPreviewUrl" alt="Prévia da foto no formato Polaroid">
      </div>

      <div v-else-if="sessao.produto?.id === 'NORMAL_10X15'" class="foto-normal">
        <img :src="sessao.fotoPreviewUrl" alt="Prévia da foto no formato 10 por 15">
      </div>

      <div v-else-if="sessao.produto?.id === 'SEIS_FOTOS_3X4'" class="folha-3x4">
        <div class="grade-3x4">
          <img
            v-for="numero in 6"
            :key="numero"
            :src="sessao.fotoPreviewUrl"
            :alt="`Prévia da foto 3x4 número ${numero}`"
          >
        </div>
      </div>
    </div>

    <p class="orientacao">Confira o corte e a disposição antes de continuar.</p>

    <div class="acoes">
      <button class="botao botao-secundario" @click="refazer">Refazer</button>
      <button class="botao botao-primario" @click="confirmar">Confirmar</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()

async function refazer() {
  await api.delete(`/sessoes/${sessao.id}/foto`)
  if (sessao.fotoPreviewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(sessao.fotoPreviewUrl)
  }
  sessao.fotoPreviewUrl = null
  router.push('/capturar')
}

function confirmar() {
  router.push('/pagamento')
}
</script>

<style scoped>
.revisao-impressao {
  padding: 20px;
  gap: 12px;
}

header {
  text-align: center;
}

h1 {
  font-size: 20px;
}

header p,
.orientacao {
  color: #6b6b66;
  font-size: 13px;
}

header p {
  margin-top: 4px;
}

.area-preview {
  flex: 1;
  width: 100%;
  min-height: 0;
  padding: 18px;
  border-radius: 16px;
  background: #deddd7;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.polaroid {
  width: min(82%, 310px);
  padding: 16px 16px 62px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .2);
  transform: rotate(-1deg);
}

.polaroid img {
  display: block;
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
}

.foto-normal {
  width: min(72%, 300px);
  aspect-ratio: 2 / 3;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .2);
  overflow: hidden;
}

.foto-normal img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.folha-3x4 {
  width: 100%;
  max-width: 410px;
  aspect-ratio: 3 / 2;
  padding: 12px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.grade-3x4 {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(3, 22%);
  grid-template-rows: repeat(2, auto);
  align-content: center;
  justify-content: center;
  gap: 5px;
}

.grade-3x4 img {
  width: 100%;
  aspect-ratio: 3 / 4;
  display: block;
  object-fit: cover;
  border: 1px solid #e0dfd9;
}

.orientacao {
  text-align: center;
}

.acoes {
  width: 100%;
  display: flex;
  gap: 10px;
}
</style>
