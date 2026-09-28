<div align="center">

# MAIKEISE · BANKING SIMULATOR

**Simulador bancário educacional em Java e Spring Boot**  
**Educational banking simulator built with Java and Spring Boot**

[Português](#portugues) · [English](#english) · [Documentação / Documentation](#documentacao)

<br>

<table>
<tr>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/java/java-original.svg" width="42" height="42" alt="Java"><br><sub>Java 21</sub></td>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/spring/spring-original.svg" width="42" height="42" alt="Spring"><br><sub>Spring Boot</sub></td>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/postgresql/postgresql-original.svg" width="42" height="42" alt="PostgreSQL"><br><sub>PostgreSQL</sub></td>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/docker/docker-original.svg" width="42" height="42" alt="Docker"><br><sub>Docker</sub></td>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/maven/maven-original.svg" width="42" height="42" alt="Maven"><br><sub>Maven</sub></td>
<td align="center"><img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/junit/junit-original.svg" width="42" height="42" alt="JUnit"><br><sub>JUnit 5</sub></td>
</tr>
</table>

<br>

**Java 21 · Spring Boot 4.1.1 · PostgreSQL 17 · Docker Compose · Flyway · Spring Security · Testcontainers**

</div>

---

<a id="portugues"></a>

## Português

### Sobre o projeto

Projeto individual de estudo e portfólio para praticar **modelagem de domínio (DDD)**, arquitetura, persistência, segurança e testes. A proposta é construir, por etapas, os fluxos de um banco fictício: uma pessoa se cadastra, consulta sua conta, adiciona saldo de demonstração, transfere para outra conta do app, paga uma cobrança de teste, compra com cartão virtual e quita a fatura.

### Estado do desenvolvimento

| Etapa | Estado |
| --- | --- |
| Escopo, regras iniciais e base Spring Boot executável | Concluídos |
| Modelo DDD, arquitetura e padrões | [Definidos e documentados](docs/decisions/ADR-001-domain-and-architecture.md) |
| Cadastro, login, conta, crédito de demonstração e extrato | Implementados; testes com H2 e PostgreSQL aprovados no JDK 21 |
| Pix interno simulado entre contas do app | Implementado; testes H2 e PostgreSQL aprovados no JDK 21 |
| Cobranças fictícias | Planejadas |
| Cartão virtual, limite e fatura mensal | Planejados |
| Interface | Planejada após os fluxos do domínio |

**O que esta etapa acrescenta:** Pix interno entre duas contas cadastradas. O envio debita uma conta, credita a outra e registra duas movimentações na mesma transação. A chave de idempotência permite repetir o pedido sem nova transferência; a migração `V2` preserva o histórico do banco. A autora executou `.\mvnw.cmd verify -Ppostgres-tests` com sucesso no JDK 21 e Docker em 27/09/2026.

### Tecnologias utilizadas

| Área | Tecnologia | Uso nesta etapa |
| --- | --- | --- |
| Linguagem e aplicação | Java 21, Spring Boot 4.1.1, Spring Web MVC, Jackson e Jakarta Validation | API HTTP, JSON e validação das entradas. |
| Segurança | Spring Security e BCrypt | Login por sessão HTTP, senha com hash e proteção CSRF. |
| Dados | PostgreSQL 17, Spring Data JPA (Hibernate) e Flyway | Persistência da aplicação e migração SQL versionada. |
| Ambiente local | Docker Desktop e Docker Compose | Executar o PostgreSQL localmente sem instalação separada do banco. |
| Testes | JUnit 5, MockMvc, H2 e Testcontainers | Testes de fluxo HTTP e concorrência num PostgreSQL temporário. |
| Build | Maven Wrapper, Maven Surefire e Maven Failsafe | Compilar, rodar os testes comuns e o teste de integração PostgreSQL. |

### Decisões de modelagem

- **DDD:** Acesso, Contas e Pagamentos (Pix) têm implementações. Cartões e a parte de cobranças de Pagamentos estão planejados. Conta e Transferência protegem suas próprias regras.
- **Arquitetura:** monólito modular com domínio separado de HTTP e persistência. As dependências entre módulos passam por operações públicas.
- **Padrões selecionados:** Aggregate, Value Object, Application Service, Repository, Adapter e métodos de criação com nomes explícitos. Idempotência, transações locais e controle de concorrência são garantias das operações financeiras documentadas separadamente.
- **Dinheiro e fatura:** saldo persistido com movimentações imutáveis; compras ocupam o limite até o pagamento integral da fatura. O ciclo fecha no último dia do mês e vence no dia 10 do mês seguinte.

As regras detalhadas e os motivos de cada escolha estão na [documentação](#documentacao).

### Executar localmente (Windows / PowerShell)

Requer **JDK 21** e **Docker Desktop** rodando com suporte a Docker Compose. O Maven Wrapper já vem no projeto. Na pasta raiz, execute nesta ordem:

1. `docker compose up -d` — inicia apenas o PostgreSQL local. Espere ficar saudável.
2. `.\mvnw.cmd test` — executa testes de domínio e HTTP com H2; este comando também funciona sem Docker.
3. `.\mvnw.cmd verify -Ppostgres-tests` — inclui os testes concorrentes com PostgreSQL em Testcontainers; precisa de Docker.
4. `.\mvnw.cmd spring-boot:run` — inicia a API em `http://localhost:8080`.

macOS/Linux: use `./mvnw` nos comandos 2 a 4. Se já tiver PostgreSQL próprio, configure `DB_URL`, `DB_USER` e `DB_PASSWORD` e dispense o Compose para iniciar a aplicação. As credenciais do `compose.yaml` são **somente para desenvolvimento local**; não publique este banco na internet.

### Experimentar a API

Não há interface gráfica nesta etapa. Veja o [guia da API](docs/api-guide.md) para cadastro, login com cookie de sessão, token CSRF, crédito, Pix e extrato. Todas as operações financeiras são de demonstração.

| Método e caminho | Função |
| --- | --- |
| `GET /api/csrf` | Obtém o token CSRF da sessão. |
| `POST /api/users` | Cadastra usuário e abre uma conta de saldo zero. |
| `POST /login` | Entra usando email e senha; mantém uma sessão HTTP. |
| `GET /api/accounts/me` | Consulta sua própria conta e saldo. |
| `POST /api/accounts/me/demo-credits` | Adiciona crédito fictício com `Idempotency-Key`. |
| `POST /api/transfers` | Envia Pix interno para o ID de outra conta com `Idempotency-Key`. |
| `GET /api/accounts/me/entries` | Lista seu extrato, com `page` e `size`. |

**Limite da simulação:** não há dinheiro real, Pix externo, linha digitável válida, cartão utilizável nem integração com redes bancárias.

---

<a id="english"></a>

## English

### About the project

A solo study and portfolio project to practice **domain modeling (DDD)**, architecture, persistence, security, and testing. The fictional banking flows will be built in stages: a user registers, views an account, adds demo funds, transfers to another app account, pays a test bill, makes a virtual card purchase, and pays the invoice.

### Development status

| Stage | Status |
| --- | --- |
| Scope, initial rules, and runnable Spring Boot foundation | Completed |
| DDD model, architecture, and patterns | [Defined and documented](docs/decisions/ADR-001-domain-and-architecture.md) |
| Registration, login, account, demo funding, and statement | Implemented; H2 and PostgreSQL tests passed on JDK 21 |
| Simulated internal Pix between app accounts | Implemented; H2 and PostgreSQL tests passed on JDK 21 |
| Fictional bills | Planned |
| Virtual card, limit, and monthly invoice | Planned |
| Interface | Planned after the domain flows |

**This stage adds:** internal Pix between two registered accounts. It debits one account, credits the other, and records two entries in the same transaction. An idempotency key makes retries safe, and the `V2` migration preserves the existing database history. The author successfully ran `.\mvnw.cmd verify -Ppostgres-tests` with JDK 21 and Docker on 27 September 2026.

### Technologies used

| Area | Technology | Role in this stage |
| --- | --- | --- |
| Language and application | Java 21, Spring Boot 4.1.1, Spring Web MVC, Jackson, and Jakarta Validation | HTTP API, JSON, and request validation. |
| Security | Spring Security and BCrypt | HTTP session login, hashed passwords, and CSRF protection. |
| Data | PostgreSQL 17, Spring Data JPA (Hibernate), and Flyway | Application persistence and versioned SQL migrations. |
| Local environment | Docker Desktop and Docker Compose | Run PostgreSQL locally without a separate database installation. |
| Tests | JUnit 5, MockMvc, H2, and Testcontainers | HTTP flow and concurrency tests against temporary PostgreSQL. |
| Build | Maven Wrapper, Maven Surefire, and Maven Failsafe | Compile and run regular and PostgreSQL integration tests. |

### Modeling decisions

- **DDD:** Access, Accounts, and Payments (Pix) are implemented. Cards and the Payments bill flow remain planned. Account and Transfer protect their own rules.
- **Architecture:** a modular monolith with the domain separated from HTTP and persistence. Modules interact through public operations.
- **Selected patterns:** Aggregate, Value Object, Application Service, Repository, Adapter, and explicitly named creation methods. Idempotency, local transactions, and concurrency control are guarantees for financial operations documented separately.
- **Money and invoices:** stored balance plus immutable entries; purchases use the limit until their invoices are fully paid. A cycle closes on the last day of the month and is due on the 10th of the next month.

Detailed rules and the reasoning behind each choice are in the [documentation](#documentacao).

### Run locally

Requires **JDK 21** and a running **Docker Desktop** with Docker Compose. Maven is included through the wrapper. From the repository root:

1. `docker compose up -d` — starts the local PostgreSQL database.
2. `.\mvnw.cmd test` — runs domain and HTTP tests using H2; Docker is optional for this step.
3. `.\mvnw.cmd verify -Ppostgres-tests` — includes the concurrent PostgreSQL Testcontainers tests; Docker is required.
4. `.\mvnw.cmd spring-boot:run` — starts the API at `http://localhost:8080`.

On macOS/Linux, use `./mvnw` for steps 2–4. If PostgreSQL is already installed, set `DB_URL`, `DB_USER`, and `DB_PASSWORD` instead of using Compose to start the application. The credentials in `compose.yaml` are **for local development only**.

### Try the API

There is no graphical interface yet. Follow the [API guide](docs/api-guide.md) for registration, session login, CSRF token, demo funding, internal Pix, and statements. Financial operations are fictional.

| Method and path | Purpose |
| --- | --- |
| `GET /api/csrf` | Fetch the current session's CSRF token. |
| `POST /api/users` | Register a user and create a zero-balance account. |
| `POST /login` | Sign in with email and password using an HTTP session. |
| `GET /api/accounts/me` | Read your account and balance. |
| `POST /api/accounts/me/demo-credits` | Add fictional funds with an `Idempotency-Key`. |
| `POST /api/transfers` | Send internal Pix to another account ID with an `Idempotency-Key`. |
| `GET /api/accounts/me/entries` | Read your statement with `page` and `size`. |

**Simulation boundary:** no real money, external Pix, valid bank payment line, usable card, or banking network integration.

---

<a id="documentacao"></a>

## Documentação / Documentation

| Arquivo / File | Conteúdo / Content |
| --- | --- |
| [Escopo / Scope](docs/scope.md) | Jornada, regras e limites / User journey, rules, and boundaries |
| [Domínio / Domain](docs/domain.md) | Contextos, agregados e invariantes / Contexts, aggregates, and invariants |
| [Arquitetura / Architecture](docs/architecture.md) | Módulos, padrões, transações e testes / Modules, patterns, transactions, and tests |
| [Guia da API / API guide](docs/api-guide.md) | Cadastro e uso da API / Registration and API usage |
| [ADR-001](docs/decisions/ADR-001-domain-and-architecture.md) | Registro das decisões / Decision record |
| [ADR-002](docs/decisions/ADR-002-internal-transfers.md) | Transação e concorrência do Pix / Pix transaction and concurrency |
| [Cards / Issues](https://github.com/notmaikeise/maikeise-banking-simulator/issues) | Etapas de desenvolvimento / Development stages |

<sub>Ícones / Icons: <a href="https://github.com/devicons/devicon/tree/v2.17.0">Devicon v2.17.0</a>.</sub>
