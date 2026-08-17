# Pagamentos

## Estado Atual

O projeto possui o contrato `PixService` e uma implementação `PixServiceStub` ativa somente no profile `local`. Não existe pagamento real, pedido persistente, webhook, conciliação ou estorno.

## Responsabilidade do Backend

- Buscar a sessão.
- Obter o valor de `sessao.getProduto().getValor()`.
- Solicitar cobrança ao adaptador Pix.
- Guardar `txId` na sessão.
- Alterar estado para `AGUARDANDO_PAGAMENTO`.
- Consultar o adaptador e marcar `PAGAMENTO_CONFIRMADO`.

O cliente não envia o valor da cobrança.

## Comportamento do Frontend

- Gera QR Code a partir de `qrCodePayload`.
- Exibe produto e preço.
- Mostra cronômetro visual de 300 segundos.
- Consulta status a cada dois segundos.
- Em confirmação, aguarda 1,5 segundo e chama a finalização.
- Em cancelamento, finaliza a sessão e volta ao catálogo.

## Problemas Antes de Produção

- O stub retorna pago em qualquer consulta.
- O cronômetro chegando a zero não encerra cobrança nem polling.
- Não há webhook ou reconciliação.
- Repetir `POST /pagamento` pode criar cobranças diferentes.
- Erros de polling não são tratados.
- O backend perde a cobrança ao reiniciar.
- Cancelar localmente não necessariamente cancela no provedor.
- A sessão é removida sem manter histórico financeiro.

## Requisitos para Integração Real

- Pedido persistente com valor e moeda.
- Chave idempotente por pedido.
- Expiração controlada pelo servidor/provedor.
- Assinatura e idempotência de webhook.
- Consulta de reconciliação como fallback.
- Estados criado, pendente, confirmado, expirado, cancelado, estornado e falhou.
- Política para pagamento recebido após expiração.
- Política de reembolso por falha de impressão.
- Auditoria sem registrar segredos ou dados excessivos.

## Regra Central

Impressão só pode ser liberada após confirmação verificável do provedor e precisa continuar rastreável mesmo se a interface ou o backend reiniciar.
