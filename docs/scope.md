# Visão e escopo / Product vision and scope

## Português

**Objetivo.** Construir, individualmente, um simulador bancário local para estudar Java, Spring Boot, DDD, persistência, segurança e testes com funcionalidades implementadas aos poucos.

**Jornada planejada.** Uma pessoa fictícia se cadastra, consulta sua conta em BRL, adiciona saldo de demonstração, transfere a outro usuário do app, paga um boleto criado para teste, simula uma compra no cartão e paga a fatura com saldo da conta.

### Decisões iniciais

| Tema | Decisão |
| --- | --- |
| Pix | Apenas entre contas cadastradas na aplicação. |
| Saldo de teste | Crédito manual de demonstração, registrado no extrato. |
| Cartão inicial | Compras à vista, limite disponível e pagamento integral da fatura. |
| Boleto | Cobrança fictícia criada no app, com identificador que não é uma linha digitável bancária válida. |

### Regras do primeiro recorte

- Valores em BRL devem ser positivos nas operações e ter até duas casas decimais. O saldo da conta não pode ficar negativo.
- Uma transferência interna debita uma conta e credita outra com o mesmo valor. Uma repetição da mesma solicitação não pode transferir duas vezes.
- Um boleto fictício pago não pode ser pago novamente.
- Uma compra no cartão utiliza limite e entra na fatura; ela não debita o saldo da conta no momento da compra. Pagar a fatura integralmente usa o saldo da conta.

**Limites.** Não há transações reais, destinatários Pix externos, boletos bancários válidos, cartões utilizáveis ou compras parceladas nesta primeira versão. Os módulos e agregados de DDD serão definidos conforme os fluxos forem implementados, mantendo a documentação ligada ao código.

**Estado:** requisitos iniciais definidos e base executável criada com Java 21, Spring Boot 4.1.1 e Maven; teste inicial passou. Próximo card: cadastro, conta e extrato de demonstração.

## English

**Goal.** Build a locally runnable banking simulator solo to study Java, Spring Boot, DDD, persistence, security, and testing while adding features in small steps.

**Planned journey.** A fictional user registers, checks a BRL account, adds demo funds, transfers to another app user, pays a bill created for testing, simulates a card purchase, and pays the invoice using the account balance.

### Initial decisions

| Topic | Decision |
| --- | --- |
| Pix | Only between accounts registered in the application. |
| Demo balance | Manual demo credit, recorded in the account statement. |
| Initial card | Single-payment purchases, available limit, and full invoice payment. |
| Bill | Fictional bill created in the app, with an identifier that is not a valid bank payment line. |

### Rules for the first scope

- BRL amounts must be positive for operations and have at most two decimal places. The account balance cannot become negative.
- An internal transfer debits one account and credits another by the same amount. Retrying the same request must not transfer twice.
- A paid fictional bill cannot be paid again.
- A card purchase uses the limit and appears on the invoice; it does not debit the account balance at purchase time. Full invoice payment uses the account balance.

**Boundaries.** This first version has no real transactions, external Pix recipients, valid bank bills, usable cards, or installment purchases. DDD modules and aggregates will be defined as flows are implemented so the documentation stays connected to the code.

**Status:** initial requirements defined and a runnable foundation created with Java 21, Spring Boot 4.1.1, and Maven; the initial test passed. Next card: registration, account, and demo statement.
