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
        <p class="mensagem-titulo">Câmera indisponível</p>
        <p>Verifique se a câmera do totem está conectada e se a permissão foi concedida.</p>
      </div>
    </div>

    <label v-if="cameras.length > 1" class="seletor-camera">
      <span>Webcam</span>
      <select v-model="cameraSelecionada" :disabled="carregandoCamera" @change="trocarCamera">
        <option v-for="camera in cameras" :key="camera.deviceId" :value="camera.deviceId">
          {{ camera.label }}
        </option>
      </select>
    </label>

    <p v-if="cameraDisponivel" class="instrucao">Centralize seu rosto na moldura</p>
    <p v-else class="instrucao">A foto será mantida somente neste totem durante a sessão.</p>
    <p v-if="erro" class="erro">{{ erro }}</p>

    <button
      v-if="cameraDisponivel"
      class="botao botao-primario"
      :disabled="!cameraPronta || contagem !== null || enviando"
      @click="iniciarContagem"
    >
      {{ enviando ? 'Processando...' : 'Tirar foto' }}
    </button>

    <button
      v-else
      class="botao botao-primario"
      :disabled="carregandoCamera"
      @click="abrirCamera"
    >
      {{ carregandoCamera ? 'Abrindo câmera...' : 'Tentar abrir câmera' }}
    </button>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { definirFotoLocal } from '../services/sessaoState'

const router = useRouter()
const video = ref(null)
const contagem = ref(null)
const carregandoCamera = ref(true)
const cameraDisponivel = ref(false)
const cameraPronta = ref(false)
const enviando = ref(false)
const erro = ref(null)
const cameras = ref([])
const cameraSelecionada = ref('')
let stream = null
let intervaloContagem = null

onMounted(() => {
  abrirCamera()
  navigator.mediaDevices?.addEventListener?.('devicechange', carregarCameras)
})

onUnmounted(() => {
  clearInterval(intervaloContagem)
  stream?.getTracks().forEach(track => track.stop())
  if (video.value) video.value.srcObject = null
  navigator.mediaDevices?.removeEventListener?.('devicechange', carregarCameras)
})

async function abrirCamera() {
  erro.value = null
  carregandoCamera.value = true
  cameraDisponivel.value = false
  cameraPronta.value = false
  stream?.getTracks().forEach(track => track.stop())
  stream = null

  if (!window.isSecureContext) {
    erro.value = 'A webcam exige acesso por localhost ou HTTPS. Abra o totem em http://localhost:5173.'
    carregandoCamera.value = false
    return
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    erro.value = 'Este navegador não oferece acesso à webcam.'
    carregandoCamera.value = false
    return
  }

  try {
    stream = await solicitarWebcam()
    video.value.srcObject = stream
    await video.value.play()
    cameraDisponivel.value = true
    await carregarCameras()
  } catch (e) {
    stream?.getTracks().forEach(track => track.stop())
    stream = null
    if (video.value) video.value.srcObject = null
    cameraDisponivel.value = false
    erro.value = mensagemErroCamera(e)
  } finally {
    carregandoCamera.value = false
  }
}

async function solicitarWebcam() {
  const video = cameraSelecionada.value
    ? {
        deviceId: { exact: cameraSelecionada.value },
        width: { ideal: 1280 },
        height: { ideal: 720 },
        frameRate: { ideal: 30 }
      }
    : true

  try {
    return await navigator.mediaDevices.getUserMedia({ video, audio: false })
  } catch (e) {
    if (cameraSelecionada.value && ['NotFoundError', 'OverconstrainedError'].includes(e.name)) {
      cameraSelecionada.value = ''
      return navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    }
    throw e
  }
}

async function carregarCameras() {
  if (!navigator.mediaDevices?.enumerateDevices) return
  try {
    const dispositivos = await navigator.mediaDevices.enumerateDevices()
    cameras.value = dispositivos
      .filter(dispositivo => dispositivo.kind === 'videoinput')
      .map((dispositivo, indice) => ({
        deviceId: dispositivo.deviceId,
        label: dispositivo.label || `Webcam ${indice + 1}`
      }))

    const dispositivoAtivo = stream?.getVideoTracks()[0]?.getSettings().deviceId
    if (dispositivoAtivo) cameraSelecionada.value = dispositivoAtivo
  } catch {
    cameras.value = []
  }
}

function trocarCamera() {
  abrirCamera()
}

function mensagemErroCamera(e) {
  const mensagens = {
    NotAllowedError: 'Permissão da webcam negada. Libere o acesso nas configurações do navegador.',
    NotFoundError: 'Nenhuma webcam foi encontrada. Verifique o cabo USB e tente novamente.',
    NotReadableError: 'A webcam está sendo usada por outro programa. Feche-o e tente novamente.',
    OverconstrainedError: 'A webcam não suporta a configuração solicitada.',
    SecurityError: 'O navegador bloqueou a webcam. Use localhost ou HTTPS.'
  }
  return mensagens[e?.name] || 'Não foi possível acessar a webcam do totem.'
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
  prepararFotoLocal(blob, 'foto.jpg')
}

function prepararFotoLocal(arquivo, nomeArquivo) {
  enviando.value = true
  erro.value = null
  try {
    const foto = arquivo instanceof File
      ? arquivo
      : new File([arquivo], nomeArquivo, { type: arquivo.type || 'image/jpeg' })
    definirFotoLocal(foto)
    router.push('/revisar')
  } catch {
    erro.value = 'Não foi possível preparar a foto. Tente novamente.'
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

.seletor-camera {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b6b66;
  font-size: 13px;
}

.seletor-camera select {
  flex: 1;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid #d7d5cc;
  border-radius: 10px;
  background: #fff;
  color: #1c1c1a;
}

.erro {
  color: #c83c3b;
  text-align: center;
  font-size: 13px;
}

.botao:disabled {
  opacity: .45;
  cursor: not-allowed;
}
</style>
