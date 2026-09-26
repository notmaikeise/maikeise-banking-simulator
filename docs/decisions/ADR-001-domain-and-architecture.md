# ADR-001 — Domínio e arquitetura / Domain and architecture

**Data / Date:** 2026-09-25  
**Estado / Status:** aceito; primeira etapa implementada em Acesso e Contas / accepted; first stage implemented in Access and Accounts

## Português

### Contexto

Na data desta decisão, o repositório tinha escopo e uma base Spring Boot executável, sem funcionalidades bancárias. Precisávamos registrar limites do domínio, consistência e padrões antes de ampliar o código.

### Decisões de produto confirmadas

1. Saldo persistido com movimentações imutáveis no extrato.
2. PostgreSQL com mudanças de esquema versionadas por Flyway.
3. Login e autorização antes de expor operações bancárias; aplicação local com sessão HTTP.
4. Fatura mensal: fecha no último dia do mês, vence no dia 10 do mês seguinte e inicialmente só aceita pagamento integral.

### Arquitetura e padrões selecionados para implementação

Um monólito modular com **Acesso, Contas, Pagamentos e Cartões**, organizados internamente em domínio, aplicação e adaptadores. Usaremos **Aggregate, Value Object, Application Service, Repository, Adapter e métodos estáticos de criação com nomes de domínio**. As regras exatas estão em [domain.md](../domain.md), e o propósito e a localização de cada padrão em [architecture.md](../architecture.md). Introduziremos as classes com os casos de uso, sem estrutura vazia antecipada.

### Motivos e consequências

Um único banco permite transações locais atômicas entre contas, cobrança e fatura. Movimentações preservam o extrato enquanto o saldo persistido facilita a consulta. Flyway registra a evolução do esquema junto ao código. O ciclo mensal exige relógio controlável e testes na virada do mês. A sessão HTTP exige validação de titularidade e proteção CSRF. Limites de módulo impedem acesso direto às entidades JPA de outro contexto.

Strategy, classes State, Observer para mudanças financeiras, microsserviços, filas, saga, CQRS e event sourcing não entram no primeiro recorte porque as regras atuais não exigem suas variações ou distribuição. Spring Modulith pode ser adotado depois para verificar os limites, sem alterar a escolha pelo monólito modular.

**Implementação atual:** Acesso e Contas aplicam essas decisões com PostgreSQL, Flyway e segurança por sessão. Pagamentos, Cartões e suas regras continuam previstos em [architecture.md](../architecture.md). Testes H2 e PostgreSQL aprovados no JDK 21 com Docker em 26/09/2026.

## English

### Context

When this decision was made, the repository had a product scope and a runnable Spring Boot foundation, without banking features. Domain boundaries, consistency, and patterns needed to be recorded before the code grew.

### Confirmed product decisions

1. Persisted balance plus immutable statement entries.
2. PostgreSQL with schema changes versioned by Flyway.
3. Login and authorization before exposing financial operations; a local application with HTTP sessions.
4. Monthly invoices: close on the last day of the month, are due on the 10th of the following month, and initially accept full payment only.

### Architecture and patterns selected for implementation

A modular monolith with **Access, Accounts, Payments, and Cards**, each organized internally into domain, application, and adapters. We will use **Aggregate, Value Object, Application Service, Repository, Adapter, and explicitly named static creation methods**. Exact rules are in [domain.md](../domain.md); each pattern's purpose and location are in [architecture.md](../architecture.md). Classes will be added with real use cases rather than empty scaffolding.

### Rationale and consequences

One database supports atomic local transactions across accounts, bills, and invoices. Entries preserve statements while a stored balance makes queries simple. Flyway records schema changes with the code. Monthly billing needs a controllable clock and month-boundary tests. HTTP sessions require ownership checks and CSRF protection. Module boundaries prevent direct access to another context's JPA entities.

Strategy, State classes, Observer for financial changes, microservices, queues, saga, CQRS, and event sourcing are outside the initial scope because current rules require neither their variants nor distribution. Spring Modulith can be added later to verify the boundaries without changing the modular monolith decision.

**Current implementation:** Access and Accounts apply these decisions with PostgreSQL, Flyway, and session security. Payments, Cards, and their rules remain planned in [architecture.md](../architecture.md). H2 and PostgreSQL tests passed on JDK 21 with Docker on 26 September 2026.
