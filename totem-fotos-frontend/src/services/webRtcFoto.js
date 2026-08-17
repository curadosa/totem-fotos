const TEMPO_LIMITE_ICE = 10000

export function criarConexaoFoto() {
  return new RTCPeerConnection({ iceServers: [] })
}

export function aguardarColetaIce(conexao) {
  if (conexao.iceGatheringState === 'complete') return Promise.resolve()

  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => finalizar(new Error('Tempo excedido ao preparar a conexão.')), TEMPO_LIMITE_ICE)

    function aoMudarEstado() {
      if (conexao.iceGatheringState === 'complete') finalizar()
    }

    function finalizar(erro) {
      clearTimeout(timeout)
      conexao.removeEventListener('icegatheringstatechange', aoMudarEstado)
      erro ? reject(erro) : resolve()
    }

    conexao.addEventListener('icegatheringstatechange', aoMudarEstado)
  })
}

export function aguardarCanalAberto(canal, tempoLimite = 30000) {
  if (canal.readyState === 'open') return Promise.resolve()

  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => finalizar(new Error('O totem não respondeu a tempo.')), tempoLimite)

    function abriu() { finalizar() }
    function falhou() { finalizar(new Error('Não foi possível abrir a conexão direta.')) }
    function finalizar(erro) {
      clearTimeout(timeout)
      canal.removeEventListener('open', abriu)
      canal.removeEventListener('error', falhou)
      erro ? reject(erro) : resolve()
    }

    canal.addEventListener('open', abriu)
    canal.addEventListener('error', falhou)
  })
}

export function aguardarEspacoNoCanal(canal) {
  const LIMITE_BUFFER = 2 * 1024 * 1024
  if (canal.bufferedAmount <= LIMITE_BUFFER) return Promise.resolve()

  canal.bufferedAmountLowThreshold = 1024 * 1024
  return new Promise((resolve, reject) => {
    function liberou() { finalizar() }
    function fechou() { finalizar(new Error('A conexão foi encerrada durante o envio.')) }
    function finalizar(erro) {
      canal.removeEventListener('bufferedamountlow', liberou)
      canal.removeEventListener('close', fechou)
      erro ? reject(erro) : resolve()
    }

    canal.addEventListener('bufferedamountlow', liberou, { once: true })
    canal.addEventListener('close', fechou, { once: true })
  })
}
