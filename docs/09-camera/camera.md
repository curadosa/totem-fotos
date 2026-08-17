# Câmera

## Objetivo

Obter uma imagem pela câmera conectada ao totem e mantê-la somente na memória do navegador durante a sessão.

## Modos Implementados

### Câmera ao vivo

- Usa `navigator.mediaDevices.getUserMedia`.
- Solicita a webcam selecionada, sem áudio.
- Detecta dispositivos `videoinput`, usa a webcam padrão e permite escolher outra quando houver mais de uma.
- Solicita preferencialmente 1280×720 a 30 fps, com fallback para as capacidades da webcam.
- Mostra o vídeo com `object-fit: cover` e moldura pontilhada.
- Faz contagem de três segundos.
- Copia o frame para canvas e gera JPEG com qualidade 0,92.

### Indisponibilidade da câmera

- `/capturar` não oferece seletor de arquivo nem abre a câmera de um celular.
- Quando `getUserMedia` falha, orienta a verificar conexão e permissão e permite tentar novamente.
- Diferencia permissão negada, webcam ausente, dispositivo ocupado e acesso fora de contexto seguro.
- A captura gera JPEG local com qualidade 0,92 e não chama o backend.

### Transferência direta por QR Code

- O navegador do totem cria um canal de dados WebRTC e o backend emite um token UUID válido por cinco minutos.
- O QR Code aponta para `/upload-celular` no endereço local do frontend.
- O backend troca somente oferta e resposta SDP, mantidas em memória durante a negociação.
- A foto JPEG ou PNG, limitada a 10 MB, segue diretamente do celular para o navegador do totem em blocos de 16 KiB.
- A página móvel mostra prévia e progresso, e aguarda a confirmação do totem.
- O token e a sinalização são invalidados após o recebimento.
- Nenhum byte da foto é enviado ao backend ou gravado em disco.

## Requisitos de Rede

- Celular e totem devem alcançar o mesmo endereço.
- Frontend deve estar acessível na porta 5173.
- Backend deve estar acessível na porta 8080 para a sinalização WebRTC e escuta em `0.0.0.0`.
- Firewall do sistema operacional deve liberar somente a rede necessária.
- Produção deve preferir HTTPS e configuração explícita de origem.

## Qualidade e Formato

A prévia usa `object-fit: cover`, portanto corta a imagem conforme a área. Ainda não há:

- Validação de resolução mínima.
- Correção de orientação EXIF.
- Conversão confiável de HEIC/PNG para JPEG.
- Detecção de foco, exposição ou rosto.
- Regra única compartilhada entre prévia e arquivo impresso.

## Validação de Upload Pendente

- Normalização de orientação EXIF e espaço de cor.
- Suporte explícito a HEIC quando exigido pelos dispositivos homologados.
- Validação de resolução mínima específica de cada produto.

## Métricas Futuras

- Tempo até preview.
- Falha por tipo: permissão, contexto inseguro, dispositivo ou rede.
- Uso de câmera ao vivo versus fallback.
- Taxa de upload por QR Code.
- Refação em cada revisão.
