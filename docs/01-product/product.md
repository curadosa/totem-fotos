# Produto

## Resumo

Totem Fotos é uma aplicação web para um ponto físico de impressão fotográfica self-service. A pessoa escolhe um produto, tira uma foto ou envia uma imagem do celular, faz duas confirmações, paga por Pix e retira a impressão.

## Produtos Atuais

| Código | Produto | Entrega | Preço |
|---|---|---:|---:|
| `POLAROID` | Polaroid | 1 foto com moldura | R$ 4,50 |
| `NORMAL_10X15` | Normal 10×15 | 1 foto | R$ 5,50 |
| `SEIS_FOTOS_3X4` | Fotos 3×4 | Conjunto com 6 fotos | R$ 19,90 |

Os preços são definidos no enum `ProdutoFoto` do backend. O frontend mantém os mesmos dados para apresentação, mas não determina o valor cobrado.

## Fluxo Implementado

1. Consentimento e início.
2. Escolha do produto e visualização do preço.
3. Escolha entre tirar foto ou enviar do celular.
4. Captura com câmera ao vivo, câmera nativa como fallback ou upload por QR Code.
5. Primeira revisão: qualidade da foto original.
6. Segunda revisão: corte e disposição no formato de impressão.
7. Geração e exibição da cobrança Pix.
8. Consulta periódica do status do pagamento.
9. Tela simulada de impressão e retorno ao início.

## Formatos de Prévia

- Polaroid: recorte quadrado dentro de moldura branca com base maior.
- Normal 10×15: recorte na proporção 2:3.
- 6 fotos 3×4: seis recortes 3:4, em grade de três colunas por duas linhas.

## Implementado

- Interface Vue otimizada para uma tela lógica de 480×800.
- API local Spring Boot.
- Sessões em memória.
- Foto temporária em disco por data e sessão.
- Token de upload por celular com validade de cinco minutos.
- Pix por adaptador, com implementação simulada no profile `local`.

## Ainda Não Implementado

- Cobrança Pix real e reconciliação.
- Geração de arquivos finais com resolução e DPI de impressão.
- Driver, spool ou fila de impressora.
- Persistência de pedidos, pagamentos e auditoria.
- Painel administrativo e monitoramento.
- Timeout completo de sessão e limpeza periódica.
