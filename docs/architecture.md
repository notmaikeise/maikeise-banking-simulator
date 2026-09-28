# Arquitetura e padrões / Architecture and patterns

> **Estado / Status:** ADR-001 define os limites; ADR-002 registra o Pix interno. Acesso, Contas e o Pix de Pagamentos estão implementados; Cobranças e Cartões seguem planejados. A autora executou os testes H2 e PostgreSQL com sucesso no JDK 21 e Docker em 27/09/2026. / ADR-001 defines the boundaries; ADR-002 records internal Pix. Access, Accounts, and Payments' Pix flow are implemented; Bills and Cards remain planned. The author successfully ran H2 and PostgreSQL tests on JDK 21 with Docker on 27 September 2026.

## Português

### Arquitetura escolhida

Um **monólito modular** reúne [quatro contextos](domain.md) em uma aplicação Java/Spring Boot e um PostgreSQL. Acesso, Contas e a transferência de Pagamentos já existem; Cobranças e Cartões entram com seus casos de uso. **DDD** define a linguagem e as regras; **arquitetura hexagonal** separa domínio, entrada HTTP e saída para banco; **módulos** restringem dependências entre partes do negócio. Esses termos descrevem responsabilidades diferentes da mesma aplicação.

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

O caso de uso abre uma transação para cada operação crítica. Cadastro cria usuário e conta juntos; crédito de demonstração atualiza saldo, extrato e chave de idempotência na mesma transação. Pix atualiza ambos os saldos, duas movimentações e a Transferência na mesma transação; Cobranças e Faturas aplicarão a mesma regra depois. Uma falha reverte todas as mudanças da operação. A base usa **Java 21, Spring Boot 4.1.1, PostgreSQL, Flyway, Spring Security e Maven**. O login usa a sessão HTTP padrão do Spring Security; o token CSRF é obtido em `/api/csrf`.

**Código desta etapa:** `access` possui Usuário e autenticação; `accounts` possui Conta, Movimentação e a operação pública `TransferFunds`; `payments` possui Transferência, caso de uso e adaptadores HTTP/JPA. `shared` contém Dinheiro, identidade autenticada e respostas de erro comuns. O domínio é Java sem anotações do Spring/JPA. Acesso chama `OpenAccount`; Pagamentos chama `TransferFunds`, sem importar entidades JPA de Contas. Os controllers chamam os serviços da aplicação. Antes de movimentar, Pix bloqueia pessimisticamente as duas Contas por ordem crescente de ID e consulta a chave novamente após obter os bloqueios. A restrição única `(owner_id, idempotency_key)` na tabela de transferências protege o banco como segunda linha de defesa. Flyway `V2__internal_transfers.sql` acrescenta a tabela e a referência das movimentações sem editar a migração `V1`. Detalhes em [ADR-002](decisions/ADR-002-internal-transfers.md).

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

1. `mvnw test` inclui regras de Conta e fluxos HTTP completos com H2: cadastro, sessão, CSRF, crédito, Pix, extrato dos dois participantes e tentativas inválidas.
2. `mvnw verify -Ppostgres-tests` acrescenta PostgreSQL via Testcontainers para exercitar migrações, repetição concorrente da mesma chave e transferências em sentidos opostos.
3. Etapas futuras: testar cobranças, limites do cartão e transições da fatura, inclusive fim de mês e dia 10.

O comando `.\mvnw.cmd verify -Ppostgres-tests` passou no JDK 21 com Docker em 27/09/2026, incluindo os testes novos do Pix. H2 oferece feedback rápido; o perfil PostgreSQL confirma o comportamento específico do banco escolhido.

## English

### Chosen architecture

A **modular monolith** keeps the [four contexts](domain.md) in one Java/Spring Boot application backed by PostgreSQL. Access, Accounts, and Payments' transfer flow are implemented; Bills and Cards will arrive with their use cases. **DDD** defines language and business rules; **hexagonal architecture** separates domain, HTTP input, and database output; **modules** restrict dependencies between business areas. These terms describe different responsibilities within the same application.

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

A use case opens a transaction for each critical operation. Registration creates the user and account together; demo funding updates the balance, statement entry, and idempotency key in one transaction. Pix updates both balances, two entries, and the Transfer in one transaction; Bills and Invoices will follow the same rule later. Any failure rolls back the operation. The foundation uses **Java 21, Spring Boot 4.1.1, PostgreSQL, Flyway, Spring Security, and Maven**. Login uses Spring Security's HTTP session; `/api/csrf` provides the CSRF token.

**Code in this stage:** `access` owns User and authentication; `accounts` owns Account, Entry, and the public `TransferFunds` operation; `payments` owns Transfer, its use case, and HTTP/JPA adapters. `shared` contains Money, authenticated identity, and shared API errors. Domain types are plain Java without Spring/JPA annotations. Access calls `OpenAccount`; Payments calls `TransferFunds` without importing Accounts' JPA entities. Controllers call application services. Pix locks both Accounts in ascending ID order and checks the key again after locking. The transfer table's unique `(owner_id, idempotency_key)` constraint adds database protection. Flyway `V2__internal_transfers.sql` adds the table and entry reference without editing `V1`. See [ADR-002](decisions/ADR-002-internal-transfers.md).

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

1. `mvnw test` covers Account rules and H2 HTTP flows: registration, session, CSRF, funding, Pix, statements for both users, and invalid attempts.
2. `mvnw verify -Ppostgres-tests` adds Testcontainers PostgreSQL tests for migrations, concurrent retries with the same key, and opposite-direction transfers.
3. Future stages will test bills, card limits, and invoice transitions, including month-end and the 10th.

The author successfully ran `.\mvnw.cmd verify -Ppostgres-tests` on JDK 21 with Docker on 27 September 2026, including the new Pix tests. H2 provides fast feedback; the PostgreSQL profile verifies database-specific behavior.
