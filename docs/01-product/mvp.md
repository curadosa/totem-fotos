# MVP

## Objetivo

Validar, em um único ponto físico, se pessoas concluem o fluxo de escolha, captura, dupla revisão, pagamento e retirada sem assistência constante.

## Escopo Obrigatório do Piloto

- Três produtos com preços controlados pelo backend.
- Captura pela câmera do totem.
- Recuperação com nova tentativa quando a câmera do totem estiver indisponível.
- Upload do celular por QR Code e token temporário.
- Revisão da foto original.
- Revisão do formato final.
- Pix real com expiração e confirmação confiável.
- Arquivo de impressão gerado conforme o produto.
- Impressão física com status de sucesso ou falha.
- Limpeza garantida da foto.
- Logs mínimos por sessão, sem dados sensíveis.

## Estado Atual

| Capacidade | Estado |
|---|---|
| Escolha de produto e preço | Implementado |
| Captura e upload | Implementado, com limitações de HTTP móvel |
| Duas revisões | Implementado como prévia CSS |
| Pix | Simulado |
| Arquivo final de impressão | Não implementado |
| Impressora física | Não implementado |
| Persistência e auditoria | Não implementado |
| Testes automatizados | Não implementado |

## Fora do MVP

- Editor avançado, filtros e remoção de fundo.
- Aplicativo consumidor.
- Cupons, fidelidade e redes sociais.
- Multi-tenant e múltiplos pontos.
- Inteligência artificial para moderação.
- Painel analítico avançado.

## Critérios de Sucesso

- Pelo menos 95% das sessões de teste controlado chegam à retirada sem erro técnico.
- Nenhuma sessão imprime antes da confirmação real do pagamento.
- O recorte impresso corresponde à segunda prévia dentro da tolerância definida.
- Fotos são removidas após conclusão, cancelamento e expiração.
- Uma falha de impressora não perde o registro de um pedido pago.
- Um operador identifica a causa de uma falha em até cinco minutos.
