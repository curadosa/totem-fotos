# Administração

## Estado Atual

Não há painel administrativo, autenticação, usuários, relatórios ou configuração remota. Produtos e preços estão no código.

## Objetivo Futuro

Dar ao operador visibilidade e ações seguras sem expor a interface do consumidor ou exigir acesso direto à máquina.

## MVP Administrativo Recomendado

- Estado do backend, câmera, armazenamento e impressora.
- Catálogo e preços versionados.
- Pedidos por período e produto.
- Pagamentos pendentes, confirmados e divergentes.
- Impressões pendentes, concluídas e com falha.
- Reimpressão controlada com motivo.
- Limpeza e capacidade do armazenamento temporário.
- Versão do software e horário do último sinal.

## Perfis Futuros

- Operador local: diagnóstico e ações operacionais limitadas.
- Gestor: catálogo, preços e relatórios.
- Financeiro: pagamentos, conciliação e estornos.
- Suporte: diagnóstico e histórico técnico.

## Perguntas que o Painel Deve Responder

- O totem está pronto para vender?
- Quantos pedidos de cada produto foram concluídos hoje?
- Qual pagamento foi confirmado e não impresso?
- Há foto temporária além do prazo?
- A impressora está sem papel, offline ou com erro?
- Quem autorizou uma reimpressão ou estorno?

## Segurança

- Administração não deve compartilhar sessão com a interface pública.
- Ações financeiras e reimpressões exigem auditoria.
- Fotos não devem aparecer por padrão em listagens.
- Acesso remoto deve usar autenticação forte e transporte seguro.
