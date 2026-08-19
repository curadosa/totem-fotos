import WebSocket from 'ws'

const sessao = `teste-${Date.now()}`

function abrir(papel) {
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(`ws://127.0.0.1:5173/signal?role=${papel}&session=${sessao}`)
    const primeira = new Promise((resolveMensagem, rejectMensagem) => {
      socket.once('message', dados => resolveMensagem(JSON.parse(dados.toString())))
      socket.once('error', rejectMensagem)
    })
    socket.once('open', () => resolve({ socket, primeira }))
    socket.once('error', reject)
  })
}

function proximaMensagem(socket) {
  return new Promise((resolve, reject) => {
    socket.once('message', dados => resolve(JSON.parse(dados.toString())))
    socket.once('error', reject)
  })
}

const conexaoTotem = await abrir('totem')
const conexaoCelular = await abrir('mobile')
const totem = conexaoTotem.socket
const celular = conexaoCelular.socket

if ((await conexaoTotem.primeira).type !== 'peer-ready' || (await conexaoCelular.primeira).type !== 'peer-ready') {
  throw new Error('O pareamento não foi confirmado.')
}

const recebida = proximaMensagem(celular)
totem.send(JSON.stringify({ type: 'offer', teste: true }))
const sinal = await recebida
if (sinal.type !== 'offer' || sinal.teste !== true) throw new Error('A sinalização não foi retransmitida.')

totem.close()
celular.close()
console.log('sinalizacao-electron=ok')
