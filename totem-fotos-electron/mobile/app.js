const TAMANHO_MAXIMO = 10 * 1024 * 1024
const TIPOS_ACEITOS = ['image/jpeg', 'image/png']
const TEMPO_LIMITE_ICE = 10000
const sessaoId = new URLSearchParams(location.search).get('session')

const input = document.querySelector('#arquivo')
const preview = document.querySelector('#preview')
const convite = document.querySelector('#convite')
const seletor = document.querySelector('#seletor')
const nome = document.querySelector('#nome')
const mensagem = document.querySelector('#mensagem')
const botao = document.querySelector('#enviar')
const progresso = document.querySelector('#progresso')
const barra = document.querySelector('#barra')
const conteudo = document.querySelector('#conteudo')
const sucesso = document.querySelector('#sucesso')

let arquivo
let previewUrl
let socket
let conexao
let canal
let resolverCanal
let rejeitarCanal
let canalPronto = novaEsperaCanal()
let temporizadorConexao

function novaEsperaCanal() {
  return new Promise((resolve, reject) => {
    resolverCanal = resolve
    rejeitarCanal = reject
  })
}

function mostrarMensagem(texto, erro = false) {
  mensagem.textContent = texto
  mensagem.classList.toggle('erro', erro)
}

function enviarSinal(dados) {
  if (socket?.readyState === WebSocket.OPEN) socket.send(JSON.stringify(dados))
}

function aguardarColetaIce(peer) {
  if (peer.iceGatheringState === 'complete') return Promise.resolve()

  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => finalizar(new Error('Tempo excedido ao preparar a conexão direta.')), TEMPO_LIMITE_ICE)

    function aoMudarEstado() {
      if (peer.iceGatheringState === 'complete') finalizar()
    }

    function finalizar(erro) {
      clearTimeout(timeout)
      peer.removeEventListener('icegatheringstatechange', aoMudarEstado)
      erro ? reject(erro) : resolve()
    }

    peer.addEventListener('icegatheringstatechange', aoMudarEstado)
  })
}

function criarConexao() {
  conexao?.close()
  canal?.close()
  canal = null
  canalPronto = novaEsperaCanal()
  conexao = new RTCPeerConnection({ iceServers: [] })
  conexao.addEventListener('datachannel', event => {
    canal = event.channel
    canal.binaryType = 'arraybuffer'
    canal.addEventListener('open', () => {
      clearTimeout(temporizadorConexao)
      mostrarMensagem('Conectado ao totem.')
      botao.disabled = !arquivo
      resolverCanal(canal)
    }, { once: true })
    canal.addEventListener('close', () => rejeitarCanal(new Error('A conexão com o totem foi encerrada.')), { once: true })
  })
  conexao.addEventListener('connectionstatechange', () => {
    if (conexao.connectionState === 'failed') {
      clearTimeout(temporizadorConexao)
      mostrarMensagem('Não foi possível criar a conexão direta. Verifique se celular e totem estão na mesma rede.', true)
    }
  })
}

async function receberSinal(event) {
  const dados = JSON.parse(event.data)
  if (dados.type === 'peer-ready') {
    mostrarMensagem('Preparando conexão direta...')
    return
  }
  if (dados.type === 'offer') {
    criarConexao()
    await conexao.setRemoteDescription(dados.description)
    await conexao.setLocalDescription(await conexao.createAnswer())
    mostrarMensagem('Preparando resposta direta...')
    await aguardarColetaIce(conexao)
    enviarSinal({ type: 'answer', description: conexao.localDescription })
    clearTimeout(temporizadorConexao)
    temporizadorConexao = setTimeout(() => {
      if (canal?.readyState !== 'open') {
        mostrarMensagem('A sinalização funcionou, mas não existe rota WebRTC direta entre as redes.', true)
      }
    }, 15000)
    return
  }
}

function conectarSinalizacao() {
  if (!sessaoId || !window.RTCPeerConnection) {
    mostrarMensagem('Link inválido ou navegador incompatível.', true)
    return
  }
  const protocolo = location.protocol === 'https:' ? 'wss:' : 'ws:'
  socket = new WebSocket(`${protocolo}//${location.host}/signal?role=mobile&session=${encodeURIComponent(sessaoId)}`)
  socket.addEventListener('message', event => receberSinal(event).catch(() => {
    mostrarMensagem('Não foi possível negociar a conexão com o totem.', true)
  }))
  socket.addEventListener('close', () => {
    if (!sucesso.hidden) return
    mostrarMensagem('A conexão com o totem foi encerrada.', true)
    botao.disabled = true
  })
  socket.addEventListener('error', () => mostrarMensagem('Não foi possível acessar o totem.', true))
}

input.addEventListener('change', event => {
  const selecionado = event.target.files?.[0]
  arquivo = null
  botao.disabled = true
  if (previewUrl) URL.revokeObjectURL(previewUrl)
  previewUrl = null
  preview.hidden = true
  convite.hidden = false
  seletor.classList.remove('com-foto')
  nome.textContent = ''

  if (!selecionado) return
  if (!TIPOS_ACEITOS.includes(selecionado.type)) {
    mostrarMensagem('Escolha uma foto JPEG ou PNG.', true)
    return
  }
  if (selecionado.size > TAMANHO_MAXIMO) {
    mostrarMensagem('A foto deve ter no máximo 10 MB.', true)
    return
  }
  arquivo = selecionado
  previewUrl = URL.createObjectURL(selecionado)
  preview.src = previewUrl
  preview.hidden = false
  convite.hidden = true
  seletor.classList.add('com-foto')
  nome.textContent = `${selecionado.name} · ${(selecionado.size / 1024 / 1024).toFixed(1).replace('.', ',')} MB`
  botao.disabled = canal?.readyState !== 'open'
})

botao.addEventListener('click', async () => {
  if (!arquivo) return
  botao.disabled = true
  progresso.hidden = false
  barra.style.width = '0%'
  mostrarMensagem('Enviando...')
  try {
    const canalAberto = canal?.readyState === 'open' ? canal : await Promise.race([
      canalPronto,
      new Promise((_, reject) => setTimeout(() => reject(new Error('O totem não respondeu a tempo.')), 30000))
    ])
    const bytes = new Uint8Array(await arquivo.arrayBuffer())
    canalAberto.send(JSON.stringify({ tipo: 'foto', nome: arquivo.name, mime: arquivo.type, tamanho: arquivo.size }))
    for (let inicio = 0; inicio < bytes.length; inicio += 60 * 1024) {
      while (canalAberto.bufferedAmount > 2 * 1024 * 1024) {
        await new Promise(resolve => setTimeout(resolve, 20))
      }
      const fim = Math.min(inicio + 60 * 1024, bytes.length)
      canalAberto.send(bytes.subarray(inicio, fim))
      barra.style.width = `${Math.round((fim * 100) / bytes.length)}%`
    }
    const confirmacao = new Promise((resolve, reject) => {
      const timeout = setTimeout(() => reject(new Error('O totem não confirmou o recebimento.')), 20000)
      canalAberto.addEventListener('message', function recebeu(event) {
        if (typeof event.data !== 'string') return
        const dados = JSON.parse(event.data)
        if (dados.tipo === 'recebida') {
          clearTimeout(timeout)
          canalAberto.removeEventListener('message', recebeu)
          resolve()
        }
      })
    })
    canalAberto.send(JSON.stringify({ tipo: 'fim' }))
    await confirmacao
    conteudo.hidden = true
    sucesso.hidden = false
    if (previewUrl) URL.revokeObjectURL(previewUrl)
  } catch (erro) {
    mostrarMensagem(erro.message || 'Não foi possível enviar a foto.', true)
    botao.disabled = false
  }
})

window.addEventListener('beforeunload', () => {
  clearTimeout(temporizadorConexao)
  if (previewUrl) URL.revokeObjectURL(previewUrl)
  canal?.close()
  conexao?.close()
  socket?.close()
})

conectarSinalizacao()
