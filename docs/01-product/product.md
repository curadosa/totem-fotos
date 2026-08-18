# Produto

## Resumo

Totem Fotos é uma aplicação web para um ponto físico de impressão fotográfica self-service. A pessoa escolhe um produto, tira uma foto ou envia uma imagem do celular, faz duas confirmações, paga por Pix e retira a impressão.

## Produtos Atuais

| Código | Produto | Entrega | Preço |
|---|---|---:|---:|
| `POLAROID` | Polaroid | 1 foto com moldura | R$ 4,50 |
| `NORMAL_10X15` | Normal 10×15 | 1 foto | R$ 5,50 |
| `OITO_FOTOS_3X4` | Fotos 3×4 | Conjunto com 8 fotos | R$ 19,90 |

Os preços são definidos no enum `ProdutoFoto` do backend. O frontend mantém os mesmos dados para apresentação, mas não determina o valor cobrado.

## Fluxo Implementado

1. Consentimento e início.
2. Escolha do produto e visualização do preço.
3. Escolha entre tirar foto ou enviar do celular.
4. Captura local ou transferência direta do celular por WebRTC, iniciada por QR Code.
5. Primeira revisão: qualidade da foto original.
6. Segunda revisão: corte e disposição no formato de impressão.
7. Geração e exibição da cobrança Pix.
8. Consulta periódica do status do pagamento.
9. Tela simulada de impressão e retorno ao início.

## Formatos de Prévia

- Polaroid: recorte quadrado dentro de moldura branca com base maior.
- Normal 10×15: recorte na proporção 2:3.
- 8 fotos 3×4: oito recortes 3:4, em grade de quatro colunas por duas linhas.

## Implementado

- Interface Vue otimizada para uma tela lógica de 480×800.
- API local Spring Boot.
- Sessões em memória.
- Foto mantida somente na memória do navegador do totem durante a sessão.
- Conexão direta com o celular por WebRTC e token de sinalização válido por cinco minutos.
- Pix por adaptador, com implementação simulada no profile `local`.

## Ainda Não Implementado

- Cobrança Pix real e reconciliação.
- Geração de arquivos finais com resolução e DPI de impressão.
- Driver, spool ou fila de impressora.
- Persistência de pedidos, pagamentos e auditoria.
- Painel administrativo e monitoramento.
- Timeout completo de sessão e limpeza periódica.
