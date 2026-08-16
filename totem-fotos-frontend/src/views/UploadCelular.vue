<template>
  <div style="max-width: 360px; margin: 40px auto; text-align: center; padding: 0 20px; font-family: system-ui, sans-serif;">
    <p style="font-size: 18px; font-weight: 600;">Enviar sua foto</p>
    <p style="font-size: 13px; color: #6b6b66; margin: 8px 0 24px;">Sem cadastro, sem senha.</p>

    <input type="file" accept="image/*" @change="enviar" :disabled="enviando || enviado" />

    <p v-if="enviando" style="margin-top: 16px;">Enviando...</p>
    <p v-if="enviado" style="margin-top: 16px; color: #1d9e75;">Foto enviada! Pode voltar pro totem.</p>
    <p v-if="erro" style="margin-top: 16px; color: #e24b4a;">{{ erro }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'

const route = useRoute()
const sessaoId = route.query.sessao
const token = route.query.token

const enviando = ref(false)
const enviado = ref(false)
const erro = ref(null)

async function enviar(event) {
  const arquivo = event.target.files[0]
  if (!arquivo) return

  enviando.value = true
  erro.value = null
  try {
    const form = new FormData()
    form.append('token', token)
    form.append('arquivo', arquivo)
    await api.post(`/sessoes/${sessaoId}/foto/celular/upload`, form)
    enviado.value = true
  } catch (e) {
    erro.value = 'Não foi possível enviar. Volte ao totem e gere um novo QR code.'
  } finally {
    enviando.value = false
  }
}
</script>
