# Estratégia de Testes

## Estado Atual

O frontend possui apenas scripts de desenvolvimento e build. O backend compila, mas não possui fontes de teste. A validação atual se limita a build manual e navegação exploratória.

## Prioridades

1. O preço cobrado corresponde ao produto.
2. A segunda prévia corresponde ao arquivo impresso.
3. Pagamento confirmado gera no máximo uma impressão.
4. Falha de impressão preserva o pedido e permite recuperação controlada.
5. Fotos são removidas em todos os encerramentos.
6. Captura e upload funcionam nos dispositivos homologados.

## Testes Unitários

### Backend

- Valor e descrição de cada `ProdutoFoto`.
- Criação e transições válidas da sessão.
- Expiração e uso único do token.
- Idempotência futura de cobrança e impressão.
- Política de retenção.

### Frontend

- Seleção e formatação de preço.
- Renderização dos três layouts.
- Falha, permissão e nova tentativa da câmera conectada ao totem.
- Limpeza de timers e URLs `blob:`.
- Redirecionamento quando falta estado.

## Testes de Integração

- API de sessão com produto válido, ausente e inválido.
- Upload multipart e leitura da imagem.
- Token válido, inválido, expirado e reutilizado.
- Refazer e finalizar removem arquivo.
- Adaptador Pix com sucesso, timeout, erro e confirmação repetida.
- Agente de impressão com sucesso e falhas simuladas.

## Testes E2E

- Cada produto por captura local.
- Cada produto por upload via QR Code.
- Refazer na primeira revisão.
- Refazer na segunda revisão.
- Cancelar pagamento.
- Expirar cobrança.
- Reiniciar frontend, backend e agente em etapas críticas.

## Matriz de Dispositivos

- Totem/hardware homologado.
- Chrome e Edge na versão implantada.
- Android Chrome e iOS Safari para upload.
- HTTP local e HTTPS alvo.
- Permissão de câmera concedida, negada e indisponível.
- Rede lenta, intermitente e sem internet.

## Testes Físicos de Impressão

- Régua para dimensões 10×15 e 3×4.
- Carta de cor e resolução.
- Corte nas bordas e sangria.
- Oito cópias na disposição esperada.
- Falta de papel, tampa aberta, offline e atolamento.

## Evidência de Release

- Commit/versão e ambiente.
- Hardware, navegador, impressora e driver.
- Cenários executados e resultados.
- Exemplos físicos aprovados por produto.
- Falhas conhecidas e decisão de implantação.
