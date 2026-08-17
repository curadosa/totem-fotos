<template>
  <div class="tela revisao-impressao">
    <header>
      <h1>Assim será impresso</h1>
      <p>{{ sessao.produto?.nome }}</p>
    </header>

    <div class="area-preview">
      <div
        v-if="sessao.produto"
        class="papel-10x15"
        :class="{ 'papel-10x15--paisagem': sessao.produto.id === 'SEIS_FOTOS_3X4' }"
      >
        <div v-if="sessao.produto.id === 'POLAROID'" class="polaroid">
          <img
            :key="sessao.fotoPreviewUrl"
            :src="sessao.fotoPreviewUrl"
            alt="Prévia da foto no formato Polaroid"
          >
        </div>

        <img
          v-else-if="sessao.produto.id === 'NORMAL_10X15'"
          :key="sessao.fotoPreviewUrl"
          class="foto-normal"
          :src="sessao.fotoPreviewUrl"
          alt="Prévia da foto no formato 10 por 15"
        >

        <div v-else-if="sessao.produto.id === 'SEIS_FOTOS_3X4'" class="grade-3x4">
          <img
            v-for="numero in 6"
            :key="`${sessao.fotoPreviewUrl}-${numero}`"
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
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import {
  limparFotoLocal,
  renovarFotoPreviewLocal,
  sessao
} from '../services/sessaoState'

const router = useRouter()

onBeforeMount(() => {
  if (!sessao.produto) {
    router.replace('/home')
    return
  }
  if (!sessao.fotoLocal) {
    router.replace('/capturar')
    return
  }
  renovarFotoPreviewLocal()
})

function refazer() {
  limparFotoLocal()
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

.papel-10x15 {
  width: min(76%, 280px);
  aspect-ratio: 2 / 3;
  flex: none;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.papel-10x15--paisagem {
  width: min(100%, 410px);
  aspect-ratio: 3 / 2;
}

.polaroid {
  width: 82%;
  aspect-ratio: 155 / 178;
  background: #fff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, .16);
  display: grid;
  grid-template-columns: 16fr 278fr 16fr;
  grid-template-rows: 16fr 278fr 62fr;
}

.polaroid img {
  grid-column: 2;
  grid-row: 2;
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.foto-normal {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.grade-3x4 {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(3, 20%);
  grid-template-rows: repeat(2, 40%);
  align-content: center;
  justify-content: center;
  gap: 2%;
}

.grade-3x4 img {
  width: 100%;
  height: 100%;
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
