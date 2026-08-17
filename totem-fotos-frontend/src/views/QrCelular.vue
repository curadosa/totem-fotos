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
import { definirFotoLocal, sessao } from '../services/sessaoState'
import { aguardarColetaIce, criarConexaoFoto } from '../services/webRtcFoto'

const TAMANHO_MAXIMO = 10 * 1024 * 1024
const TIPOS_ACEITOS = ['image/jpeg', 'image/png']

const router = useRouter()
const canvas = ref(null)
const carregando = ref(true)
const erro = ref(null)
const segundosRestantes = ref(300)

let polling = null
let relogio = null
let consultando = false
let conexao = null
let canal = null
let metadadosFoto = null
let partesFoto = []
let bytesRecebidos = 0

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

function encerrarConexao() {
  canal?.close()
  conexao?.removeEventListener('connectionstatechange', acompanharConexao)
  conexao?.close()
  canal = null
  conexao = null
  metadadosFoto = null
  partesFoto = []
  bytesRecebidos = 0
}

function encerrarTudo() {
  pararTemporizadores()
  encerrarConexao()
}

async function iniciar() {
  encerrarTudo()
  carregando.value = true
  erro.value = null
  segundosRestantes.value = 300

  try {
    if (!window.RTCPeerConnection) {
      throw new Error('Este navegador não oferece conexão direta.')
    }

    conexao = criarConexaoFoto()
    canal = conexao.createDataChannel('foto', { ordered: true })
    prepararRecebimento(canal)
    conexao.addEventListener('connectionstatechange', acompanharConexao)

    await conexao.setLocalDescription(await conexao.createOffer())
    await aguardarColetaIce(conexao)

    const { data } = await api.post(
      `/sessoes/${sessao.id}/foto/celular/iniciar`,
      conexao.localDescription.toJSON()
    )
    segundosRestantes.value = data.expiraEmSegundos

    const origemPublica = import.meta.env.VITE_PUBLIC_URL || window.location.origin
    const url = new URL('/upload-celular', origemPublica)
    url.searchParams.set('sessao', sessao.id)
    url.searchParams.set('token', data.token)
    await QRCode.toCanvas(canvas.value, url.toString(), { width: 240, margin: 1 })

    carregando.value = false
    iniciarRelogio()
    polling = setInterval(consultarResposta, 300)
  } catch (e) {
    encerrarTudo()
    carregando.value = false
    erro.value = e.message || 'Não foi possível gerar o link de envio.'
  }
}

function iniciarRelogio() {
  relogio = setInterval(() => {
    segundosRestantes.value -= 1
    if (segundosRestantes.value <= 0) {
      falharConexao('Este QR Code expirou.')
    }
  }, 1000)
}

async function consultarResposta() {
  if (consultando) return
  consultando = true
  try {
    const { data } = await api.get(`/sessoes/${sessao.id}/foto/celular/conexao/resposta`)
    if (data.resposta && !conexao.remoteDescription) {
      clearInterval(polling)
      polling = null
      await conexao.setRemoteDescription(data.resposta)
    }
  } catch (e) {
    encerrarTudo()
    erro.value = 'A conexão com o totem foi interrompida.'
  } finally {
    consultando = false
  }
}

function prepararRecebimento(canalFoto) {
  canalFoto.binaryType = 'arraybuffer'
  canalFoto.addEventListener('message', async event => {
    try {
      if (typeof event.data === 'string') {
        const mensagem = JSON.parse(event.data)
        if (mensagem.tipo === 'foto') {
          validarMetadados(mensagem)
          metadadosFoto = mensagem
          partesFoto = []
          bytesRecebidos = 0
        } else if (mensagem.tipo === 'fim') {
          await concluirRecebimento()
        }
        return
      }

      if (!metadadosFoto) throw new Error('Dados da foto recebidos fora de ordem.')
      bytesRecebidos += event.data.byteLength
      if (bytesRecebidos > metadadosFoto.tamanho || bytesRecebidos > TAMANHO_MAXIMO) {
        throw new Error('O tamanho recebido não corresponde à foto selecionada.')
      }
      partesFoto.push(event.data)
    } catch (e) {
      falharConexao(e.message)
    }
  })
}

function validarMetadados(dados) {
  if (!Number.isInteger(dados.tamanho) || dados.tamanho <= 0 || dados.tamanho > TAMANHO_MAXIMO) {
    throw new Error('A foto deve ter no máximo 10 MB.')
  }
  if (!TIPOS_ACEITOS.includes(dados.mime)) {
    throw new Error('Envie uma foto JPEG ou PNG.')
  }
}

async function concluirRecebimento() {
  if (!metadadosFoto || bytesRecebidos !== metadadosFoto.tamanho) {
    throw new Error('A transferência da foto ficou incompleta.')
  }

  const foto = new File(partesFoto, metadadosFoto.nome || 'foto', { type: metadadosFoto.mime })
  definirFotoLocal(foto)
  pararTemporizadores()
  try {
    await api.post(`/sessoes/${sessao.id}/foto/celular/conexao/concluir`)
  } catch {
    // A confirmação direta abaixo ainda informa o celular se a sinalização falhar.
  }
  if (canal?.readyState === 'open') {
    canal.send(JSON.stringify({ tipo: 'recebida' }))
  }
  router.push('/revisar')
}

function acompanharConexao() {
  if (conexao && ['failed', 'closed'].includes(conexao.connectionState)) {
    falharConexao('Não foi possível manter a conexão direta com o celular.')
  }
}

function falharConexao(mensagem) {
  encerrarTudo()
  carregando.value = false
  erro.value = mensagem
}

function voltar() {
  encerrarTudo()
  router.back()
}

onMounted(iniciar)
onUnmounted(encerrarTudo)
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
