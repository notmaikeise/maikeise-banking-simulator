# Modelo de domínio / Domain model

> **Estado / Status:** decisões de produto confirmadas em 25/09/2026; implementação pendente. Este documento descreve o modelo planejado.

## Português

### Linguagem do domínio

| Termo | Significado neste simulador |
| --- | --- |
| Usuário | Pessoa fictícia que faz login e possui uma conta. |
| Conta | Guarda saldo em BRL e identifica o titular. |
| Dinheiro | Valor em BRL com duas casas decimais; operações exigem valor positivo. Usar `BigDecimal`, nunca `double`. |
| Movimentação | Registro imutável de entrada ou saída de uma conta; compõe o extrato. |
| Transferência | Envio simulado entre duas contas cadastradas no app, chamado Pix interno na interface. |
| Cobrança | Boleto fictício criado dentro do app; seu identificador não é uma linha digitável válida. |
| Cartão | Cartão virtual fictício com limite total, utilizado e disponível. |
| Compra | Gasto à vista que ocupa limite e pertence a uma fatura. |
| Fatura | Conjunto de compras de um cartão em uma competência mensal, com fechamento, vencimento e pagamento integral. |

### Contextos e relações

| Contexto / módulo | O que possui | Relação com os demais |
| --- | --- | --- |
| Acesso | Usuário, credenciais e sessão | Publica a identidade do usuário; solicita abertura de Conta ao cadastrar. |
| Contas | Conta, saldo e movimentações | Expõe operações autorizadas de crédito, débito, consulta e extrato. |
| Pagamentos | Transferência e Cobrança | Usa operações de Contas; referencia contas por identificador. |
| Cartões | Cartão, Compra e Fatura | Usa Contas para pagar fatura; referencia conta e usuário por identificador. |

Um **contexto** delimita o significado dos termos e a propriedade dos dados. Um **agregado** delimita as regras que precisam ser mantidas ao alterar um objeto. Módulos usam operações públicas; não acessam diretamente tabelas ou entidades JPA de outros módulos.

### Agregados e invariantes

| Raiz do agregado | Regras protegidas |
| --- | --- |
| Conta | Tem um titular; saldo não pode ficar negativo; alterações de saldo geram movimentações rastreáveis. Movimentações ficam em registros próprios, sem coleção ilimitada na entidade Conta. |
| Transferência | Origem e destino existem e são diferentes; valor positivo; registra a identidade e o resultado de uma operação executada uma única vez. |
| Cobrança | Tem valor positivo e estado; depois de paga não pode ser paga novamente. |
| Cartão | Tem titular, limite total e limite utilizado; compra não pode exceder o disponível. |
| Fatura | Pertence a um cartão e a uma competência única; compras ficam em registros próprios; fatura fechada só pode ser quitada integralmente uma vez. |

### Casos de uso e consistência

| Caso de uso | Mudanças na mesma transação local |
| --- | --- |
| Cadastrar | Cria Usuário e sua Conta; falha em qualquer parte desfaz ambas. |
| Adicionar saldo de demonstração | Credita a Conta e cria Movimentação identificada como demonstração. |
| Transferir (Pix interno) | Debita origem, credita destino, grava duas Movimentações e uma Transferência pelo mesmo valor. |
| Pagar cobrança fictícia | Debita Conta, grava Movimentação e marca Cobrança como paga. |
| Comprar com cartão | Ocupa limite e registra Compra na Fatura aberta; não debita Conta na compra. |
| Pagar fatura | Debita Conta, grava Movimentação, quita Fatura e libera somente o limite das compras daquela fatura. |

O Pix interno conserva o valor entre as duas contas. Cobranças e faturas representam **saídas para liquidação fictícia**, sem crédito para outro usuário do app. Não há rede Pix, boleto ou cartão real.

Cada comando que altera dinheiro ou limite recebe **chave de idempotência**. Repetir chave e dados devolve o resultado já registrado; repetir chave com dados diferentes retorna conflito. Restrição única no banco e controle de concorrência impedem execução duplicada em solicitações simultâneas. O usuário opera apenas recursos próprios; a outra parte de um Pix vê sua própria movimentação.

### Ciclo mensal do cartão

- A competência é o mês civil em `America/Sao_Paulo`. Compras entram na fatura aberta daquele mês.
- A fatura fecha ao fim do **último dia do mês** e vence no **dia 10 do mês seguinte**. O fechamento é idempotente e é atualizado antes de consultar ou alterar uma fatura após a virada, inclusive depois de reiniciar a aplicação.
- Estados persistidos: **aberta → fechada → paga**. Uma fatura fechada e não paga após o vencimento aparece como vencida; esse atraso não bloqueia o pagamento.
- Inicialmente só há compras à vista e quitação integral de fatura fechada. Não há juros, multa, pagamento parcial, parcelamento ou estorno.
- Compras de todas as faturas não pagas ocupam limite. Quitar uma fatura libera apenas seu valor; repetir pagamento não debita a conta outra vez.

## English

### Domain language

| Term | Meaning in this simulator |
| --- | --- |
| User | Fictional person who logs in and owns an account. |
| Account | Holds a BRL balance and identifies its owner. |
| Money | BRL amount with two decimal places; operations require positive amounts. Use `BigDecimal`, never `double`. |
| Entry | Immutable account credit or debit record; entries make up the statement. |
| Transfer | Simulated payment between two registered app accounts, shown as internal Pix in the UI. |
| Bill | Fictional bill created in the app; its identifier is not a valid bank payment line. |
| Card | Fictional virtual card with a total, used, and available limit. |
| Purchase | Single-payment charge that uses card limit and belongs to an invoice. |
| Invoice | A card's purchases for a monthly billing cycle, with closing, due date, and full payment. |

### Contexts and relationships

| Context / module | What it owns | Relationship to other modules |
| --- | --- | --- |
| Access | User, credentials, and session | Exposes user identity; requests Account opening on registration. |
| Accounts | Account, balance, and entries | Exposes authorized credit, debit, balance, and statement operations. |
| Payments | Transfer and Bill | Uses Accounts operations; references accounts by identifier. |
| Cards | Card, Purchase, and Invoice | Uses Accounts to pay invoices; references account and user by identifier. |

A **context** defines what terms mean and who owns the data. An **aggregate** defines the rules that must hold when an object changes. Modules use public operations; they never directly access another module's tables or JPA entities.

### Aggregates and invariants

| Aggregate root | Protected rules |
| --- | --- |
| Account | Has an owner; balance cannot be negative; balance changes create traceable entries. Entries live in separate records, without an unbounded collection inside Account. |
| Transfer | Source and destination exist and differ; amount is positive; records the identity and outcome of an operation executed only once. |
| Bill | Has a positive amount and state; once paid, cannot be paid again. |
| Card | Has an owner, total limit, and used limit; a purchase cannot exceed available limit. |
| Invoice | Belongs to one card and a unique monthly cycle; purchases live in separate records; a closed invoice can only be fully paid once. |

### Use cases and consistency

| Use case | Changes in the same local transaction |
| --- | --- |
| Register | Creates User and Account; a failure in either rolls both back. |
| Add demo funds | Credits Account and creates an Entry marked as demo funding. |
| Transfer (internal Pix) | Debits the source, credits the destination, and records two Entries and one Transfer for the same amount. |
| Pay fictional bill | Debits Account, records an Entry, and marks Bill paid. |
| Make card purchase | Uses limit and records Purchase in the open Invoice; does not debit Account at purchase time. |
| Pay invoice | Debits Account, records an Entry, pays Invoice, and releases only the limit used by purchases on that invoice. |

Internal Pix conserves value between two accounts. Bills and invoices are **fictional outgoing settlements**, with no credit to another app user. There is no real Pix, bill, or card network.

Each command that changes money or limit takes an **idempotency key**. Reusing the same key and data returns the recorded result; using that key with different data returns a conflict. A database uniqueness constraint and concurrency control prevent duplicates from simultaneous requests. Users operate only their own resources; the other party of an internal Pix sees their own entry.

### Monthly card cycle

- A cycle is a calendar month in `America/Sao_Paulo`. Purchases enter that month's open invoice.
- The invoice closes at the end of the **last day of the month** and is due on the **10th of the following month**. Closing is idempotent and is applied before viewing or changing an invoice after the month turns, including after a restart.
- Persisted states: **open → closed → paid**. An unpaid closed invoice is displayed as overdue after its due date; being overdue does not prevent payment.
- This version supports only single-payment purchases and full payment of a closed invoice. There is no interest, late fee, partial payment, installment purchase, or refund.
- Purchases on all unpaid invoices use card limit. Paying an invoice releases only its amount; retrying payment cannot debit the account again.
