# ADR-001 — Domínio e arquitetura / Domain and architecture

**Data / Date:** 2026-09-25  
**Estado / Status:** proposta para revisão / proposed for review

## Português

**Contexto.** O repositório já tem escopo e uma base executável, mas ainda não tem funcionalidades bancárias. É o momento de fixar limites e regras antes de expandir o código.

**Escolhas confirmadas pela autora:** (1) saldo persistido com movimentações imutáveis; (2) PostgreSQL com migrações Flyway; (3) login antes de operações bancárias expostas; (4) faturas por ciclos mensais. A data de fechamento e vencimento proposta em [domain.md](../domain.md) ainda precisa de revisão.

**Proposta arquitetural:** um monólito modular, com contextos Acesso, Contas, Pagamentos e Cartões; domínio separado de adaptadores HTTP e banco; casos de uso coordenam transações locais. Ver [domain.md](../domain.md) e [architecture.md](../architecture.md) para termos, regras e padrões. A separação será introduzida com os casos de uso, sem criar classes vazias para todos os módulos agora.

**Motivos e consequências.** Um banco único permite transferências internas, pagamentos e faturamento com alterações atômicas. Movimentações preservam o extrato enquanto o saldo persistido mantém consultas simples. Flyway registra a evolução do esquema junto ao código. A modelagem mensal do cartão exige regras de calendário e testes de virada de mês. Fronteiras de módulo reduzem acoplamento; verificá-las em testes é uma etapa posterior. Padrões adicionais dependerão de uma necessidade concreta.

**Pendências de revisão:** confirmar ou ajustar fechamento no fim do mês, vencimento no dia 10 do mês seguinte e sessão HTTP como mecanismo inicial de autenticação. Este ADR não declara esses detalhes como implementação concluída.

## English

**Context.** The repository has a product scope and a runnable foundation, but no banking features yet. Boundaries and rules can be reviewed before code grows.

**Author-confirmed choices:** (1) persisted balance plus immutable entries; (2) PostgreSQL and Flyway migrations; (3) login before financial operations are exposed; (4) monthly billing cycles. The proposed closing and due dates in [domain.md](../domain.md) still need review.

**Architectural proposal:** a modular monolith with Access, Accounts, Payments, and Cards contexts; the domain is separated from HTTP and database adapters; use cases coordinate local transactions. [domain.md](../domain.md) and [architecture.md](../architecture.md) hold the vocabulary, rules, and patterns. Module structure grows with implemented use cases rather than empty scaffolding.

**Rationale and consequences.** A single database allows atomic internal transfers and payments. Immutable entries preserve statements while a stored balance makes queries simple. Flyway records schema changes alongside code. Monthly billing requires time rules and month-boundary tests. Module boundaries reduce coupling and can later be verified automatically. Additional patterns need a concrete use case.

**Open for review:** month-end closing, the following month's 10th as due date, and HTTP sessions as the initial authentication mechanism. This ADR does not claim those details are implemented.
