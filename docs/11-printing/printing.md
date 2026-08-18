# Impressão

## Estado Atual

Não existe geração de arquivo final nem comunicação com impressora. `/imprimindo` mostra uma mensagem por seis segundos. A foto permanece apenas no navegador do totem e sua referência local é revogada ao encerrar a tela, portanto o comportamento atual é somente uma simulação visual.

## Produtos e Composição Alvo

### Polaroid

- Uma imagem com recorte quadrado.
- Moldura branca e margem inferior maior.
- Tamanho físico final ainda precisa ser definido.

### Normal 10×15

- Uma imagem na proporção 2:3.
- Definir orientação, política de corte e sangria.
- Saída física de 10×15 cm.

### Oito fotos 3×4

- Oito imagens com proporção 3:4.
- Grade de quatro colunas por duas linhas na prévia atual.
- Cada foto deve medir 3×4 cm no papel, com espaçamento e margens calibrados.

## Regra de Fidelidade

A composição de tela e o arquivo impresso devem compartilhar parâmetros de recorte. CSS isolado não garante que a impressão corresponda à prévia.

## Pipeline Alvo

1. Receber foto e normalizar orientação/cor.
2. Validar resolução mínima para o produto.
3. Aplicar corte aprovado na segunda revisão.
4. Compor canvas na dimensão física e DPI homologados.
5. Gerar arquivo final sem metadados desnecessários.
6. Criar job persistente e idempotente.
7. Enviar ao agente/driver da impressora.
8. Registrar aceitação, conclusão ou falha.
9. Só então limpar a imagem conforme política.

## Requisitos Operacionais

- Impressora e driver homologados.
- Tamanho de papel, DPI, perfil de cor e orientação definidos.
- Fila persistente.
- Detecção de offline, papel, insumo, atolamento e erro de driver quando suportada.
- Reimpressão protegida e auditada.
- Política de reembolso quando não houver entrega.

## Critérios de Aceite

- Medidas físicas dentro da tolerância estabelecida.
- Nenhum corte diferente da prévia aprovada.
- Um pagamento gera no máximo uma impressão automática.
- Reinício não perde job pago.
- Falha mantém dados suficientes para reimpressão ou reembolso, respeitando retenção.
