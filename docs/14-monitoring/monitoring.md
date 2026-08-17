# Monitoramento

## Estado Atual

Não há observabilidade estruturada. O backend usa logs padrão do Spring; a falha ao remover foto é ignorada e o frontend usa mensagens locais. Não existem métricas, health checks, alertas ou correlação persistente.

## Sinais Mínimos do Piloto

- Backend iniciado e saudável.
- Espaço livre em disco.
- Sessões abertas por estado e idade.
- Falhas de captura e upload.
- Cobranças criadas, confirmadas e expiradas.
- Jobs de impressão por estado.
- Impressora online, papel e erro quando o driver permitir.
- Tempo do fluxo e de cada etapa.

## Logs Estruturados

Cada evento deve incluir quando aplicável:

- Timestamp e nível.
- Versão e identificador do dispositivo.
- `sessaoId`, `pedidoId`, `txId` e `impressaoId`.
- Etapa, resultado, duração e código de erro.

Não registrar conteúdo da foto, token de upload, payload Pix, credencial ou dado bancário.

## Alertas Recomendados

- API indisponível.
- Disco acima do limite.
- Fotos além da retenção.
- Alta taxa de falha de câmera ou upload.
- Pagamento confirmado sem job de impressão.
- Job parado ou impressora offline.
- Reimpressões acima do esperado.

## Métricas de Produto

- Sessões iniciadas e concluídas.
- Conversão por produto e origem da foto.
- Refação na primeira e segunda revisão.
- Abandono por etapa.
- Receita e margem por produto.
- Tempo até retirada.

## Operação Local

Mesmo sem plataforma central, o piloto precisa de log rotativo, página de saúde protegida e procedimento para exportar diagnóstico sem copiar fotos.
