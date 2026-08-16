<template>
  <div class="tela" style="justify-content: center; text-align: center; gap: 16px;">
    <p style="font-size: 18px; font-weight: 600;">Aponte a câmera do celular para o QR code</p>
    <canvas ref="canvas" style="width: 220px; height: 220px;"></canvas>
    <p style="font-size: 13px; color: #6b6b66;">
      Conecta na rede do totem e abre a página de envio automaticamente.
      Válido por 5 minutos.
    </p>
    <button class="botao botao-secundario" style="margin-top: auto;" @click="router.back()">
      Voltar
    </button>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()
const canvas = ref(null)
let polling = null

onMounted(async () => {
  const { data } = await api.post(`/sessoes/${sessao.id}/foto/celular/iniciar`)
  // IP local do totem na rede/hotspot - trocar pelo IP real da maquina.
  const ipTotem = window.location.hostname
  const url = `http://${ipTotem}:5173/upload-celular?sessao=${sessao.id}&token=${data.token}`
  await QRCode.toCanvas(canvas.value, url, { width: 220 })

  polling = setInterval(async () => {
    const { data: s } = await api.get(`/sessoes/${sessao.id}`)
    if (s.estado === 'REVISANDO_FOTO') {
      clearInterval(polling)
      const { data: foto } = await api.get(`/sessoes/${sessao.id}/foto`, { responseType: 'blob' })
      sessao.fotoPreviewUrl = URL.createObjectURL(foto)
      router.push('/revisar')
    }
  }, 2000)
})

onUnmounted(() => clearInterval(polling))
</script>
