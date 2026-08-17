<template>
  <div class="tela qr-celular">
    <header>
      <h1>Envie pelo celular</h1>
      <p>Escaneie o QR Code e escolha uma foto da galeria.</p>
    </header>

    <div class="qr-area">
      <canvas v-show="!carregando && !erro" ref="canvas"></canvas>
      <p v-if="carregando" class="status">Gerando QR Code...</p>
      <div v-else-if="erro" class="estado-erro">
        <p>{{ erro }}</p>
        <button type="button" class="botao botao-secundario" @click="iniciar">
          Gerar novo QR Code
        </button>
      </div>
    </div>

    <div v-if="!erro" class="instrucoes">
      <strong>Aguardando sua foto</strong>
      <p>O celular precisa estar conectado à mesma rede do totem.</p>
      <p class="validade">Link válido por {{ tempoFormatado }}</p>
    </div>

    <button class="botao botao-secundario voltar" @click="voltar">
      Voltar
    </button>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()
const canvas = ref(null)
const carregando = ref(true)
const erro = ref(null)
const segundosRestantes = ref(300)

let polling = null
let relogio = null
let consultando = false

const tempoFormatado = computed(() => {
  const minutos = Math.floor(segundosRestantes.value / 60)
  const segundos = String(segundosRestantes.value % 60).padStart(2, '0')
  return `${minutos}:${segundos}`
})

function pararTemporizadores() {
  clearInterval(polling)
  clearInterval(relogio)
  polling = null
  relogio = null
}

async function iniciar() {
  pararTemporizadores()
  carregando.value = true
  erro.value = null
  segundosRestantes.value = 300

  try {
    const { data } = await api.post(`/sessoes/${sessao.id}/foto/celular/iniciar`)
    segundosRestantes.value = data.expiraEmSegundos

    const origemPublica = import.meta.env.VITE_PUBLIC_URL || window.location.origin
    const url = new URL('/upload-celular', origemPublica)
    url.searchParams.set('sessao', sessao.id)
    url.searchParams.set('token', data.token)
    await QRCode.toCanvas(canvas.value, url.toString(), { width: 240, margin: 1 })

    carregando.value = false
    iniciarRelogio()
    polling = setInterval(consultarEnvio, 1500)
  } catch {
    carregando.value = false
    erro.value = 'Não foi possível gerar o link de envio. Verifique a conexão.'
  }
}

function iniciarRelogio() {
  relogio = setInterval(() => {
    segundosRestantes.value -= 1
    if (segundosRestantes.value <= 0) {
      pararTemporizadores()
      erro.value = 'Este QR Code expirou.'
    }
  }, 1000)
}

async function consultarEnvio() {
  if (consultando) return
  consultando = true
  try {
    const { data: estadoSessao } = await api.get(`/sessoes/${sessao.id}`)
    if (estadoSessao.estado === 'REVISANDO_FOTO') {
      pararTemporizadores()
      const { data: foto } = await api.get(`/sessoes/${sessao.id}/foto`, { responseType: 'blob' })
      if (sessao.fotoPreviewUrl?.startsWith('blob:')) {
        URL.revokeObjectURL(sessao.fotoPreviewUrl)
      }
      sessao.fotoPreviewUrl = URL.createObjectURL(foto)
      router.push('/revisar')
    }
  } catch {
    pararTemporizadores()
    erro.value = 'A conexão com o totem foi interrompida.'
  } finally {
    consultando = false
  }
}

function voltar() {
  pararTemporizadores()
  router.back()
}

onMounted(iniciar)
onUnmounted(pararTemporizadores)
</script>

<style scoped>
.qr-celular {
  padding: 28px 24px 20px;
  gap: 18px;
}

header {
  text-align: center;
}

h1 {
  font-size: 24px;
}

header p,
.instrucoes p {
  color: #6b6b66;
  font-size: 13px;
  line-height: 1.45;
}

header p {
  margin-top: 6px;
}

.qr-area {
  width: 280px;
  height: 280px;
  padding: 20px;
  border: 1px solid #e6e4dc;
  border-radius: 20px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-area canvas {
  width: 240px !important;
  height: 240px !important;
}

.status,
.estado-erro {
  color: #6b6b66;
  font-size: 14px;
  text-align: center;
}

.estado-erro .botao {
  margin-top: 18px;
  padding: 14px;
  font-size: 14px;
}

.instrucoes {
  text-align: center;
}

.instrucoes strong {
  display: block;
  margin-bottom: 4px;
  font-size: 16px;
}

.instrucoes .validade {
  margin-top: 7px;
  color: #1c1c1a;
  font-weight: 600;
}

.voltar {
  margin-top: auto;
}
</style>
