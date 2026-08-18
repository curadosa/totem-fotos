# Requisitos

## Requisitos Funcionais

### Sessão e catálogo

- RF-001 — O sistema deve exibir Polaroid, Normal 10×15 e conjunto com 8 fotos 3×4.
- RF-002 — O sistema deve exibir respectivamente R$ 4,50, R$ 5,50 e R$ 19,90.
- RF-003 — O backend deve associar produto e preço à sessão.
- RF-004 — O usuário deve escolher entre captura e envio pelo celular.

### Foto

- RF-010 — O sistema deve tentar abrir a câmera frontal com preview e contagem de três segundos.
- RF-011 — Quando `getUserMedia` não estiver disponível, deve informar a falha da câmera do totem e permitir nova tentativa.
- RF-012 — O sistema deve transferir a foto diretamente do celular ao totem por WebRTC, com negociação iniciada por QR Code e token de cinco minutos.
- RF-013 — O usuário deve revisar e poder refazer a foto original.
- RF-014 — O usuário deve revisar e poder refazer o formato de impressão.
- RF-015 — Refazer deve revogar a foto local atual e retornar a `/capturar`.
- RF-016 — A prévia 3×4 deve mostrar oito cópias em grade 4×2.

### Pagamento e impressão

- RF-020 — A cobrança deve usar o preço do produto armazenado no backend.
- RF-021 — O QR Code Pix deve exibir produto, valor e contagem regressiva.
- RF-022 — O sistema deve consultar o status até confirmação, cancelamento ou expiração.
- RF-023 — Somente pagamento confirmado pode liberar a impressão.
- RF-024 — O arquivo final deve reproduzir proporção, recorte e disposição mostrados na segunda revisão.
- RF-025 — O sistema deve informar sucesso ou falha da impressão.
- RF-026 — Ao concluir ou cancelar, deve remover foto e sessão.

RF-024 e RF-025 ainda não estão implementados; a tela atual apenas simula impressão.

## Requisitos Não Funcionais

- RNF-001 — A interface deve caber em 480×800 sem rolagem no totem homologado.
- RNF-002 — Botões devem ser adequados ao toque e mensagens não podem depender apenas de cor.
- RNF-003 — O backend local deve escutar na rede necessária ao celular, com firewall configurado.
- RNF-004 — Produção deve usar HTTPS e origens CORS explícitas.
- RNF-005 — Fotos recebidas devem ter limite e validação local de MIME, dimensões e conteúdo.
- RNF-006 — Cobrança, confirmação e impressão devem ser idempotentes.
- RNF-007 — Uma reinicialização não pode perder pedido pago.
- RNF-008 — Logs não devem conter fotos, tokens ou payloads de pagamento.
- RNF-009 — Fotos devem ser mantidas apenas na memória da aba e limpas em refazer, cancelar, erro e finalização.

## Regras de Negócio

- RN-001 — Cada sessão possui exatamente um produto.
- RN-002 — O conjunto 3×4 custa R$ 19,90 pelo conjunto, não por cópia.
- RN-003 — Uma foto confirmada ainda pode ser refeita na revisão de impressão.
- RN-004 — Cancelar o pagamento encerra a sessão e remove a foto.
- RN-005 — O token de sinalização WebRTC só vale para a sessão que o emitiu e por cinco minutos.
- RN-006 — Foto e sessão não devem ser finalizadas antes de a impressora confirmar o resultado; o protótipo atual ainda viola essa regra.

## Critérios de Aceite Essenciais

- Alterar preço no frontend não altera o valor cobrado pelo backend.
- Os três formatos exibem prévias distintas.
- Acesso por IP no celular consegue chamar o backend pelo mesmo host na porta 8080.
- Falha da câmera do totem exibe orientação e permite tentar novamente.
- Foto transferida por conexão direta aparece nas duas revisões sem passar pelo backend.
- Refazer em qualquer revisão revoga a referência local anterior.
