# Princípios

## Produto

- Resolver a jornada presencial completa: escolha, foto, revisões, pagamento e retirada.
- Não cobrar antes de a pessoa confirmar o resultado final.
- Manter os três produtos compreensíveis sem ajuda de um operador.
- Evitar cadastro, senha ou instalação de aplicativo para o consumidor.

## Experiência

- Uma ação primária clara por etapa.
- Preço, progresso e próximo passo sempre visíveis.
- Mensagens de erro devem informar uma ação possível.
- A rota de captura usa somente a câmera conectada ao totem; o celular participa apenas do fluxo separado por QR Code.

## Tecnologia

- O backend é a fonte de verdade dos preços.
- Integrações externas ficam atrás de contratos, como `PixService` e um futuro `PrintService`.
- Pagamento e impressão devem se tornar idempotentes antes de produção.
- Estado em memória e arquivos locais são aceitáveis apenas para o protótipo de um totem.

## Operação

- O fluxo deve voltar ao início após conclusão, cancelamento ou timeout.
- Fotos órfãs precisam de limpeza periódica, além da exclusão no fluxo feliz.
- Falhas não podem desaparecer em blocos vazios de tratamento de exceção.
- A impressora física e seus insumos fazem parte do produto.

## Segurança e Privacidade

- Não armazenar fotos além do necessário para o pedido.
- Não expor fotos por identificadores previsíveis ou listagens públicas.
- Usar HTTPS, CORS restrito e validação de arquivo antes de produção.
- Não registrar imagem, token de upload, payload Pix ou segredo em logs.
