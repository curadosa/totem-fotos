# Diretrizes de UI

## Base Atual

- Área principal lógica de 480×800 pixels.
- Fundo externo `#1c1c1a` e cartão branco com cantos arredondados.
- Fonte `system-ui`.
- Botão primário escuro e secundário cinza-claro.
- Controles grandes, destinados a toque.

## Navegação

- Cada tela deve ter uma ação principal inequívoca.
- A pessoa não deve depender dos botões do navegador.
- A rota deve refletir a etapa, mas não deve ser a única fonte do estado.
- Rotas internas devem redirecionar com segurança quando sessão, produto ou foto estiverem ausentes.

## Catálogo

- Mostrar nome, entrega e preço em cada opção.
- Destacar a opção selecionada com borda e fundo, não apenas cor.
- Desabilitar avanço até haver seleção.

## Captura

- Mostrar estado “Abrindo a câmera”.
- Mostrar preview, moldura e contagem quando disponíveis.
- Mostrar fallback nativo com explicação curta quando a câmera ao vivo não funcionar.
- Mostrar falha de upload e permitir nova tentativa.

## Revisões

- Primeira revisão responde “a foto ficou boa?”.
- Segunda revisão responde “o corte e a disposição estão corretos?”.
- Ambas devem oferecer Refazer e Confirmar.
- A segunda prévia deve usar a mesma regra de recorte do gerador de impressão futuro.

## Pagamento

- Mostrar produto e valor vindos ou validados pelo backend.
- Mostrar expiração real, não apenas contagem visual.
- Distinguir aguardando, confirmado, expirado, cancelado e erro.

## Acessibilidade

- Manter contraste WCAG AA onde aplicável.
- Usar `alt` nas prévias relevantes.
- Preservar foco visível e semântica de botão.
- Não comunicar estado somente por cor.
- Testar zoom, fontes maiores e leitores de tela no fluxo móvel.

## Responsividade Pendente

O tamanho fixo atual pode ultrapassar telas móveis menores. Antes do piloto, usar limites com `min()`, unidades de viewport e suporte a `100dvh`, preservando 480×800 como referência do totem.
