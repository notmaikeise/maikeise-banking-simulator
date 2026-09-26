# Visão e escopo / Product vision and scope

## Português

### Objetivo e jornada

Projeto individual de estudo e portfólio: construir localmente, em Java e Spring Boot, um simulador de banco fictício para praticar modelagem de domínio, persistência, segurança e testes. Uma pessoa fictícia se cadastra, entra na sua conta em BRL, adiciona saldo de demonstração, transfere para outra pessoa do app, paga uma cobrança de teste, compra com cartão virtual e quita a fatura.

### Decisões do produto

| Tema | Decisão |
| --- | --- |
| Acesso | Login e autorização por sessão HTTP na aplicação local. |
| Conta | Uma conta em BRL por usuário, com saldo persistido e movimentações imutáveis. |
| Saldo de teste | Crédito manual de demonstração, registrado no extrato. |
| Pix | Apenas entre contas cadastradas no app. |
| Boleto | Cobrança fictícia criada no app; identificador sem linha digitável bancária válida. |
| Cartão | Compras à vista usam limite e entram em faturas mensais. |
| Fatura | Fecha no último dia do mês, vence no dia 10 seguinte e inicialmente só pode ser paga integralmente. |
| Persistência | PostgreSQL com evolução do esquema por migrações Flyway. |

### Regras e limites

- Operações usam valores positivos em BRL com até duas casas decimais. Conta não fica com saldo negativo.
- Pix interno debita origem e credita destino pelo mesmo valor na mesma transação. Repetir a solicitação não transfere novamente.
- Uma cobrança paga não pode ser paga de novo.
- Compra no cartão ocupa limite e entra na fatura; não debita a conta naquele momento. Pagar fatura integralmente debita a conta e libera o limite correspondente.
- Usuários acessam apenas os próprios recursos. Os detalhes de contextos, agregados e transações estão no [modelo de domínio](domain.md); arquitetura e padrões estão em [architecture.md](architecture.md).

**Limites da simulação:** sem dinheiro real, Pix externo, boleto bancário válido, cartão utilizável, compra parcelada ou integração com rede bancária.

**Estado atual:** escopo definido e base executável com Java 21, Spring Boot 4.1.1 e Maven; o teste inicial passou. Os documentos de domínio, arquitetura e padrões estão em revisão. Nenhum fluxo bancário foi implementado. Próxima etapa de código: cadastro, conta e extrato de demonstração, com login antes de expor operações.

## English

### Goal and journey

A solo study and portfolio project: build a locally runnable fictional bank simulator in Java and Spring Boot to practice domain modeling, persistence, security, and testing. A fictional user registers, logs into a BRL account, adds demo funds, transfers to another app user, pays a test bill, makes a virtual card purchase, and pays the invoice.

### Product decisions

| Topic | Decision |
| --- | --- |
| Access | Login and authorization with HTTP sessions in the local application. |
| Account | One BRL account per user, with a persisted balance and immutable entries. |
| Demo funds | Manual demo credit, recorded in the statement. |
| Pix | Only between accounts registered in the app. |
| Bill | Fictional bill created in the app; identifier without a valid bank payment line. |
| Card | Single-payment purchases use limit and enter monthly invoices. |
| Invoice | Closes on the last day of the month, is due on the following month's 10th, and initially accepts full payment only. |
| Persistence | PostgreSQL with schema evolution through Flyway migrations. |

### Rules and boundaries

- Operations use positive BRL amounts with at most two decimal places. Account balance cannot be negative.
- Internal Pix debits the source and credits the destination by the same amount in one transaction. Retrying the request cannot transfer again.
- A paid bill cannot be paid again.
- A card purchase uses limit and enters an invoice; it does not debit the account at purchase time. Full invoice payment debits the account and releases the matching limit.
- Users access only their own resources. Contexts, aggregates, and transactions are in the [domain model](domain.md); architecture and patterns are in [architecture.md](architecture.md).

**Simulation boundaries:** no real money, external Pix, valid bank bill, usable card, installment purchase, or banking network integration.

**Current state:** scope defined and a runnable Java 21, Spring Boot 4.1.1, and Maven foundation; the initial test passed. Domain, architecture, and pattern documents are under review. No banking flow has been implemented. The next coding stage covers registration, account, and demo statement, with login before operations are exposed.
