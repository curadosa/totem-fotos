# Jornadas

## Jornada Principal

1. Usuário lê o aviso de privacidade e inicia.
2. Escolhe um produto e vê o preço.
3. Escolhe tirar foto ou enviar do celular.
4. Produz ou envia a imagem.
5. Confirma a qualidade em `/revisar`.
6. Confirma corte e disposição em `/revisar-impressao`.
7. Lê o QR Code e paga.
8. Aguarda confirmação.
9. Aguarda impressão e retira o produto.
10. Sistema limpa a sessão e volta à abertura.

## Jornada de Captura no Totem

- O navegador solicita permissão para câmera.
- Com permissão e contexto seguro, mostra preview, moldura e contagem regressiva.
- Sem `getUserMedia`, informa que a câmera do totem está indisponível e permite tentar novamente.
- A imagem permanece como `Blob` local e é exibida na primeira revisão.

## Jornada de Transferência por QR Code

1. Totem cria oferta WebRTC e solicita token temporário de sinalização.
2. Exibe URL em QR Code apontando para o IP local e porta 5173.
3. Celular abre `/upload-celular` e escolhe uma imagem.
4. Backend valida sessão, token e expiração e troca apenas oferta/resposta SDP.
5. Celular envia a imagem pelo canal de dados WebRTC diretamente ao navegador do totem.
6. Totem confirma o recebimento, guarda um `Blob` local e abre a revisão.

## Jornada de Refação

- Em qualquer revisão, “Refazer” revoga a URL `blob:` e remove a referência local.
- O frontend abre `/capturar` sem enviar a imagem ao backend.

## Jornada de Pagamento Atual

1. Frontend solicita cobrança para a sessão.
2. Backend usa o preço de `ProdutoFoto` e o `PixService` ativo.
3. QR Code fica visível com cronômetro de cinco minutos.
4. Frontend consulta status a cada dois segundos.
5. No profile local, o stub responde pago imediatamente.
6. O frontend finaliza a sessão e exibe a tela simulada por seis segundos.

## Pontos Críticos

- HTTP por IP bloqueia câmera ao vivo em muitos celulares.
- Portas 5173 e 8080 precisam estar acessíveis na rede.
- Atualizar a página perde o estado reativo do frontend.
- Cronômetro visual ainda não expira a cobrança nem encerra o polling.
- Uma falha no polling não possui tratamento específico.
- O protótipo ainda não possui agente de impressão capaz de consumir a foto mantida no navegador.
