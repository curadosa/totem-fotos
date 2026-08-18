# Totem de Fotos Electron

Alternativa desktop independente do frontend web e do backend Spring. O aplicativo possui:

- renderer Vue em `http://localhost:5173`;
- servidor HTTP móvel e sinalização WebSocket em `http://0.0.0.0:9000`;
- estado, catálogo e pagamento simulado locais;
- transferência da foto diretamente do celular ao renderer por DataChannel WebRTC.

O processo principal Electron retransmite apenas SDP e candidatos ICE. Ele não recebe a foto e limita mensagens WebSocket a 64 KiB.

## Executar

```powershell
npm install
npm run dev
```

Quando houver mais de uma interface de rede, defina o IP que o celular alcança:

```powershell
$env:VITE_MOBILE_URL = 'http://192.168.0.10:9000'
npm run dev
```

Não é necessário iniciar `totem-fotos-backend` ou `totem-fotos-frontend`. O firewall do totem deve permitir TCP de entrada na porta 9000 para a rede local.

## Validar

```powershell
npm run build
npm run test:signal
```

O segundo comando espera que o servidor Electron da porta 9000 já esteja em execução.
