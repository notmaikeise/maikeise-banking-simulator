# Maikeise Banking Simulator

> Simulador educacional em construção · Educational simulator in progress

## Português

Projeto individual em Java e Spring Boot para praticar modelagem de domínio, APIs, testes e arquitetura enquanto construo fluxos de um banco fictício.

**Escopo planejado:** cadastro e conta em BRL; crédito de demonstração manual; Pix simulado entre usuários do app; pagamento de boletos fictícios; cartão virtual com compras à vista, limite e fatura.

**Estado atual:** [escopo inicial definido](docs/scope.md) e base executável criada com Java 21, Spring Boot 4.1.1 e Maven. O teste inicial passou; os fluxos bancários ainda não foram implementados. O trabalho é acompanhado pelos [cards do projeto](../../issues).

### Execução local

Requer JDK 21. No Windows, use `.\mvnw.cmd test` para executar o teste e `.\mvnw.cmd spring-boot:run` para iniciar. No macOS/Linux, use `./mvnw test` e `./mvnw spring-boot:run`. Ainda não há endpoints bancários.

Nenhuma operação envolve dinheiro real ou integração com sistemas bancários, Pix, boletos ou redes de cartão.

## English

A solo Java and Spring Boot project to practice domain modeling, APIs, testing, and architecture while building fictional banking flows.

**Planned scope:** registration and a BRL account; manual demo funds; simulated Pix between app users; fictional bill payments; a virtual card with single-payment purchases, a limit, and an invoice.

**Current status:** [initial scope defined](docs/scope.md) and a runnable foundation created with Java 21, Spring Boot 4.1.1, and Maven. The initial test passed; banking flows are not implemented yet. Work is tracked in the [project cards](../../issues).

### Run locally

Requires JDK 21. On Windows, run `.\mvnw.cmd test` to execute the test and `.\mvnw.cmd spring-boot:run` to start the app. On macOS/Linux, use `./mvnw test` and `./mvnw spring-boot:run`. There are no banking endpoints yet.

No operation involves real money or integration with banking systems, Pix, bill payment, or card networks.
