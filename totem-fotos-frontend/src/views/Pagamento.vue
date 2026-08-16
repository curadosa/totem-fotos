<template>
  <div class="tela" style="justify-content: center; text-align: center; gap: 16px;">
    <template v-if="erro">
      <p style="font-size: 16px; font-weight: 600;">Sem conexão no momento</p>
      <p style="font-size: 13px; color: #6b6b66;">Tente novamente em instantes</p>
      <button class="botao botao-primario" @click="gerarCobranca">Tentar novamente</button>
    </template>

    <template v-else-if="pago">
      <p style="font-size: 16px; font-weight: 600;">Pagamento confirmado</p>
      <p style="font-size: 13px; color: #6b6b66;">Imprimindo sua foto...</p>
    </template>

    <template v-else-if="cobranca">
      <p style="font-size: 16px; font-weight: 600;">Escaneie para pagar</p>
      <p style="font-size: 13px; color: #6b6b66;">{{ sessao.produto?.nome }}</p>
      <p style="font-size: 22px; font-weight: 600;">{{ valorFormatado }}</p>
      <canvas ref="canvas" style="width: 200px; height: 200px;"></canvas>
      <p style="font-size: 13px; color: #6b6b66;">Expira em {{ minutosRestantes }}:{{ segundosRestantes }}</p>
      <button class="botao botao-secundario" @click="cancelar">Cancelar</button>
    </template>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()
const canvas = ref(null)
const cobranca = ref(null)
const pago = ref(false)
const erro = ref(false)
const segundosTotal = ref(300)
let polling = null
let cronometro = null

const minutosRestantes = computed(() => String(Math.floor(segundosTotal.value / 60)).padStart(1, '0'))
const segundosRestantes = computed(() => String(segundosTotal.value % 60).padStart(2, '0'))
const valorFormatado = computed(() => sessao.produto?.valor.toLocaleString('pt-BR', {
  style: 'currency',
  currency: 'BRL'
}) ?? '')

async function gerarCobranca() {
  erro.value = false
  try {
    const { data } = await api.post(`/sessoes/${sessao.id}/pagamento`)
    cobranca.value = data
    segundosTotal.value = 300
    await QRCode.toCanvas(canvas.value, data.qrCodePayload, { width: 200 })
    iniciarPolling()
    iniciarCronometro()
  } catch (e) {
    erro.value = true
  }
}

function iniciarPolling() {
  polling = setInterval(async () => {
    const { data } = await api.get(`/sessoes/${sessao.id}/pagamento/status`)
    if (data.pago) {
      pago.value = true
      clearInterval(polling)
      clearInterval(cronometro)
      setTimeout(async () => {
        await api.post(`/sessoes/${sessao.id}/finalizar`)
        router.push('/imprimindo')
      }, 1500)
    }
  }, 2000)
}

function iniciarCronometro() {
  cronometro = setInterval(() => {
    if (segundosTotal.value > 0) segundosTotal.value -= 1
  }, 1000)
}

async function cancelar() {
  clearInterval(polling)
  clearInterval(cronometro)
  await api.post(`/sessoes/${sessao.id}/finalizar`)
  sessao.id = null
  sessao.fotoPreviewUrl = null
  sessao.produto = null
  router.push('/home')
}

onMounted(gerarCobranca)
onUnmounted(() => {
  clearInterval(polling)
  clearInterval(cronometro)
})
</script>
