import { app, BrowserWindow, dialog, screen, session } from 'electron'
import { createReadStream } from 'node:fs'
import { createServer } from 'node:http'
import { extname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { WebSocket, WebSocketServer } from 'ws'

const RENDERER_URL = process.env.ELECTRON_RENDERER_URL || 'http://localhost:5173'
const RENDERER_ORIGIN = new URL(RENDERER_URL).origin
const PORTA_MOBILE = Number(process.env.ELECTRON_MOBILE_PORT || 9000)
const diretorioMobile = fileURLToPath(new URL('../mobile', import.meta.url))
const pares = new Map()
let servidorMobile

app.commandLine.appendSwitch('disable-features', 'WebRtcHideLocalIpsWithMdns')

function responderArquivo(resposta, arquivo) {
  const tipos = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8'
  }
  resposta.writeHead(200, {
    'Content-Type': tipos[extname(arquivo)] || 'application/octet-stream',
    'Cache-Control': 'no-store'
  })
  createReadStream(arquivo).on('error', () => {
    if (!resposta.headersSent) resposta.writeHead(404)
    resposta.end()
  }).pipe(resposta)
}

function iniciarServidorMobile() {
  const servidor = createServer((requisicao, resposta) => {
    const caminho = new URL(requisicao.url, `http://${requisicao.headers.host}`).pathname
    if (caminho === '/health') {
      resposta.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' })
      resposta.end('ok')
      return
    }
    if (caminho === '/' || caminho === '/upload' || caminho === '/upload-celular') {
      responderArquivo(resposta, join(diretorioMobile, 'index.html'))
      return
    }
    if (caminho === '/app.js' || caminho === '/style.css') {
      responderArquivo(resposta, join(diretorioMobile, caminho.slice(1)))
      return
    }
    resposta.writeHead(404)
    resposta.end()
  })

  const sockets = new WebSocketServer({ server: servidor, path: '/signal', maxPayload: 64 * 1024 })
  sockets.on('connection', (socket, requisicao) => {
    const url = new URL(requisicao.url, `http://${requisicao.headers.host}`)
    const sessaoId = url.searchParams.get('session')
    const papel = url.searchParams.get('role')
    if (!sessaoId || !['totem', 'mobile'].includes(papel) || sessaoId.length > 100) {
      socket.close(1008, 'Sessão inválida')
      return
    }

    const par = pares.get(sessaoId) || { totem: null, mobile: null }
    par[papel]?.close(1000, 'Nova conexão')
    par[papel] = socket
    pares.set(sessaoId, par)

    const outroPapel = papel === 'totem' ? 'mobile' : 'totem'
    if (par[outroPapel]?.readyState === WebSocket.OPEN) {
      const pronta = JSON.stringify({ type: 'peer-ready' })
      socket.send(pronta)
      par[outroPapel].send(pronta)
    }

    socket.on('message', (mensagem, binaria) => {
      const destino = pares.get(sessaoId)?.[outroPapel]
      if (destino?.readyState === WebSocket.OPEN && !binaria) destino.send(mensagem.toString())
    })

    socket.on('close', () => {
      const atual = pares.get(sessaoId)
      if (!atual || atual[papel] !== socket) return
      atual[papel] = null
      if (!atual.totem && !atual.mobile) pares.delete(sessaoId)
    })
  })

  servidor.on('error', erro => {
    dialog.showErrorBox(
      'Servidor móvel indisponível',
      `Não foi possível abrir a porta ${PORTA_MOBILE}. Feche outra aplicação que esteja usando essa porta.\n\n${erro.message}`
    )
    app.quit()
  })
  servidor.listen(PORTA_MOBILE, '0.0.0.0')
  servidorMobile = servidor
}

function criarJanela() {
  const area = screen.getPrimaryDisplay().workAreaSize
  const janela = new BrowserWindow({
    width: Math.min(520, area.width),
    height: Math.min(860, area.height),
    minWidth: Math.min(360, area.width),
    minHeight: Math.min(500, area.height),
    center: true,
    resizable: true,
    autoHideMenuBar: true,
    backgroundColor: '#1c1c1a',
    show: false,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  })

  janela.once('ready-to-show', () => janela.show())
  janela.webContents.setWindowOpenHandler(() => ({ action: 'deny' }))
  janela.webContents.on('will-navigate', (evento, url) => {
    if (new URL(url).origin !== RENDERER_ORIGIN) evento.preventDefault()
  })
  janela.loadURL(RENDERER_URL)
}

app.whenReady().then(() => {
  iniciarServidorMobile()
  session.defaultSession.setPermissionCheckHandler((_webContents, permissao, origem) => {
    return permissao === 'media' && new URL(origem).origin === RENDERER_ORIGIN
  })
  session.defaultSession.setPermissionRequestHandler((webContents, permissao, responder) => {
    const origem = new URL(webContents.getURL()).origin
    responder(permissao === 'media' && origem === RENDERER_ORIGIN)
  })
  criarJanela()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) criarJanela()
  })
})

app.on('before-quit', () => servidorMobile?.close())
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
