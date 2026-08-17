# Segurança e Privacidade

## Dados Tratados

- Foto do usuário.
- Identificador UUID de sessão.
- Token temporário de sinalização WebRTC.
- Identificador e payload da cobrança Pix.
- Futuramente, registros de pedido, impressão e estorno.

## Controles Implementados

- Sessões usam UUID aleatório.
- A negociação iniciada pelo QR Code exige token UUID com expiração de cinco minutos.
- A foto é transferida diretamente por WebRTC e não passa pelo backend.
- Refazer, cancelar e finalizar revogam a URL `blob:` e removem a referência local.
- O frontend informa a finalidade e a exclusão no início.
- Preço da cobrança é determinado pelo backend.

## Riscos Atuais

### Críticos para produção

- Tráfego local usa HTTP.
- CORS permite qualquer origem.
- Endpoints de sessão e sinalização dependem somente do UUID e, no lado móvel, do token na URL.
- A validação local limita tipo declarado e tamanho, mas ainda não verifica integralmente conteúdo e dimensões.
- Sessões e pagamentos não são persistidos nem auditados.
- Não há proteção contra abuso, rate limit ou limite de sessões.

### Operacionais

- Recarregar o navegador encerra a sessão visual e perde a foto local.
- O WebRTC sem servidor TURN depende da conectividade direta da rede local.
- Fechamento abrupto pode manter a sinalização em memória até a sessão expirar ou ser finalizada.
- HEIC ainda não é aceito.
- O profile `cloud` não define uma política de segredos.

## Controles Necessários

- HTTPS no totem e no acesso móvel.
- CORS limitado às origens implantadas.
- Autorização mais forte para consultar e alterar sessões.
- Token de sinalização de uso único e invalidado após sucesso.
- Validação e normalização de imagem em processo seguro.
- Limites de request, sessão e taxa.
- Persistência e idempotência de pagamento/impressão.
- Segredos fora do repositório e separados por ambiente.
- Logs sanitizados e auditoria de ações financeiras.
- Expiração automática de sessões e sinalizações abandonadas.

## Privacidade

- Garantir limpeza local em encerramento, erro, cancelamento e timeout.
- Não persistir nem incluir fotos em backup, cache ou telemetria.
- Separar dados transacionais da imagem.
- Documentar finalidade, retenção e canal de atendimento conforme legislação aplicável.
- Evitar biometria, reconhecimento facial ou uso secundário sem decisão e base legal específicas.

## Modelo de Ameaça Inicial

- Pessoa na rede enumera ou acessa sessões conhecidas.
- Site malicioso chama a API por CORS aberto.
- Arquivo grande ou malformado esgota a memória do navegador.
- Pagamento falso ou repetido libera impressão.
- Reimpressão não autorizada expõe imagem.
- Extensão maliciosa do navegador ou acesso físico ao totem lê a foto enquanto a sessão está ativa.
