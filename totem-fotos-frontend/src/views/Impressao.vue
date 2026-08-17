<template>
  <div class="tela" style="justify-content: center; align-items: center; text-align: center; gap: 16px;">
    <p style="font-size: 18px; font-weight: 600;">Imprimindo sua foto...</p>
    <p style="font-size: 13px; color: #6b6b66;">Retire a foto na bandeja abaixo</p>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { limparSessaoLocal } from '../services/sessaoState'

const router = useRouter()

onMounted(() => {
  setTimeout(() => {
    limparSessaoLocal()
    router.push('/')
  }, 6000)
})
</script>
