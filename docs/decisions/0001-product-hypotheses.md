# Decisão 0001: Hipóteses Iniciais de Produto

## Status

Proposta, registrada em 2026-08-16.

## Contexto

O protótipo já implementa uma jornada local para três tipos de impressão, mas ainda não passou por piloto com pagamento e impressora reais.

## Decisão

Validar primeiro um único totem web e local, com fluxo sem cadastro e três produtos fixos:

- Polaroid por R$ 4,50.
- Normal 10×15 por R$ 5,50.
- Conjunto com 8 fotos 3×4 por R$ 19,90.

O usuário pode capturar ou enviar a imagem e deve fazer duas confirmações antes de pagar: qualidade da foto e formato de impressão.

## Hipóteses

- Três opções cobrem usos de lembrança e documento sem tornar a escolha complexa.
- A segunda revisão reduz desperdício e contestação de corte.
- O upload sem cadastro aumenta conclusão quando a câmera do totem não é desejada.
- Pix é suficiente para o primeiro piloto.
- Arquitetura local reduz dependência da internet, mas não elimina a necessidade de persistência.

## Como Validar

- Integrar impressão e Pix reais.
- Medir escolha por produto e conversão.
- Medir refação em cada revisão.
- Medir uso de câmera, fallback e QR Code.
- Calcular margem real por produto.
- Entrevistar usuários e operador após o piloto.

## Critério de Revisão

Revisar preços, catálogo e arquitetura após dados suficientes do primeiro piloto, ou antes se custo de insumos, taxa Pix ou limitações da impressora inviabilizarem algum produto.
