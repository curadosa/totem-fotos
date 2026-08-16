<template>
  <div class="tela" style="justify-content: center; text-align: center;">
    <p style="font-size: 26px; font-weight: 600; line-height: 1.3;">Imprima sua foto</p>
    <p style="font-size: 14px; color: #6b6b66; line-height: 1.7;">
      Ao iniciar, você autoriza o uso da imagem — seja tirada na cabine ou
      enviada do seu celular — apenas para processar e imprimir este pedido.
      Todas as fotos são removidas automaticamente ao encerrar a sessão.
    </p>
    <button class="botao botao-primario" style="margin-top: auto;" @click="iniciar">
      Iniciar sessão
    </button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
function iniciar() {
  router.push('/home')
}
</script>
