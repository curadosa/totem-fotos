<template>
  <main class="aplicacao">
    <section v-if="tela === 'boas-vindas'" class="tela centro">
      <p class="marca">Totem Fotos Desktop</p>
      <h1>Imprima seus momentos</h1>
      <p class="apoio">Tire uma foto agora ou envie uma imagem do celular.</p>
      <label class="consentimento">
        <input v-model="consentiu" type="checkbox">
        <span>Concordo com o uso temporário da foto somente neste pedido.</span>
      </label>
      <button class="botao primario" :disabled="!consentiu" @click="tela = 'home'">Começar</button>
    </section>

    <section v-else-if="tela === 'home'" class="tela">
      <header><h1>Escolha sua foto</h1><p>Selecione o formato que deseja imprimir</p></header>
      <div class="produtos">
        <button v-for="item in produtos" :key="item.id" class="produto" :class="{ selecionado: produto?.id === item.id }" @click="produto = item">
          <span><strong>{{ item.nome }}</strong><small>{{ item.detalhe }}</small></span>
          <strong>{{ moeda(item.valor) }}</strong>
        </button>
      </div>
      <div class="acoes rodape">
        <button class="botao primario" :disabled="!produto" @click="abrirCaptura">Tirar foto agora</button>
        <button class="botao secundario" :disabled="!produto" @click="abrirQr">Enviar do celular</button>
      </div>
    </section>

    <section v-else-if="tela === 'capturar'" class="tela">
      <header><h1>Tirar foto</h1><p>Centralize-se na imagem</p></header>
      <div class="camera">
        <video v-show="cameraAtiva" ref="video" autoplay muted playsinline></video>
        <p v-if="!cameraAtiva">{{ erroCamera || 'Abrindo câmera...' }}</p>
      </div>
      <button class="botao primario" :disabled="!cameraAtiva" @click="capturar">Tirar foto</button>
      <button class="botao secundario" @click="voltarHome">Voltar</button>
    </section>

    <section v-else-if="tela === 'qr'" class="tela qr">
      <header><h1>Envie pelo celular</h1><p>Escaneie o QR Code e escolha uma foto.</p></header>
      <div class="qr-area">
        <canvas v-show="!erroQr" ref="qrCanvas"></canvas>
        <p v-if="erroQr" class="erro">{{ erroQr }}</p>
      </div>
      <div class="status">
        <strong>{{ statusQr }}</strong>
        <p>O celular precisa estar na mesma rede do totem.</p>
        <p>Link válido por {{ tempoQr }}</p>
      </div>
      <button class="botao secundario rodape" @click="voltarHome">Voltar</button>
    </section>

    <section v-else-if="tela === 'revisar'" class="tela">
      <header><h1>Ficou boa?</h1><p>Confira a foto antes de continuar</p></header>
      <div class="preview"><img :src="fotoUrl" alt="Foto selecionada"></div>
      <div class="acoes linha rodape">
        <button class="botao secundario" @click="refazer">Refazer</button>
        <button class="botao primario" @click="tela = 'revisar-impressao'">Confirmar</button>
      </div>
    </section>

    <section v-else-if="tela === 'revisar-impressao'" class="tela">
      <header><h1>Assim será impresso</h1><p>{{ produto.nome }}</p></header>
      <div class="papel" :class="produto.id.toLowerCase()">
        <img v-for="indice in produto.id === 'OITO_FOTOS_3X4' ? 8 : 1" :key="indice" :src="fotoUrl" alt="Prévia de impressão">
      </div>
      <div class="acoes linha rodape">
        <button class="botao secundario" @click="refazer">Refazer</button>
        <button class="botao primario" @click="abrirPagamento">Confirmar</button>
      </div>
    </section>

    <section v-else-if="tela === 'pagamento'" class="tela centro">
      <h1>{{ pagamentoConfirmado ? 'Pagamento confirmado' : 'Escaneie para pagar' }}</h1>
      <template v-if="!pagamentoConfirmado">
        <p>{{ produto.nome }}</p><strong class="valor">{{ moeda(produto.valor) }}</strong>
        <canvas ref="pixCanvas"></canvas>
        <p class="apoio">Pagamento local simulado</p>
        <button class="botao secundario" @click="cancelar">Cancelar</button>
      </template>
      <p v-else>Preparando a impressão...</p>
    </section>

    <section v-else class="tela centro">
      <h1>Imprimindo sua foto...</h1>
      <p class="apoio">Retire a foto na bandeja abaixo.</p>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import QRCode from 'qrcode'

const TAMANHO_MAXIMO = 10 * 1024 * 1024
const TIPOS_ACEITOS = ['image/jpeg', 'image/png']
const produtos = [
  { id: 'POLAROID', nome: 'Polaroid', detalhe: '1 foto', valor: 4.5 },
  { id: 'NORMAL_10X15', nome: 'Normal 10x15', detalhe: '1 foto', valor: 5.5 },
  { id: 'OITO_FOTOS_3X4', nome: 'Fotos 3x4', detalhe: 'Conjunto com 8 fotos', valor: 19.9 }
]

const tela = ref('boas-vindas')
const consentiu = ref(false)
const produto = ref(null)
const foto = ref(null)
const fotoUrl = ref(null)
const video = ref(null)
const cameraAtiva = ref(false)
const erroCamera = ref(null)
const qrCanvas = ref(null)
const pixCanvas = ref(null)
const erroQr = ref(null)
const statusQr = ref('Aguardando o celular...')
const segundosQr = ref(300)
const pagamentoConfirmado = ref(false)

let stream
let socket
let conexao
let canal
let relogio
let temporizadorPagamento
let temporizadorImpressao
let ofertaEmAndamento = false
let candidatosPendentes = []
let metadados
let partes = []
let bytesRecebidos = 0

const tempoQr = computed(() => `${Math.floor(segundosQr.value / 60)}:${String(segundosQr.value % 60).padStart(2, '0')}`)
const moeda = valor => valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

function limparFoto() {
  if (fotoUrl.value) URL.revokeObjectURL(fotoUrl.value)
  foto.value = null
  fotoUrl.value = null
}

function definirFoto(arquivo) {
  limparFoto()
  foto.value = arquivo
  fotoUrl.value = URL.createObjectURL(arquivo)
}

async function abrirCaptura() {
  encerrarTransferencia()
  tela.value = 'capturar'
  await nextTick()
  erroCamera.value = null
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    video.value.srcObject = stream
    await video.value.play()
    cameraAtiva.value = true
  } catch {
    erroCamera.value = 'Não foi possível acessar a webcam.'
  }
}

async function capturar() {
  const canvas = document.createElement('canvas')
  canvas.width = video.value.videoWidth
  canvas.height = video.value.videoHeight
  canvas.getContext('2d').drawImage(video.value, 0, 0)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', .92))
  if (!blob) return
  definirFoto(new File([blob], 'foto.jpg', { type: 'image/jpeg' }))
  pararCamera()
  tela.value = 'revisar'
}

function pararCamera() {
  stream?.getTracks().forEach(track => track.stop())
  stream = null
  cameraAtiva.value = false
  if (video.value) video.value.srcObject = null
}

async function abrirQr() {
  pararCamera()
  encerrarTransferencia()
  tela.value = 'qr'
  erroQr.value = null
  statusQr.value = 'Aguardando o celular...'
  segundosQr.value = 300
  const sessaoId = crypto.randomUUID()
  await nextTick()
  const origemMobile = import.meta.env.VITE_MOBILE_URL || 'http://localhost:9000'
  const url = new URL('/upload-celular', origemMobile)
  url.searchParams.set('session', sessaoId)
  await QRCode.toCanvas(qrCanvas.value, url.toString(), { width: 240, margin: 1 })

  socket = new WebSocket(`ws://localhost:9000/signal?role=totem&session=${encodeURIComponent(sessaoId)}`)
  socket.addEventListener('message', event => receberSinal(event).catch(falharQr))
  socket.addEventListener('error', () => falharQr(new Error('O servidor móvel não está disponível.')))
  relogio = setInterval(() => {
    segundosQr.value -= 1
    if (segundosQr.value <= 0) falharQr(new Error('Este QR Code expirou.'))
  }, 1000)
}

function enviarSinal(dados) {
  if (socket?.readyState === WebSocket.OPEN) socket.send(JSON.stringify(dados))
}

async function prepararOferta() {
  if (ofertaEmAndamento) return
  ofertaEmAndamento = true
  conexao?.close()
  canal?.close()
  candidatosPendentes = []
  conexao = new RTCPeerConnection({ iceServers: [] })
  canal = conexao.createDataChannel('foto', { ordered: true })
  prepararCanal(canal)
  conexao.addEventListener('icecandidate', event => {
    if (event.candidate) enviarSinal({ type: 'candidate', candidate: event.candidate })
  })
  conexao.addEventListener('connectionstatechange', () => {
    if (conexao.connectionState === 'connected') statusQr.value = 'Celular conectado. Aguardando a foto...'
    if (['failed', 'disconnected'].includes(conexao.connectionState)) statusQr.value = 'Reconectando ao celular...'
  })
  await conexao.setLocalDescription(await conexao.createOffer())
  enviarSinal({ type: 'offer', description: conexao.localDescription })
  ofertaEmAndamento = false
}

async function receberSinal(event) {
  const dados = JSON.parse(event.data)
  if (dados.type === 'peer-ready') {
    statusQr.value = 'Celular encontrado. Conectando...'
    await prepararOferta()
    return
  }
  if (dados.type === 'answer') {
    await conexao.setRemoteDescription(dados.description)
    for (const candidato of candidatosPendentes.splice(0)) await conexao.addIceCandidate(candidato)
    return
  }
  if (dados.type === 'candidate' && dados.candidate) {
    if (conexao?.remoteDescription) await conexao.addIceCandidate(dados.candidate)
    else candidatosPendentes.push(dados.candidate)
  }
}

function prepararCanal(canalFoto) {
  canalFoto.binaryType = 'arraybuffer'
  canalFoto.addEventListener('message', async event => {
    try {
      if (typeof event.data === 'string') {
        const dados = JSON.parse(event.data)
        if (dados.tipo === 'foto') {
          if (!Number.isInteger(dados.tamanho) || dados.tamanho <= 0 || dados.tamanho > TAMANHO_MAXIMO || !TIPOS_ACEITOS.includes(dados.mime)) {
            throw new Error('A foto recebida é inválida.')
          }
          metadados = dados
          partes = []
          bytesRecebidos = 0
        } else if (dados.tipo === 'fim') {
          await concluirFotoRecebida()
        }
        return
      }
      if (!metadados) throw new Error('Dados recebidos fora de ordem.')
      bytesRecebidos += event.data.byteLength
      if (bytesRecebidos > metadados.tamanho || bytesRecebidos > TAMANHO_MAXIMO) throw new Error('A foto excedeu o tamanho informado.')
      partes.push(event.data)
      statusQr.value = `Recebendo foto: ${Math.round((bytesRecebidos * 100) / metadados.tamanho)}%`
    } catch (erro) {
      falharQr(erro)
    }
  })
}

async function concluirFotoRecebida() {
  if (!metadados || bytesRecebidos !== metadados.tamanho) throw new Error('A transferência ficou incompleta.')
  definirFoto(new File(partes, metadados.nome || 'foto', { type: metadados.mime }))
  if (canal?.readyState === 'open') canal.send(JSON.stringify({ tipo: 'recebida' }))
  setTimeout(encerrarTransferencia, 100)
  tela.value = 'revisar'
}

function falharQr(erro) {
  erroQr.value = erro?.message || 'Não foi possível receber a foto.'
  statusQr.value = 'Falha na conexão'
  encerrarTransferencia()
}

function encerrarTransferencia() {
  clearInterval(relogio)
  relogio = null
  canal?.close()
  conexao?.close()
  socket?.close()
  canal = null
  conexao = null
  socket = null
  ofertaEmAndamento = false
  metadados = null
  partes = []
  bytesRecebidos = 0
}

function voltarHome() {
  pararCamera()
  encerrarTransferencia()
  tela.value = 'home'
}

function refazer() {
  limparFoto()
  tela.value = 'home'
}

async function abrirPagamento() {
  tela.value = 'pagamento'
  pagamentoConfirmado.value = false
  await nextTick()
  await QRCode.toCanvas(pixCanvas.value, `PIX-SIMULADO-${crypto.randomUUID()}-${produto.value.valor}`, { width: 210 })
  temporizadorPagamento = setTimeout(() => {
    pagamentoConfirmado.value = true
    temporizadorImpressao = setTimeout(() => {
      tela.value = 'imprimindo'
      temporizadorImpressao = setTimeout(resetar, 5000)
    }, 1200)
  }, 2500)
}

function cancelar() {
  clearTimeout(temporizadorPagamento)
  resetar()
}

function resetar() {
  clearTimeout(temporizadorPagamento)
  clearTimeout(temporizadorImpressao)
  pararCamera()
  encerrarTransferencia()
  limparFoto()
  produto.value = null
  consentiu.value = false
  pagamentoConfirmado.value = false
  tela.value = 'boas-vindas'
}

onBeforeUnmount(resetar)
</script>

<style>
* { box-sizing: border-box; }
html, body, #app { width: 100%; min-height: 100%; }
body { margin: 0; min-height: 100dvh; background: #1c1c1a; font-family: system-ui, sans-serif; overflow: auto; }
button, input { font: inherit; }
.aplicacao { width: 100%; min-height: 100dvh; padding: 8px; display: grid; place-items: center; }
.tela { width: min(480px, 100%); height: min(800px, calc(100dvh - 16px)); min-height: 0; padding: clamp(18px, 4vh, 32px) clamp(16px, 4vw, 24px); border-radius: 24px; background: #fff; color: #1c1c1a; display: flex; flex-direction: column; gap: clamp(12px, 2.5vh, 20px); overflow-x: hidden; overflow-y: auto; }
.centro { align-items: center; justify-content: center; text-align: center; }
header { text-align: center; }
h1, p { margin: 0; }
h1 { font-size: clamp(22px, 5vw, 27px); }
header p, .apoio, .status p { margin-top: 7px; color: #6b6b66; font-size: 14px; line-height: 1.45; }
.marca { color: #6b6b66; font-size: 12px; font-weight: 800; letter-spacing: .1em; text-transform: uppercase; }
.consentimento { padding: 16px; border-radius: 14px; background: #f0efe9; display: flex; gap: 10px; text-align: left; font-size: 14px; }
.botao { width: 100%; min-height: 52px; padding: 14px 18px; border: 0; border-radius: 14px; font-size: 17px; font-weight: 700; cursor: pointer; flex: 0 0 auto; }
.botao:disabled { opacity: .4; cursor: default; }
.primario { background: #1c1c1a; color: #fff; }
.secundario { background: #f0efe9; color: #1c1c1a; }
.produtos, .acoes { display: flex; flex-direction: column; gap: 11px; }
.produto { min-height: 76px; padding: 13px 16px; border: 2px solid #e6e4dc; border-radius: 16px; background: #fff; display: flex; align-items: center; justify-content: space-between; gap: 12px; text-align: left; cursor: pointer; }
.produto.selecionado { border-color: #1c1c1a; background: #f5f4ef; }
.produto span strong, .produto small { display: block; }
.produto small { margin-top: 5px; color: #6b6b66; }
.rodape { margin-top: auto; }
.linha { flex-direction: row; }
.camera, .preview { flex: 1 0 240px; min-height: 240px; border-radius: 16px; background: #2c2c2a; display: grid; place-items: center; overflow: hidden; color: #fff; }
.camera video, .preview img { width: 100%; height: 100%; object-fit: cover; }
.qr { align-items: center; }
.qr-area { width: min(280px, 100%); aspect-ratio: 1; padding: clamp(10px, 3vw, 20px); border: 1px solid #e6e4dc; border-radius: 20px; display: grid; place-items: center; flex: 0 0 auto; }
.qr-area canvas { width: min(240px, 100%) !important; height: auto !important; }
.status { text-align: center; }
.erro { color: #b52d2c; text-align: center; }
.papel { width: 100%; flex: 1 0 280px; min-height: 280px; padding: 18px; background: #f8f7f3; border: 1px solid #ddd; display: grid; place-items: center; overflow: hidden; }
.papel img { width: 100%; height: 100%; object-fit: cover; min-height: 0; }
.papel.polaroid { padding: 22px 22px 70px; }
.papel.oito_fotos_3x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(2, 1fr); gap: 5px; padding: 10px; }
.valor { font-size: 25px; }
.tela > canvas { max-width: 100%; height: auto !important; }

@media (max-height: 700px) {
  .tela { padding-top: 16px; padding-bottom: 16px; gap: 10px; }
  .produto { min-height: 66px; padding: 10px 14px; }
  .produto small { margin-top: 2px; }
  .camera, .preview { flex-basis: 210px; min-height: 210px; }
  .papel { flex-basis: 230px; min-height: 230px; }
  .qr-area { width: min(220px, 100%); }
}

@media (max-width: 380px) {
  .aplicacao { padding: 0; }
  .tela { width: 100%; height: 100dvh; border-radius: 0; }
  .linha { flex-direction: column; }
}
</style>
