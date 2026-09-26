# Arquitetura e padrões / Architecture and patterns

> **Estado / Status:** arquitetura e padrões selecionados para a implementação, registrados para revisão no ADR-001. A base executável ainda não implementa os módulos.

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
| Migrações | Flyway aplica SQL versionado: `V1__create_accounts.sql`, depois `V2__...`. Mudanças aplicadas recebem uma migração nova, preservando o histórico. |
| Login local | Spring Security com senha armazenada como hash, sessão HTTP via cookie e proteção CSRF nas ações que mudam estado; o caso de uso confere a titularidade a partir da sessão. |

O caso de uso abre uma transação para cada operação crítica. Cadastro cria usuário e conta juntos; Pix altera as duas contas; cobrança e fatura atualizam o estado do pagamento com o débito. Uma falha reverte todas as mudanças daquela operação. A base atual usa **Java 21, Spring Boot 4.1.1 e Maven**; PostgreSQL, Flyway, persistência e segurança são escolhas de implementação futura e **não constam ainda do `pom.xml`**.

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

### Como verificar quando implementarmos

1. Testes unitários do domínio: dinheiro válido, saldo insuficiente, limite do cartão e transições da fatura, incluindo fim de mês e dia 10.
2. Testes de integração com PostgreSQL em contêiner: transações, migrações, concorrência, idempotência e restrições únicas.
3. Testes HTTP e de segurança: login, sessão, CSRF, validação, titularidade e respostas de conflito.

O teste atual apenas verifica que o contexto Spring inicia; não comprova nenhuma regra bancária.

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
| Migrations | Flyway applies versioned SQL: `V1__create_accounts.sql`, then `V2__...`. A new migration changes an applied schema while preserving history. |
| Local login | Spring Security with hashed passwords, an HTTP session cookie, and CSRF protection for state-changing actions; use cases check ownership from the session. |

A use case opens a transaction for each critical operation. Registration creates the user and account together; internal Pix updates both accounts; bill and invoice payments update payment state with the debit. Failure rolls back every change in that operation. The current foundation uses **Java 21, Spring Boot 4.1.1, and Maven**; PostgreSQL, Flyway, persistence, and security are future implementation choices and **are not in `pom.xml` yet**.

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

### How to verify this during implementation

1. Domain unit tests: valid money, insufficient balance, card limit, and invoice transitions, including month-end and the 10th.
2. Integration tests with PostgreSQL in a container: transactions, migrations, concurrency, idempotency, and unique constraints.
3. HTTP and security tests: login, session, CSRF, validation, ownership, and conflict responses.

The current test only verifies Spring context startup; it does not prove any banking rule.
