# Totem de Fotos

A documentação completa de produto, arquitetura, operação e roadmap está em [`docs/`](docs/README.md).

Protótipo web local para selecionar, capturar, revisar, pagar e imprimir fotos em um totem self-service.

## Backend (Java + Spring Boot)

Requisitos: Java 17+ e Maven.

```
cd totem-fotos-backend
mvn spring-boot:run
```

Sobe em `http://localhost:8080` com o profile `local` ativo. As sessões e a sinalização WebRTC ficam temporariamente em memória; o backend não recebe nem armazena as fotos.

`PixServiceStub` simula o pagamento como aprovado automaticamente. Ele não gera uma cobrança pagável.

## Frontend (Vue + Vite)

Requisitos: Node.js 18+.

```
cd totem-fotos-frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173`. Ao acessar por outro aparelho na rede, o frontend usa automaticamente o mesmo IP na porta 8080 para encontrar o backend. É possível sobrescrever esse endereço com `VITE_API_URL`. Se o totem for aberto por `localhost`, defina `VITE_PUBLIC_URL=http://IP-DO-TOTEM:5173` para o QR Code usar um endereço acessível pelo celular.

A rota `/capturar` usa exclusivamente a câmera conectada ao totem. O navegador do totem deve acessar a aplicação por `localhost` ou HTTPS para liberar `getUserMedia`.

Para acesso pela rede, as portas 5173 e 8080 precisam estar liberadas para a rede local. O Vite é servidor de desenvolvimento e não deve ser usado como servidor de produção.

## Fluxo implementado

1. Boas-vindas e consentimento.
2. Escolha entre Polaroid (R$ 4,50), Normal 10×15 (R$ 5,50) e conjunto com 8 fotos 3×4 (R$ 19,90).
3. Escolha entre tirar foto e enviar do celular.
4. Captura local ou transferência direta do celular para o navegador do totem por WebRTC, iniciada por QR Code.
5. Primeira revisão da foto original.
6. Segunda revisão do formato de impressão.
7. QR Code Pix e consulta periódica do status.
8. Tela simulada de impressão, limpeza e retorno ao início.

## Pendente antes de producao

- Persistir pedido, pagamento e impressão.
- Implementar `PixService` real, idempotência, expiração e conciliação.
- Gerar os arquivos finais dos três formatos com medidas e DPI homologados.
- Integrar impressora física e fila persistente.
- Implementar HTTPS, CORS restrito, orientação EXIF e suporte a HEIC quando necessário.
- Criar timeout, limpeza periódica, logs, health checks e modo kiosk.
- Adicionar testes automatizados e testes físicos de impressão.
