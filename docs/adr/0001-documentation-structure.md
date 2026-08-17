# ADR 0001: Estrutura de Documentação

## Status

Aceita em 2026-08-16.

## Contexto

Totem Fotos precisa manter, no mesmo repositório, conhecimento de produto, experiência, arquitetura, câmera, pagamentos, impressão, operação e evolução. O projeto `autofoto` possui uma taxonomia útil, mas representa um produto semelhante e maior, não a implementação deste repositório.

## Decisão

Adotar em `docs/` a mesma estrutura temática de diretórios do projeto de referência, reescrevendo todo o conteúdo para Totem Fotos.

Cada documento deve:

- Distinguir claramente implementado, parcial e planejado.
- Usar o código deste repositório como fonte para o estado atual.
- Não declarar Android nativo, admin, pagamento real ou impressão física como prontos.
- Manter links relativos e decisões rastreáveis.

Decisões arquiteturais ficam em `docs/adr/`; decisões de produto e negócio, em `docs/decisions/`.

## Consequências

- Áreas futuras possuem local definido antes de existir código.
- A documentação pode ser comparada com projetos similares sem misturar escopos.
- Há custo de atualizar documentos junto com preços, rotas, estados e integrações.
- Diretórios como `08-android` e `13-admin` permanecem, ainda que descrevam ausência e evolução futura.
