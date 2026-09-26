# Arquitetura e padrões / Architecture and patterns

> **Estado / Status:** decisões registradas no ADR-001. A primeira implementação hexagonal cobre Acesso e Contas; Pagamentos e Cartões ainda estão planejados. Testes H2 e PostgreSQL aprovados no JDK 21 com Docker em 26/09/2026. / Decisions recorded in ADR-001; first hexagonal implementation covers Access and Accounts. Payments and Cards remain planned. H2 and PostgreSQL tests passed on JDK 21 with Docker on 26 September 2026.

## Português

### Arquitetura escolhida

Um **monólito modular** reúne [quatro contextos](domain.md) em uma aplicação Java/Spring Boot e um PostgreSQL. Começamos por Acesso e Contas; Pagamentos e Cartões entram quando seus casos de uso forem implementados. **DDD** define a linguagem e as regras; **arquitetura hexagonal** separa domínio, entrada HTTP e saída para banco; **módulos** restringem dependências entre partes do negócio. Esses termos descrevem responsabilidades diferentes da mesma aplicação.

| Parte de um módulo | Responsabilidade | Exemplo |
| --- | --- | --- |
| Domínio | Agregados, valores e regras, sem Spring, HTTP ou JPA | Conta recusa débito acima do saldo. |
| Aplicação | Casos de uso, autorização, transações e portas para dependências | Transferir coordena contas e registros. |
| Adaptador de entrada | HTTP, validação de formato e mapeamento de DTO | Controller chama Transferir. |
| Adaptador de saída | Implementa portas de persistência | Adaptador JPA salva Conta. |

Pacotes de primeiro nível são `access`, `accounts`, `payments` e `cards`; dentro de cada módulo usamos `domain`, `application` e `adapter` à medida que surgirem classes reais. Acesso solicita a criação da Conta pela API pública de Contas; Pagamentos e Cartões chamam operações públicas de Contas. Contas não depende deles. Um controller não manipula um repositório diretamente, e um módulo não importa entidades JPA de outro.

### Persistência, transações e acesso

| Decisão | Aplicação neste projeto |
| --- | --- |
| Banco | Um PostgreSQL para todos os módulos, com tabelas de propriedade de cada módulo e transações locais. Não é necessário separar schemas para começar. |
| Saldo e extrato | Saldo persistido + movimentações imutáveis, atualizados na mesma transação. |
| Concorrência | Bloqueio ou verificação de versão para escritas simultâneas; contas de uma transferência obtidas em ordem estável para reduzir deadlocks. |
| Idempotência | Chave única por usuário e tipo de operação, vinculada aos dados do pedido e ao resultado; garantida também por restrição no banco. |
| Migrações | Flyway aplica SQL versionado: `V1__initial_banking_schema.sql`, depois `V2__...`. Mudanças aplicadas recebem uma migração nova, preservando o histórico. |
| Login local | Spring Security com senha armazenada como hash, sessão HTTP via cookie e proteção CSRF nas ações que mudam estado; o caso de uso confere a titularidade a partir da sessão. |

O caso de uso abre uma transação para cada operação crítica. Cadastro cria usuário e conta juntos; crédito de demonstração atualiza saldo, extrato e chave de idempotência na mesma transação. Uma falha reverte essas mudanças. Pix, cobrança e fatura aplicarão a mesma regra quando forem implementados. A base usa **Java 21, Spring Boot 4.1.1, PostgreSQL, Flyway, Spring Security e Maven**. O login usa a sessão HTTP padrão do Spring Security; o token CSRF é obtido em `/api/csrf`.

**Código desta etapa:** `access` possui Usuário, cadastro, porta de usuários e adaptadores HTTP, JPA e segurança. `accounts` possui Conta, Movimentação, casos de uso, portas de persistência e adaptadores HTTP/JPA. `shared` contém Dinheiro, identidade autenticada e respostas de erro comuns. O domínio é Java sem anotações do Spring/JPA. Acesso chama a porta pública `OpenAccount` de Contas; os controllers chamam serviços da aplicação. O bloqueio pessimista da Conta serializa créditos concorrentes do mesmo titular antes de verificar a chave de idempotência. A restrição única `(owner_id, idempotency_key)` protege o banco como segunda linha de defesa.

### Design patterns selecionados

| Padrão | Exemplo concreto | Motivo |
| --- | --- | --- |
| **Aggregate** (DDD) | Conta, Transferência, Cobrança, Cartão, Fatura | Proteger as regras locais de cada raiz. |
| **Value Object** (DDD) | Dinheiro: valor decimal + moeda BRL | Centralizar escala, comparação e validação de valores. |
| **Application Service / Use Case** | `Transferir`, `PagarFatura` | Orquestrar autorização, agregados e transação. |
| **Repository** | Porta `ContaRepository` e adaptador JPA | Persistir agregados sem colocar JPA no domínio. |
| **Adapter** | Controller HTTP e adaptador de banco | Traduzir entradas e saídas para as portas da aplicação. |
| **Método estático de criação** | `Conta.abrir(...)`, `Fatura.abrir(...)` | Criar agregados já válidos com nomes que expressam intenção; sem fábrica genérica. |

**Distinção:** Aggregate e Value Object vêm do DDD; Application Service e Repository estruturam casos de uso e persistência; Adapter é um padrão de projeto; o método estático de criação é uma técnica de construção, não o padrão GoF Factory Method, que depende de especialização. **Idempotência, transação e controle de concorrência** são garantias técnicas documentadas acima.

**Decisão de não introduzir agora:** Strategy para vários tipos de pagamento, classes do padrão State para cada estado de fatura e Observer para mudanças financeiras. Há uma regra de cada tipo e transações locais suficientes; estados serão transições do domínio, e eventos futuros só servirão a efeitos secundários. Também não há requisito para microsserviços, filas, saga, CQRS ou event sourcing. Podemos adicionar Spring Modulith para verificar ciclos e limites dos módulos quando houver classes; não é dependência da base atual.

### Testes e verificação desta etapa

1. Nesta etapa: `mvnw test` roda regras de Conta e um fluxo HTTP completo com H2, incluindo sessão, CSRF, repetição do crédito e extrato.
2. `mvnw verify -Ppostgres-tests` acrescenta PostgreSQL via Testcontainers para exercitar migração, bloqueio e repetição concorrente.
3. Etapas futuras: testar transferências, limites do cartão e transições da fatura, inclusive fim de mês e dia 10.

Os testes H2 e PostgreSQL foram executados com sucesso no JDK 21 e Docker em 26/09/2026. H2 oferece feedback rápido; o teste opcional de PostgreSQL confirma o comportamento específico do banco escolhido.

## English

### Chosen architecture

A **modular monolith** keeps the [four contexts](domain.md) in one Java/Spring Boot application backed by PostgreSQL. Access and Accounts come first; Payments and Cards are added with their use cases. **DDD** defines language and business rules; **hexagonal architecture** separates domain, HTTP input, and database output; **modules** restrict dependencies between business areas. These terms describe different responsibilities within the same application.

| Part of a module | Responsibility | Example |
| --- | --- | --- |
| Domain | Aggregates, values, and rules, without Spring, HTTP, or JPA | Account refuses a debit above its balance. |
| Application | Use cases, authorization, transactions, and ports for dependencies | Transfer coordinates accounts and records. |
| Incoming adapter | HTTP, format validation, and DTO mapping | Controller calls Transfer. |
| Outgoing adapter | Implements persistence ports | JPA adapter saves Account. |

First-level packages are `access`, `accounts`, `payments`, and `cards`; inside each module, `domain`, `application`, and `adapter` appear as real classes are added. Access requests Account creation through the public Accounts API; Payments and Cards call public Accounts operations. Accounts does not depend on them. Controllers do not manipulate repositories directly, and modules do not import one another's JPA entities.

### Persistence, transactions, and access

| Decision | Application in this project |
| --- | --- |
| Database | One PostgreSQL database for all modules, with tables owned by each module and local transactions. Separate schemas are unnecessary at first. |
| Balance and statement | Persisted balance plus immutable entries, updated in the same transaction. |
| Concurrency | Locking or version checks for simultaneous writes; accounts in a transfer acquired in a stable order to reduce deadlocks. |
| Idempotency | A unique key per user and operation type, tied to request data and outcome; also enforced by a database constraint. |
| Migrations | Flyway applies versioned SQL: `V1__initial_banking_schema.sql`, then `V2__...`. A new migration changes an applied schema while preserving history. |
| Local login | Spring Security with hashed passwords, an HTTP session cookie, and CSRF protection for state-changing actions; use cases check ownership from the session. |

A use case opens a transaction for each critical operation. Registration creates the user and account together; demo funding updates the balance, statement entry, and idempotency key in one transaction. Failure rolls back these changes. Pix, bills, and invoices will apply the same rule when implemented. The foundation uses **Java 21, Spring Boot 4.1.1, PostgreSQL, Flyway, Spring Security, and Maven**. Login uses Spring Security's HTTP session; `/api/csrf` provides the CSRF token.

**Code in this stage:** `access` owns User, registration, its user port, and HTTP/JPA/security adapters. `accounts` owns Account, Entry, use cases, persistence ports, and HTTP/JPA adapters. `shared` contains Money, authenticated identity, and shared API errors. Domain types are plain Java without Spring/JPA annotations. Access calls Accounts' public `OpenAccount` port; controllers call application services. A pessimistic Account lock serializes concurrent credits for the same owner before checking the idempotency key. The unique `(owner_id, idempotency_key)` constraint adds database protection.

### Selected design patterns

| Pattern | Concrete example | Reason |
| --- | --- | --- |
| **Aggregate** (DDD) | Account, Transfer, Bill, Card, Invoice | Protect each root's local rules. |
| **Value Object** (DDD) | Money: decimal amount + BRL currency | Centralize scale, comparison, and amount validation. |
| **Application Service / Use Case** | `Transfer`, `PayInvoice` | Coordinate authorization, aggregates, and transaction. |
| **Repository** | `AccountRepository` port and JPA adapter | Persist aggregates without putting JPA in the domain. |
| **Adapter** | HTTP controller and database adapter | Translate inputs and outputs for application ports. |
| **Named static creation method** | `Account.open(...)`, `Invoice.open(...)` | Create valid aggregates with intentional names; no generic factory class. |

**Distinction:** Aggregate and Value Object are DDD patterns; Application Service and Repository organize use cases and persistence; Adapter is a design pattern; the named static creation method is a construction technique, not the GoF Factory Method pattern, which relies on specialization. **Idempotency, transactions, and concurrency control** are technical guarantees documented above.

**Deferred by decision:** Strategy for multiple payment types, separate State-pattern classes for each invoice status, and Observer for financial changes. Each flow has one rule and local transactions are enough; invoice states use domain transitions, and future events would serve only secondary effects. Microservices, queues, saga, CQRS, and event sourcing are also unnecessary for the current requirements. Spring Modulith can later verify module cycles and boundaries after classes exist; it is not a dependency of the current foundation.

### Tests and verification for this stage

1. In this stage, `mvnw test` exercises Account rules and the full HTTP flow with H2, including session, CSRF, credit retries, and statement.
2. `mvnw verify -Ppostgres-tests` adds a Testcontainers PostgreSQL test for migrations, locking, and concurrent retries.
3. Future stages will test transfers, card limits, and invoice transitions, including month-end and the 10th.

The H2 and PostgreSQL tests passed on JDK 21 with Docker on 26 September 2026. H2 provides fast feedback; the optional PostgreSQL test verifies behavior specific to the chosen database.
