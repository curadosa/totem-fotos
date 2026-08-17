# Android e Dispositivos Móveis

## Estado Atual

Não existe aplicativo Android nativo. O sistema é uma aplicação web acessada pelo navegador do totem ou do celular.

Este diretório é mantido para conservar a mesma taxonomia documental do projeto de referência e registrar decisões futuras sobre dispositivos Android.

## Comportamento no Navegador Móvel

- A API usa, por padrão, o hostname que entregou o frontend e a porta 8080.
- `getUserMedia` normalmente exige HTTPS quando o endereço não é `localhost`.
- Quando a câmera ao vivo falha, `/capturar` oferece `input type="file"`, `accept="image/*"` e `capture="user"`.
- O upload por QR Code abre uma rota web, sem instalação de app.

## Limitações Atuais

- HTTP local pode bloquear câmera ao vivo e recursos de segurança.
- O tamanho fixo 480×800 pode não caber em todos os aparelhos.
- Não há modo kiosk, tela sempre ativa ou bloqueio de navegação.
- Atualização da página perde o estado reativo do frontend.
- Não há controle nativo de orientação, brilho ou permissões.

## Caminhos Futuros

### Web em modo kiosk

Preferível para o primeiro piloto se o hardware executar Chrome/Edge e um sistema operacional com impressão suportada por agente local.

### PWA

Pode melhorar instalação e cache, mas não resolve sozinha impressão silenciosa, lock task ou todas as limitações de câmera.

### Wrapper Android

Avaliar apenas se o hardware escolhido for Android e exigir:

- Lock task.
- Integração nativa de câmera.
- USB, Bluetooth ou SDK específico da impressora.
- Atualização controlada e watchdog.

## Testes Obrigatórios em Android

- Chrome atual e WebView homologada.
- Permissão concedida, negada e revogada.
- Acesso HTTP e HTTPS.
- Captura frontal e seleção da galeria.
- Rotação e retorno do app.
- Arquivos JPEG, PNG e formatos do fabricante.
- Rede Wi-Fi local sem internet.
