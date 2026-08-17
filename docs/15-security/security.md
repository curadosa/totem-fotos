# Segurança e Privacidade

## Dados Tratados

- Foto do usuário.
- Identificador UUID de sessão.
- Token temporário de upload.
- Identificador e payload da cobrança Pix.
- Futuramente, registros de pedido, impressão e estorno.

## Controles Implementados

- Sessões usam UUID aleatório.
- Upload por QR Code exige token UUID com expiração de cinco minutos.
- Refazer e finalizar tentam remover a foto.
- Fotos são separadas por data e sessão no disco.
- O frontend informa a finalidade e a exclusão no início.
- Preço da cobrança é determinado pelo backend.

## Riscos Atuais

### Críticos para produção

- Tráfego local usa HTTP.
- CORS permite qualquer origem.
- Endpoints de sessão e foto não exigem autenticação além do UUID na URL.
- GET da foto não usa token.
- Upload não limita tamanho, tipo real, dimensões ou conteúdo.
- Sessões e pagamentos não são persistidos nem auditados.
- Não há proteção contra abuso, rate limit ou limite de sessões.

### Operacionais

- Falha ao excluir arquivo é silenciosa.
- Não há tarefa para limpar fotos órfãs.
- Diretórios vazios permanecem.
- O token continua armazenado após uso.
- JPEG e PNG são identificados pelo conteúdo e armazenados com a extensão correspondente; HEIC ainda não é aceito.
- O profile `cloud` não define uma política de segredos.

## Controles Necessários

- HTTPS no totem e no acesso móvel.
- CORS limitado às origens implantadas.
- Token de leitura/alteração ou sessão segura para recursos de foto.
- Token de upload de uso único e invalidado após sucesso.
- Validação e normalização de imagem em processo seguro.
- Limites de request, sessão e taxa.
- Persistência e idempotência de pagamento/impressão.
- Segredos fora do repositório e separados por ambiente.
- Logs sanitizados e auditoria de ações financeiras.
- Job periódico de retenção com métricas e alerta de falha.

## Privacidade

- Definir prazo máximo mensurável para imagens ativas e órfãs.
- Não fazer backup de fotos temporárias por padrão.
- Separar dados transacionais da imagem.
- Documentar finalidade, retenção e canal de atendimento conforme legislação aplicável.
- Evitar biometria, reconhecimento facial ou uso secundário sem decisão e base legal específicas.

## Modelo de Ameaça Inicial

- Pessoa na rede enumera ou acessa sessões conhecidas.
- Site malicioso chama a API por CORS aberto.
- Arquivo grande ou malformado esgota disco/memória.
- Pagamento falso ou repetido libera impressão.
- Reimpressão não autorizada expõe imagem.
- Operador acessa fotos temporárias diretamente no disco.
