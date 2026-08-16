# Totem de fotos - como rodar localmente

## Backend (Java + Spring Boot)

Requisitos: Java 17+ e Maven.

```
cd totem-fotos-backend
mvn spring-boot:run
```

Sobe em http://localhost:8080 com o profile "local" ativo (application-local.yml).
As sessoes ficam em memoria e as fotos sao salvas em ./data/sessoes/{data}/{sessaoId}/foto.jpg.

O PixServiceStub simula o pagamento como aprovado automaticamente - troque por
uma implementacao real da sua adquirente (Stone, PagBank, EFI...) quando for
integrar de verdade, implementando a interface PixService.

## Frontend (Vue + Vite)

Requisitos: Node.js 18+.

```
cd totem-fotos-frontend
npm install
npm run dev
```

Sobe em http://localhost:5173. Ao acessar por outro aparelho na rede, o frontend
usa automaticamente o mesmo IP na porta 8080 para encontrar o backend
(configuravel via variavel de ambiente VITE_API_URL).

Em navegadores moveis, a camera ao vivo (`getUserMedia`) exige HTTPS quando o
acesso nao e por localhost. Nesse caso, a tela oferece automaticamente o botao
"Abrir camera do celular", que usa a captura nativa do aparelho mesmo na rede local.

## Fluxo implementado

1. Boas-vindas / consentimento -> "Iniciar sessao"
2. Home -> "Tirar foto agora" ou "Enviar do celular"
3a. Tirar foto: camera ao vivo + moldura guia + contagem regressiva de 3s
3b. Enviar do celular: QR code -> pagina de upload no navegador do celular (token de 5 min)
4. Revisar e confirmar (com "Refazer", sem custo)
5. Pagamento via Pix (QR code + polling de status + timeout/erro tratados)
6. Impressao e limpeza automatica da foto ao finalizar a sessao

## Pendente antes de producao

- Implementar PixService real (integracao com a adquirente escolhida)
- Integrar impressora fisica (hoje a tela de impressao e so uma simulacao)
- Rodar o backend como Windows Service (NSSM) para auto-recuperacao
- Configurar o totem como hotspot Wi-Fi local para o QR code do celular funcionar sem roteador externo
- Trocar o profile para "cloud" quando migrar para a VPS
