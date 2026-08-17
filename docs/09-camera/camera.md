# Câmera

## Objetivo

Obter uma imagem adequada ao produto escolhido, com recuperação quando a câmera ao vivo não puder abrir.

## Modos Implementados

### Câmera ao vivo

- Usa `navigator.mediaDevices.getUserMedia`.
- Solicita vídeo frontal (`facingMode: user`) e nenhum áudio.
- Mostra o vídeo com `object-fit: cover` e moldura pontilhada.
- Faz contagem de três segundos.
- Copia o frame para canvas e gera JPEG com qualidade 0,92.

### Captura nativa

- Fallback por `input type="file"` com `capture="user"`.
- Adequado ao acesso móvel por HTTP, no qual `getUserMedia` pode ser bloqueado.
- O comportamento exato entre abrir câmera e oferecer galeria depende do navegador.

### Upload por QR Code

- Backend emite token UUID válido por cinco minutos.
- QR Code aponta para `/upload-celular` no IP local do frontend.
- Totem consulta a sessão a cada dois segundos.
- Ao receber a foto, baixa um blob para exibir nas revisões.
- A página móvel mostra a prévia e o progresso antes de confirmar o envio.
- O token é invalidado após o primeiro upload concluído.
- O backend aceita JPEG e PNG de até 10 MB e valida formato, dimensões e limite de 40 megapixels.

## Requisitos de Rede

- Celular e totem devem alcançar o mesmo endereço.
- Frontend deve estar acessível na porta 5173.
- Backend deve estar acessível na porta 8080 e escuta em `0.0.0.0`.
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
