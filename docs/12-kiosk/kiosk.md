# Kiosk

## Estado Atual

O frontend imita uma tela de totem em um navegador, com área de 480×800. Não há modo kiosk real, bloqueio do sistema operacional, watchdog ou recuperação após reinício.

## Fluxo de Rotas

| Rota | Finalidade |
|---|---|
| `/` | Consentimento e início |
| `/home` | Catálogo e origem da foto |
| `/capturar` | Câmera ao vivo ou nativa |
| `/qr-celular` | QR Code para upload |
| `/upload-celular` | Página aberta no celular |
| `/revisar` | Primeira confirmação |
| `/revisar-impressao` | Confirmação do formato |
| `/pagamento` | QR Code Pix e status |
| `/imprimindo` | Simulação de impressão |

## Requisitos para Operação Física

- Inicialização automática de backend, frontend e agente de impressão.
- Navegador em tela cheia e sem navegação externa.
- Tela sempre ativa durante horário de operação.
- Reinício automático após falha.
- Timeout por etapa e reset seguro de sessão.
- Bloqueio de atalhos e acesso ao sistema.
- Tela de manutenção protegida.
- Sincronização de relógio para tokens e pagamentos.

## Rede Local

- Vite escuta em `0.0.0.0:5173` durante desenvolvimento.
- Spring Boot escuta em `0.0.0.0:8080`.
- O frontend usa o mesmo hostname na porta 8080 por padrão.
- Celular e totem precisam de rota entre si.
- Regras de firewall devem ser restritas à rede local.

Vite dev server não é servidor de produção. Uma implantação deve usar build estático e servidor/reverse proxy apropriado.

## Recuperação Pendente

- Sessão abandonada.
- Foto órfã após queda.
- Pagamento confirmado durante reinício.
- Impressora indisponível.
- Falta de disco.
- Celular que não consegue acessar as portas.
- Rota aberta diretamente sem estado no frontend.
