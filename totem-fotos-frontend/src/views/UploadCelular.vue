<template>
  <main class="upload-celular">
    <section class="cartao-upload">
      <template v-if="linkValido">
        <header>
          <p class="etiqueta">Totem Fotos</p>
          <h1>Enviar sua foto</h1>
          <p class="descricao">Escolha uma imagem JPEG ou PNG de até 10 MB.</p>
        </header>

        <template v-if="!enviado">
          <label class="seletor" :class="{ 'seletor--com-foto': previewUrl }">
            <img v-if="previewUrl" :src="previewUrl" alt="Prévia da foto selecionada">
            <span v-else>
              <strong>Escolher foto</strong>
              <small>Toque para abrir sua galeria</small>
            </span>
            <input
              type="file"
              accept="image/jpeg,image/png"
              :disabled="enviando"
              @change="selecionar"
            >
          </label>

          <p v-if="arquivo" class="arquivo-selecionado">
            {{ arquivo.name }} · {{ tamanhoFormatado }}
          </p>

          <div v-if="enviando" class="progresso" aria-live="polite">
            <div class="progresso-barra" :style="{ width: `${progresso}%` }"></div>
          </div>

          <p v-if="erro" class="mensagem mensagem--erro" role="alert">{{ erro }}</p>

          <button
            type="button"
            class="botao-enviar"
            :disabled="!arquivo || enviando"
            @click="enviar"
          >
            {{ textoBotao }}
          </button>
        </template>

        <div v-else class="sucesso" aria-live="polite">
          <span class="sucesso-icone">✓</span>
          <h2>Foto enviada!</h2>
          <p>Ela já está disponível no totem. Você pode fechar esta página.</p>
        </div>
      </template>

      <div v-else class="link-invalido">
        <h1>Link inválido</h1>
        <p>Volte ao totem e gere um novo QR Code para enviar sua foto.</p>
      </div>

      <p class="privacidade">A foto é usada somente neste pedido.</p>
    </section>
  </main>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'
import {
  aguardarCanalAberto,
  aguardarColetaIce,
  aguardarEspacoNoCanal,
  criarConexaoFoto
} from '../services/webRtcFoto'

const TAMANHO_MAXIMO = 10 * 1024 * 1024
const TIPOS_ACEITOS = ['image/jpeg', 'image/png']

const route = useRoute()
const sessaoId = typeof route.query.sessao === 'string' ? route.query.sessao : ''
const token = typeof route.query.token === 'string' ? route.query.token : ''

const arquivo = ref(null)
const previewUrl = ref(null)
const enviando = ref(false)
const enviado = ref(false)
const progresso = ref(0)
const erro = ref(null)
let conexao = null
let canal = null

const linkValido = computed(() => Boolean(sessaoId && token))
const tamanhoFormatado = computed(() => {
  if (!arquivo.value) return ''
  return `${(arquivo.value.size / 1024 / 1024).toFixed(1).replace('.', ',')} MB`
})
const textoBotao = computed(() => {
  if (!enviando.value) return 'Enviar para o totem'
  return progresso.value >= 100 ? 'Confirmando no totem...' : `Enviando ${progresso.value}%`
})

function limparPreview() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = null
}

function selecionar(event) {
  const selecionado = event.target.files?.[0]
  erro.value = null
  arquivo.value = null
  limparPreview()

  if (!selecionado) return
  if (!TIPOS_ACEITOS.includes(selecionado.type)) {
    erro.value = 'Escolha uma foto no formato JPEG ou PNG.'
    event.target.value = ''
    return
  }
  if (selecionado.size > TAMANHO_MAXIMO) {
    erro.value = 'A foto deve ter no máximo 10 MB.'
    event.target.value = ''
    return
  }

  arquivo.value = selecionado
  previewUrl.value = URL.createObjectURL(selecionado)
}

async function enviar() {
  if (!arquivo.value || enviando.value) return

  enviando.value = true
  progresso.value = 0
  erro.value = null
  try {
    if (!window.RTCPeerConnection) {
      throw new Error('Este navegador não permite a conexão direta com o totem.')
    }

    await conectarAoTotem()
    await transferirFoto(arquivo.value)
    progresso.value = 100
    enviado.value = true
    arquivo.value = null
    limparPreview()
  } catch (e) {
    if (e.response?.status === 410 || e.response?.status === 403) {
      erro.value = 'Este link expirou. Volte ao totem e gere um novo QR Code.'
    } else {
      erro.value = e.response?.data?.detail || e.message || 'Não foi possível conectar ao totem.'
    }
    encerrarConexao()
  } finally {
    enviando.value = false
  }
}

async function conectarAoTotem() {
  encerrarConexao()
  const { data } = await api.get(`/sessoes/${sessaoId}/foto/celular/conexao`, {
    params: { token }
  })

  conexao = criarConexaoFoto()
  const canalRecebido = new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('O totem não abriu o canal de envio.')), 30000)
    conexao.addEventListener('datachannel', event => {
      clearTimeout(timeout)
      resolve(event.channel)
    }, { once: true })
  })

  await conexao.setRemoteDescription(data.oferta)
  await conexao.setLocalDescription(await conexao.createAnswer())
  await aguardarColetaIce(conexao)
  await api.post(`/sessoes/${sessaoId}/foto/celular/conexao/responder`, {
    token,
    resposta: conexao.localDescription.toJSON()
  })

  canal = await canalRecebido
  await aguardarCanalAberto(canal)
}

async function transferirFoto(foto) {
  const TAMANHO_PARTE = 60 * 1024
  const dados = await foto.arrayBuffer()
  const bytes = new Uint8Array(dados)
  let ultimoProgresso = -1

  canal.send(JSON.stringify({
    tipo: 'foto',
    nome: foto.name || 'foto',
    mime: foto.type,
    tamanho: foto.size
  }))

  for (let inicio = 0; inicio < bytes.byteLength; inicio += TAMANHO_PARTE) {
    await aguardarEspacoNoCanal(canal)
    const fim = Math.min(inicio + TAMANHO_PARTE, bytes.byteLength)
    canal.send(bytes.subarray(inicio, fim))
    const percentual = Math.round((fim * 100) / bytes.byteLength)
    if (percentual !== ultimoProgresso) {
      progresso.value = percentual
      ultimoProgresso = percentual
    }
  }

  const confirmacao = aguardarConfirmacao()
  canal.send(JSON.stringify({ tipo: 'fim' }))
  await confirmacao
  encerrarConexao()
}

function aguardarConfirmacao() {
  return new Promise((resolve, reject) => {
    let consultando = false
    const timeout = setTimeout(() => finalizar(new Error('O totem não confirmou o recebimento.')), 20000)
    const polling = setInterval(consultarEstado, 350)

    function recebeu(event) {
      if (typeof event.data !== 'string') return
      try {
        const mensagem = JSON.parse(event.data)
        if (mensagem.tipo === 'recebida') finalizar()
      } catch {
        // Ignora mensagens que não pertencem ao protocolo de confirmação.
      }
    }

    async function consultarEstado() {
      if (consultando) return
      consultando = true
      try {
        const { data } = await api.get(`/sessoes/${sessaoId}`)
        if (data.estado === 'REVISANDO_FOTO') finalizar()
      } catch {
        // A confirmação direta pelo canal ainda pode chegar.
      } finally {
        consultando = false
      }
    }

    function finalizar(erroConfirmacao) {
      clearTimeout(timeout)
      clearInterval(polling)
      canal?.removeEventListener('message', recebeu)
      erroConfirmacao ? reject(erroConfirmacao) : resolve()
    }

    canal.addEventListener('message', recebeu)
    consultarEstado()
  })
}

function encerrarConexao() {
  canal?.close()
  conexao?.close()
  canal = null
  conexao = null
}

onUnmounted(() => {
  encerrarConexao()
  limparPreview()
  arquivo.value = null
})
</script>

<style scoped>
.upload-celular {
  width: 100vw;
  height: 100dvh;
  padding: 24px 16px;
  background: #f0efe9;
  color: #1c1c1a;
  font-family: system-ui, sans-serif;
  overflow-y: auto;
}

.cartao-upload {
  width: min(100%, 420px);
  margin: 0 auto;
  padding: 28px 24px 20px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 12px 40px rgba(0, 0, 0, .1);
}

header,
.sucesso,
.link-invalido {
  text-align: center;
}

.etiqueta {
  color: #6b6b66;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
}

h1 {
  margin-top: 6px;
  font-size: 26px;
}

.descricao,
.link-invalido p,
.sucesso p {
  margin-top: 8px;
  color: #6b6b66;
  font-size: 14px;
  line-height: 1.5;
}

.seletor {
  position: relative;
  min-height: 220px;
  margin-top: 24px;
  border: 2px dashed #c9c7be;
  border-radius: 18px;
  background: #f8f7f3;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.seletor--com-foto {
  border-style: solid;
  background: #1c1c1a;
}

.seletor span,
.seletor small {
  display: block;
  text-align: center;
}

.seletor small {
  margin-top: 5px;
  color: #6b6b66;
}

.seletor img {
  width: 100%;
  height: 280px;
  display: block;
  object-fit: contain;
}

.seletor input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.arquivo-selecionado {
  margin-top: 10px;
  color: #6b6b66;
  font-size: 12px;
  text-align: center;
  overflow-wrap: anywhere;
}

.progresso {
  height: 6px;
  margin-top: 16px;
  border-radius: 999px;
  background: #e6e4dc;
  overflow: hidden;
}

.progresso-barra {
  height: 100%;
  border-radius: inherit;
  background: #1c1c1a;
  transition: width .2s ease;
}

.mensagem {
  margin-top: 14px;
  font-size: 13px;
  line-height: 1.4;
  text-align: center;
}

.mensagem--erro {
  color: #b52d2c;
}

.botao-enviar {
  width: 100%;
  margin-top: 18px;
  padding: 17px;
  border: 0;
  border-radius: 14px;
  background: #1c1c1a;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.botao-enviar:disabled {
  opacity: .4;
  cursor: not-allowed;
}

.sucesso,
.link-invalido {
  padding: 40px 4px;
}

.sucesso-icone {
  width: 64px;
  height: 64px;
  margin: 0 auto 18px;
  border-radius: 50%;
  background: #dff3e9;
  color: #167852;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 700;
}

.sucesso h2 {
  font-size: 24px;
}

.privacidade {
  margin-top: 20px;
  color: #85847e;
  font-size: 11px;
  text-align: center;
}
</style>
