# Roadmap

## Fase 0 — Protótipo Navegável

Estado: **parcialmente concluída**.

- Frontend Vue e backend Spring Boot locais.
- Catálogo com três produtos e preços.
- Captura ao vivo e fallback móvel.
- Upload por QR Code.
- Duas revisões.
- Contrato Pix e tela de impressão simulados.
- Documentação estruturada.

## Fase 1 — Impressão Real Controlada

- Escolher impressora, papel, driver e sistema operacional.
- Definir dimensões, DPI, sangria e perfil de cor.
- Compartilhar regras de recorte entre prévia e composição.
- Gerar arquivos finais dos três produtos.
- Criar agente e fila persistente de impressão.
- Tratar estados e erros físicos.

## Fase 2 — Pagamento Real e Persistência

- Modelar pedido, pagamento e impressão persistentes.
- Integrar provedor Pix.
- Implementar webhook, consulta e idempotência.
- Tratar expiração, cancelamento, pagamento tardio e estorno.
- Garantir pedido pago após reinício.

## Fase 3 — Hardening do Piloto

- HTTPS, CORS restrito e validação de upload.
- Timeout e recuperação de sessão.
- Limpeza periódica e monitoramento de disco.
- Logs estruturados e health checks.
- Modo kiosk e inicialização automática.
- Testes automatizados e matriz de hardware.

## Fase 4 — Piloto em Campo

- Instalar um totem homologado.
- Medir conversão, refação, falhas, tempo e margem.
- Documentar operação, suporte e reposição de insumos.
- Ajustar UX e preço com dados reais.
- Definir política de reembolso e reimpressão.

## Fase 5 — Administração e Escala

- Painel operacional mínimo.
- Catálogo configurável e versionado.
- Relatórios e conciliação.
- Identidade de dispositivo e ponto.
- Avaliar API central, multi-ponto e atualização remota somente após o piloto.
