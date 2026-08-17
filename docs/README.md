# Documentação Totem Fotos

Este diretório concentra a visão de produto, o comportamento implementado, as decisões técnicas e o trabalho necessário para transformar o protótipo em um totem operacional.

## Estado do Projeto

O projeto é um protótipo web local composto por frontend Vue/Vite e backend Spring Boot. Já existem seleção de produto, captura ou upload, duas revisões, Pix simulado e uma tela de impressão simulada. Integração com adquirente, geração do arquivo final, impressora física, painel administrativo, persistência e observabilidade ainda não estão implementados.

Use os rótulos abaixo nos documentos:

- **Implementado:** comportamento presente no código atual.
- **Parcial:** existe uma implementação, mas faltam requisitos para produção.
- **Planejado:** direção aprovada ou recomendada, ainda sem implementação.
- **Fora do MVP:** não deve bloquear o primeiro piloto.

## Mapa

- [`00-vision/`](00-vision/vision.md): visão, missão e princípios.
- [`01-product/`](01-product/product.md): produto, proposta de valor, MVP e modelo de negócio.
- [`02-requirements/`](02-requirements/requirements.md): requisitos e critérios de aceite.
- [`03-ux/`](03-ux/journeys.md): personas e jornadas.
- [`04-ui/`](04-ui/ui-guidelines.md): diretrizes da interface web do totem.
- [`05-brand/`](05-brand/brand.md): marca e tom de voz.
- [`06-architecture/`](06-architecture/architecture-overview.md): arquitetura atual e evolução.
- [`07-domain/`](07-domain/domain-model.md): produtos, sessão e estados.
- [`08-android/`](08-android/android.md): execução em Android e dispositivos móveis.
- [`09-camera/`](09-camera/camera.md): câmera do totem e transferência direta por QR Code.
- [`10-payments/`](10-payments/payments.md): pagamentos e Pix.
- [`11-printing/`](11-printing/printing.md): preparação e impressão dos formatos.
- [`12-kiosk/`](12-kiosk/kiosk.md): operação em modo totem.
- [`13-admin/`](13-admin/admin.md): painel administrativo futuro.
- [`14-monitoring/`](14-monitoring/monitoring.md): logs, métricas e alertas.
- [`15-security/`](15-security/security.md): privacidade, segurança e riscos atuais.
- [`16-testing/`](16-testing/testing-strategy.md): estratégia e matriz de testes.
- [`17-roadmap/`](17-roadmap/roadmap.md): fases e marcos.
- [`adr/`](adr/0001-documentation-structure.md): decisões arquiteturais.
- [`decisions/`](decisions/0001-product-hypotheses.md): decisões de produto e operação.

## Regra de Manutenção

Uma alteração que afete fluxo, preço, formato, estado, contrato de API, retenção de foto ou operação deve atualizar o documento temático correspondente. Decisões estruturais devem ser registradas em `adr/`; decisões de produto ou negócio, em `decisions/`.

Última revisão geral: 2026-08-16.
