# Arquitetura e padrões / Architecture and patterns

> **Estado / Status:** proposta para revisão. Descreve a arquitetura pretendida; os módulos, a persistência e a segurança ainda não foram implementados.

## Português

### Decisão arquitetural

Um **monólito modular** reúne os quatro contextos de [domain.md](domain.md) em uma aplicação Java/Spring Boot e um PostgreSQL. Começamos por Acesso e Contas; Pagamentos e Cartões entram conforme os casos de uso forem construídos. **DDD** dá nomes e regras ao domínio; **arquitetura hexagonal** mantém regras independentes de HTTP e banco; **módulos** limitam quem pode depender de quem. A base existente usa Java 21, Spring Boot 4.1.1 e Maven. PostgreSQL, Flyway e segurança foram escolhidos para as próximas etapas, mas ainda não constam do `pom.xml`.

| Parte de cada módulo | Responsabilidade | Exemplo |
| --- | --- | --- |
| Domínio | Estado, comportamentos e regras, sem dependências do Spring ou JPA. | Conta impede débito acima do saldo. |
| Aplicação | Caso de uso, autorização, transação e coordenação de agregados; define portas necessárias. | Transferir valida origem/destino e coordena registros. |
| Adaptadores de entrada | Recebem HTTP, validam formato e traduzem DTOs para casos de uso. | Controller de transferências. |
| Adaptadores de saída | Implementam portas para banco ou serviços externos. | Repositório JPA de contas. |

Pacotes de primeiro nível representam `access`, `accounts`, `payments` e `cards`; dentro de cada um usamos `domain`, `application` e `adapter` quando houver código correspondente. O domínio não importa classes de adaptadores. Acesso cria a identidade e solicita a criação da Conta via API pública de Contas; Pagamentos e Cartões usam apenas APIs públicas de Contas. Contas não depende dos demais. Controllers não acessam repositórios diretamente; módulos não compartilham entidades JPA.

### Persistência, consistência e segurança

- Uma instância da aplicação e um PostgreSQL, com transações locais. Cada módulo possui suas tabelas, mesmo que inicialmente compartilhem um único schema. Operações que alteram vários agregados críticos são coordenadas por um caso de uso em uma transação: ou tudo é confirmado, ou tudo é revertido.
- Saldo persistido mais movimentações imutáveis. Escritas concorrentes nas mesmas contas exigem bloqueio/controle de versão; transferências devem obter contas em ordem estável para reduzir impasses. Testes de integração devem cobrir saldo insuficiente, concorrência e repetição de comandos.
- **Flyway** versiona o esquema SQL. Exemplo: `V1__create_accounts.sql` cria as tabelas; uma alteração posterior ganha `V2__...sql`. Migrações aplicadas não são editadas. Spring Boot executa as migrações pendentes na inicialização quando configurado. Não há migrações nem dependências de banco adicionadas nesta proposta documental.
- Login e autorização entram antes de expor operações bancárias. Recomendação inicial: Spring Security, senha armazenada com hash e sessão HTTP protegida; verificar titularidade no caso de uso, não confiar em um identificador de usuário enviado pelo cliente. Quando a interface for definida, revisaremos o encaixe da sessão e a proteção CSRF.
- IDs de operações e uma chave de idempotência única por usuário e tipo de comando ligam recibo, lançamentos e tentativas. A comparação de dados impede que a mesma chave seja reaproveitada para outra operação.

### Padrões com propósito

| Padrão | Onde entra | Por quê |
| --- | --- | --- |
| Aggregate + Value Object | Conta, Transferência, Cobrança, Cartão, Fatura; Dinheiro em BRL | Proteger invariantes e impedir valores inválidos. |
| Use Case / Application Service | Cadastro, crédito de teste, transferência, pagamento, compra | Coordenar autorização, transações e objetos de domínio. |
| Repository port + adapter | Persistência de cada agregado | Trocar detalhes do banco sem levar SQL/JPA para o domínio. |
| Idempotent command | Crédito, Pix, boleto, compra, fatura | Permitir repetição segura de pedidos. |
| Clock injetável | Fechamento, vencimento e testes da fatura | Ter regras temporais reproduzíveis. |

Não há motivo atual para introduzir microsserviços, filas, saga, CQRS ou event sourcing. Um evento de domínio pode ser útil no futuro para efeitos secundários, mas a transferência e os pagamentos mantêm suas mudanças financeiras na mesma transação local. Spring Modulith poderá verificar ciclos e limites entre pacotes quando os módulos existirem; a biblioteca ainda não foi adicionada.

**Verificação prevista:** testes unitários das invariantes; testes com PostgreSQL real em contêiner para transações, idempotência e concorrência; testes HTTP de login, autorização e validação. O teste atual de inicialização continua sendo apenas um teste da base executável.

## English

### Architectural decision

A **modular monolith** keeps the four contexts in [domain.md](domain.md) in one Java/Spring Boot application backed by PostgreSQL. Access and Accounts are built first, then Payments and Cards as use cases are added. DDD names and models business rules; hexagonal architecture keeps rules independent of HTTP and persistence; modules restrict dependencies. The existing foundation is Java 21, Spring Boot 4.1.1, and Maven. PostgreSQL, Flyway, and login are selected for later implementation; their dependencies are not in the project yet.

Each module contains domain logic, application use cases and ports, incoming HTTP adapters, and outgoing persistence adapters as needed. First-level packages are `access`, `accounts`, `payments`, and `cards`. The domain does not depend on Spring, JPA, or adapters. Other modules communicate through public operations and identifiers, never by sharing JPA entities, tables, or repositories. Access may request account creation; Payments and Cards may request account operations; Accounts does not depend on them.

One PostgreSQL database and local transactions keep related changes atomic. Persisted account balances and immutable entries change together. Concurrent writes need account locking or version checks; accounts in a transfer are acquired in a stable order. Flyway stores ordered, versioned SQL migrations such as `V1__create_accounts.sql`, and applied migrations are not edited. Login and ownership checks precede exposed financial operations; the initial recommendation is Spring Security with password hashes and a protected HTTP session, to be revisited with the UI and CSRF design.

The chosen patterns are **aggregates** for invariants, a BRL **Money value object**, **use cases** for coordination, **repository ports and adapters** for persistence, **idempotent commands** for safe retries, and an injected **Clock** for reproducible billing rules. No microservices, message broker, saga, CQRS, or event sourcing are required by the current flows. Spring Modulith may later verify module boundaries; it has not been added to the build.

Verification will include domain unit tests, PostgreSQL-backed integration tests for transactions/concurrency/retries, and HTTP security tests. The current context-loading test only verifies that the application foundation starts.
