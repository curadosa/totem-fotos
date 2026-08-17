# Escalabilidade

## Escopo Atual

A arquitetura atual foi desenhada para um protótipo de cabine única. `ConcurrentHashMap` e disco local não devem ser tratados como solução multi-ponto.

## Limites Imediatos

- Sessões pertencem a uma única instância do backend.
- Fotos pertencem ao disco da máquina local.
- Não existe recuperação após reinício.
- Polling de upload e pagamento cresce linearmente com sessões abertas.
- Não há controle de espaço em disco ou limpeza de diretórios vazios.

## Evolução Recomendada por Necessidade

### Piloto de um totem

- Persistir pedido, produto, pagamento e impressão em banco local.
- Manter fotos em armazenamento temporário local com rotina de limpeza.
- Implementar agente de impressão local.
- Adicionar logs estruturados, health check e watchdog.

### Vários totens em um local

- Identificar dispositivo e ponto.
- Centralizar configuração e pedidos ou usar sincronização confiável.
- Separar o agente de impressão do navegador.
- Evitar duplicação de impressão com chave idempotente.

### Vários pontos

- API central autenticada.
- Banco transacional e storage com retenção.
- Fila de jobs e eventos.
- Configuração por ponto e catálogo versionado.
- Observabilidade e painel administrativo.

## Princípios de Evolução

- Escalar somente após medir o piloto.
- Separar sessão de interface, pedido comercial, pagamento e impressão.
- Não enviar fotos à nuvem sem finalidade, base legal e política de retenção definidas.
- Usar adaptadores para provedor Pix e impressora.
- Tornar operações financeiras e físicas idempotentes.

## Sinais para Evoluir

- Pedidos pagos são perdidos em reinícios.
- Mais de um totem precisa usar a mesma impressora.
- Operador não consegue diagnosticar falhas localmente.
- O volume de polling afeta a API.
- Fotos permanecem no disco após sessões encerradas.

## Decisões Pendentes

- Banco local, central ou híbrido.
- Agente de impressão Windows, serviço Java ou componente específico do fabricante.
- Política offline.
- Provedor Pix.
- Topologia de rede e HTTPS local.
