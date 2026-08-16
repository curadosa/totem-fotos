<template>
  <div class="tela captura">
    <div class="camera">
      <video
        v-show="cameraDisponivel"
        ref="video"
        autoplay
        muted
        playsinline
        @loadedmetadata="cameraPronta = true"
      ></video>

      <div v-if="cameraDisponivel" class="moldura"></div>

      <div v-if="contagem !== null" class="contagem-fundo">
        <div class="contagem-circulo">
          <span>{{ contagem }}</span>
        </div>
      </div>

      <div v-if="carregandoCamera" class="mensagem-camera">
        <p>Abrindo a câmera...</p>
      </div>

      <div v-else-if="!cameraDisponivel" class="mensagem-camera">
        <p class="mensagem-titulo">Use a câmera do celular</p>
        <p>A câmera ao vivo não está disponível neste acesso pela rede.</p>
      </div>
    </div>

    <p v-if="cameraDisponivel" class="instrucao">Centralize seu rosto na moldura</p>
    <p v-else class="instrucao">Ao tocar abaixo, a câmera nativa do aparelho será aberta.</p>
    <p v-if="erro" class="erro">{{ erro }}</p>

    <button
      v-if="cameraDisponivel"
      class="botao botao-primario"
      :disabled="!cameraPronta || contagem !== null || enviando"
      @click="iniciarContagem"
    >
      {{ enviando ? 'Enviando...' : 'Tirar foto' }}
    </button>

    <label v-else class="botao botao-primario botao-arquivo" :class="{ desabilitado: enviando }">
      {{ enviando ? 'Enviando...' : 'Abrir câmera do celular' }}
      <input
        type="file"
        accept="image/*"
        capture="user"
        :disabled="enviando"
        @change="capturarComCelular"
      >
    </label>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { sessao } from '../services/sessaoState'

const router = useRouter()
const video = ref(null)
const contagem = ref(null)
const carregandoCamera = ref(true)
const cameraDisponivel = ref(false)
const cameraPronta = ref(false)
const enviando = ref(false)
const erro = ref(null)
let stream = null
let intervaloContagem = null

onMounted(abrirCamera)

onUnmounted(() => {
  clearInterval(intervaloContagem)
  stream?.getTracks().forEach(track => track.stop())
})

async function abrirCamera() {
  erro.value = null
  if (!navigator.mediaDevices?.getUserMedia) {
    carregandoCamera.value = false
    return
  }

  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user' },
      audio: false
    })
    video.value.srcObject = stream
    cameraDisponivel.value = true
  } catch (e) {
    cameraDisponivel.value = false
  } finally {
    carregandoCamera.value = false
  }
}

function iniciarContagem() {
  erro.value = null
  contagem.value = 3
  intervaloContagem = setInterval(() => {
    contagem.value -= 1
    if (contagem.value === 0) {
      clearInterval(intervaloContagem)
      capturarDoVideo()
    }
  }, 1000)
}

async function capturarDoVideo() {
  const canvas = document.createElement('canvas')
  canvas.width = video.value.videoWidth
  canvas.height = video.value.videoHeight
  canvas.getContext('2d').drawImage(video.value, 0, 0)

  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.92))
  contagem.value = null
  if (!blob) {
    erro.value = 'Não foi possível capturar a foto. Tente novamente.'
    return
  }
  await enviarFoto(blob, 'foto.jpg')
}

async function capturarComCelular(event) {
  const arquivo = event.target.files?.[0]
  if (!arquivo) return
  await enviarFoto(arquivo, arquivo.name || 'foto.jpg')
  event.target.value = ''
}

async function enviarFoto(arquivo, nomeArquivo) {
  enviando.value = true
  erro.value = null
  try {
    if (sessao.fotoPreviewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(sessao.fotoPreviewUrl)
    }
    sessao.fotoPreviewUrl = URL.createObjectURL(arquivo)

    const form = new FormData()
    form.append('arquivo', arquivo, nomeArquivo)
    await api.post(`/sessoes/${sessao.id}/foto`, form)
    router.push('/revisar')
  } catch (e) {
    erro.value = 'Não foi possível enviar a foto. Verifique a conexão e tente novamente.'
  } finally {
    enviando.value = false
  }
}
</script>

<style scoped>
.captura {
  padding: 20px;
  gap: 12px;
}

.camera {
  flex: 1;
  width: 100%;
  min-height: 0;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: #2c2c2a;
}

.camera video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.moldura {
  position: absolute;
  inset: 10%;
  border: 3px dashed rgba(255, 255, 255, .7);
  border-radius: 8px;
  pointer-events: none;
}

.contagem-fundo,
.mensagem-camera {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contagem-fundo {
  background: rgba(0, 0, 0, .25);
}

.contagem-circulo {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(0, 0, 0, .55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.contagem-circulo span {
  color: #fff;
  font-size: 48px;
  font-weight: 600;
}

.mensagem-camera {
  padding: 36px;
  flex-direction: column;
  gap: 8px;
  color: #d8d7d1;
  text-align: center;
  font-size: 14px;
  line-height: 1.5;
}

.mensagem-camera .mensagem-titulo {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.instrucao {
  color: #6b6b66;
  text-align: center;
  font-size: 14px;
}

.erro {
  color: #c83c3b;
  text-align: center;
  font-size: 13px;
}

.botao-arquivo {
  display: block;
  text-align: center;
}

.botao-arquivo input {
  display: none;
}

.botao-arquivo.desabilitado,
.botao:disabled {
  opacity: .45;
  cursor: not-allowed;
}
</style>
