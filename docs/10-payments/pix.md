# Pix

## Contrato Atual

```java
CobrancaPix gerarCobranca(String sessaoId, BigDecimal valor);
boolean foiPaga(String txId);
```

`CobrancaPix` contém `txId`, `qrCodePayload` e `qrCodeImagemBase64`.

## Fluxo Atual de Desenvolvimento

1. Frontend chama `POST /api/sessoes/{id}/pagamento`.
2. Backend obtém o valor do produto.
3. `PixServiceStub` cria UUID e payload fictício.
4. Frontend renderiza o payload como QR Code.
5. Frontend consulta `/status` a cada dois segundos.
6. Stub responde `true` e o fluxo avança.

Esse QR Code não representa uma cobrança pagável.

## Fluxo Alvo

1. Criar pedido persistente.
2. Gerar cobrança Pix idempotente com expiração.
3. Exibir QR Code e, se aplicável, Pix copia e cola.
4. Receber webhook assinado.
5. Confirmar valor, txId e vínculo com pedido.
6. Registrar evento financeiro idempotente.
7. Liberar um único job de impressão.
8. Reconciliar cobranças pendentes por consulta.

## Falhas a Tratar

- Provedor indisponível.
- QR Code expirado.
- Webhook atrasado ou duplicado.
- Valor divergente.
- Pagamento após cancelamento visual.
- Duas cobranças para a mesma sessão.
- Confirmação sem impressão.

## Decisões Pendentes

- Provedor Pix e credenciais por ambiente.
- Tempo real de expiração.
- Endpoint público e proteção do webhook.
- Estorno automático ou manual.
- Retenção dos dados de transação.
