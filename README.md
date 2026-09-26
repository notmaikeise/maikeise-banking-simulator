<div align="center">

# MAIKEISE · BANKING SIMULATOR

**Simulador bancário educacional em Java e Spring Boot**  
**Educational banking simulator built with Java and Spring Boot**

[Português](#portugues) · [English](#english) · [Documentação / Documentation](#documentacao)

<br>

<img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/java/java-original.svg" width="42" height="42" alt="Java">
&nbsp;&nbsp;
<img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/spring/spring-original.svg" width="42" height="42" alt="Spring Boot">
&nbsp;&nbsp;
<img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/maven/maven-original.svg" width="42" height="42" alt="Apache Maven">
&nbsp;&nbsp;
<img src="https://raw.githubusercontent.com/devicons/devicon/v2.17.0/icons/junit/junit-original.svg" width="42" height="42" alt="JUnit 5">

<br>

**Java 21 · Spring Boot 4.1.1 · Maven Wrapper · JUnit 5**

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
| Modelo DDD, arquitetura e padrões | [Documentados para revisão](docs/decisions/ADR-001-domain-and-architecture.md) |
| Cadastro, login, conta, crédito de demonstração e extrato | Planejados |
| Pix interno simulado e boletos fictícios | Planejados |
| Cartão virtual, limite e fatura mensal | Planejados |
| Interface | Planejada após os fluxos do domínio |

**O que funciona hoje:** a aplicação inicia e o teste inicial de carregamento passa. Ainda não existem endpoints bancários, autenticação nem banco de dados configurado. PostgreSQL, Flyway e Spring Security foram escolhidos para a implementação futura, mas **ainda não foram adicionados ao projeto**.

### Decisões de modelagem

- **DDD:** Acesso, Contas, Pagamentos e Cartões são os limites propostos do domínio. Conta, Transferência, Cobrança, Cartão e Fatura protegem regras distintas.
- **Arquitetura:** monólito modular com domínio separado de HTTP e persistência. As dependências entre módulos passam por operações públicas.
- **Padrões selecionados:** Aggregate, Value Object, Application Service, Repository, Adapter e métodos de criação com nomes explícitos. Idempotência, transações locais e controle de concorrência são garantias das operações financeiras documentadas separadamente.
- **Dinheiro e fatura:** saldo persistido com movimentações imutáveis; compras ocupam o limite até o pagamento integral da fatura. O ciclo fecha no último dia do mês e vence no dia 10 do mês seguinte.

As regras detalhadas e os motivos de cada escolha estão na [documentação](#documentacao).

### Executar localmente

Requer **JDK 21**. O projeto inclui o Maven Wrapper, portanto não exige instalar o Maven separadamente.

| Sistema | Testar | Iniciar |
| --- | --- | --- |
| Windows | `.\mvnw.cmd test` | `.\mvnw.cmd spring-boot:run` |
| macOS / Linux | `bash ./mvnw test` | `bash ./mvnw spring-boot:run` |

Execute os comandos na raiz do repositório. Ainda não há telas ou endpoints bancários para acessar após iniciar.

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
| DDD model, architecture, and patterns | [Documented for review](docs/decisions/ADR-001-domain-and-architecture.md) |
| Registration, login, account, demo funding, and statement | Planned |
| Simulated internal Pix and fictional bills | Planned |
| Virtual card, limit, and monthly invoice | Planned |
| Interface | Planned after the domain flows |

**What works today:** the application starts and its initial context-loading test passes. There are no banking endpoints, authentication, or database configuration yet. PostgreSQL, Flyway, and Spring Security have been selected for later implementation but **have not been added to the project**.

### Modeling decisions

- **DDD:** Access, Accounts, Payments, and Cards are the proposed domain boundaries. Account, Transfer, Bill, Card, and Invoice protect different rules.
- **Architecture:** a modular monolith with the domain separated from HTTP and persistence. Modules interact through public operations.
- **Selected patterns:** Aggregate, Value Object, Application Service, Repository, Adapter, and explicitly named creation methods. Idempotency, local transactions, and concurrency control are guarantees for financial operations documented separately.
- **Money and invoices:** stored balance plus immutable entries; purchases use the limit until their invoices are fully paid. A cycle closes on the last day of the month and is due on the 10th of the next month.

Detailed rules and the reasoning behind each choice are in the [documentation](#documentacao).

### Run locally

Requires **JDK 21**. The Maven Wrapper is included, so a separate Maven installation is unnecessary.

| System | Test | Start |
| --- | --- | --- |
| Windows | `.\mvnw.cmd test` | `.\mvnw.cmd spring-boot:run` |
| macOS / Linux | `bash ./mvnw test` | `bash ./mvnw spring-boot:run` |

Run these commands from the repository root. There are no screens or banking endpoints to visit after startup yet.

**Simulation boundary:** no real money, external Pix, valid bank payment line, usable card, or banking network integration.

---

<a id="documentacao"></a>

## Documentação / Documentation

| Arquivo / File | Conteúdo / Content |
| --- | --- |
| [Escopo / Scope](docs/scope.md) | Jornada, regras e limites / User journey, rules, and boundaries |
| [Domínio / Domain](docs/domain.md) | Contextos, agregados e invariantes / Contexts, aggregates, and invariants |
| [Arquitetura / Architecture](docs/architecture.md) | Módulos, padrões, transações e testes / Modules, patterns, transactions, and tests |
| [ADR-001](docs/decisions/ADR-001-domain-and-architecture.md) | Registro das decisões / Decision record |
| [Cards / Issues](../../issues) | Etapas de desenvolvimento / Development stages |

<sub>Ícones / Icons: <a href="https://github.com/devicons/devicon/tree/v2.17.0">Devicon v2.17.0</a>.</sub>
