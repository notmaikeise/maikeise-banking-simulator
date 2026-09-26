# Modelo de domínio / Domain model

> **Estado / Status:** proposta para revisão. As escolhas de saldo com movimentações, PostgreSQL, login desde o início e faturas mensais foram confirmadas em 25/09/2026. As datas da fatura abaixo são valores iniciais propostos.

## Português

### Linguagem e limites

Este é um simulador local: todo dinheiro, Pix, boleto e cartão é fictício. Um **Usuário** autenticado possui uma **Conta** em BRL. **Dinheiro** representa um valor em BRL com duas casas decimais; operações exigem valor positivo e não usam `double`. Uma **Movimentação** é o registro imutável de uma entrada ou saída da conta; o extrato é a lista dessas movimentações. O saldo persistido e o extrato mudam juntos na mesma transação.

| Contexto / módulo | O que possui | O que os outros módulos podem usar |
| --- | --- | --- |
| Acesso | Usuário, credenciais e identidade autenticada | Identificador do usuário; nunca senha ou entidade de persistência. |
| Contas | Conta, saldo e movimentações | Operações autorizadas de crédito, débito e consulta por identificador. |
| Pagamentos | Transferência interna e Cobrança fictícia | Usa as operações de Contas; guarda apenas identificadores de contas. |
| Cartões | Cartão, Compra e Fatura | Usa Contas para quitar fatura; guarda apenas identificadores. |

Um módulo não acessa diretamente as tabelas nem as entidades JPA de outro. **Contexto** delimita o significado das palavras e a responsabilidade pelo dado; **agregado** delimita as regras que um objeto protege ao ser alterado.

### Agregados e regras propostas

| Raiz do agregado | Invariantes locais |
| --- | --- |
| **Conta** | Pertence a um usuário; saldo nunca fica negativo; crédito ou débito válido gera movimentação rastreável. O extrato pode ser armazenado em tabela própria, sem uma coleção ilimitada dentro da entidade Conta. |
| **Transferência** | Origem e destino são contas diferentes e existentes; valor positivo; identifica uma operação e seu resultado. A mesma solicitação não transfere duas vezes. |
| **Cobrança** | Tem identificador fictício, valor e estado; uma cobrança paga não pode ser paga novamente. |
| **Cartão** | Pertence a um usuário/conta; compras não ultrapassam o limite disponível; limite utilizado corresponde às compras ainda não quitadas. |
| **Fatura** | Pertence a um cartão e a uma competência mensal; contém registros de compras; não pode ser paga duas vezes; inicialmente só aceita quitação integral. Compras ficam em registros próprios para não crescer indefinidamente dentro do objeto Fatura. |

**Transferir** coordena duas Contas, uma Transferência e duas Movimentações em uma única transação no mesmo banco. **Pagar cobrança** debita Conta, registra Movimentação e marca Cobrança como paga na mesma transação. **Comprar no cartão** reserva limite e registra Compra na Fatura, sem debitar a Conta. **Pagar fatura** debita Conta, registra Movimentação, quita Fatura e libera o limite correspondente na mesma transação. Crédito de demonstração também gera Movimentação claramente identificada. O pagamento de cobrança e fatura representa saída para uma liquidação fictícia, não crédito para outro usuário do app.

Cada comando que altera dinheiro ou limite recebe uma chave de idempotência: repetir a mesma chave com os mesmos dados devolve o resultado existente; reutilizá-la com dados diferentes é erro. Uma restrição única no banco sustenta essa regra mesmo com pedidos simultâneos. O usuário só consulta e opera seus próprios recursos; apenas o destinatário autorizado vê sua parte da transferência.

### Fatura mensal: regra inicial proposta

- A competência é o mês civil em `America/Sao_Paulo`. Uma compra entra na fatura aberta da competência de sua data.
- A fatura fecha ao fim do último dia do mês; vence no dia **10 do mês seguinte**. O fechamento é idempotente e deve ser aplicado antes de consultar ou alterar uma fatura que já passou do fechamento, inclusive após reiniciar a aplicação.
- Uma fatura fechada pode ser paga integralmente, inclusive após vencer. Não há juros, multa, pagamento parcial, parcelamento ou estorno nesta versão.
- Compras de todas as faturas ainda não pagas ocupam limite; quitar uma fatura libera somente o valor quitado nela. Repetir o pagamento não debita a conta outra vez.

**Para revisar:** fechamento no último dia do mês e vencimento no dia 10 são sugestões para concretizar a escolha de ciclos mensais; podem ser alterados antes do cartão ser implementado.

## English

### Language and boundaries

This locally runnable simulator uses fictional money, Pix transfers, bills, and cards. An authenticated **User** owns one BRL **Account**. **Money** holds a BRL amount with two decimal places; operations require positive amounts and never use `double`. An immutable **Entry** records an account credit or debit; the statement lists these entries. The persisted balance and entries change in the same transaction.

The four proposed contexts are **Access** (users and credentials), **Accounts** (account, balance, entries), **Payments** (internal transfers and fictional bills), and **Cards** (card, purchases, monthly invoices). Other contexts refer to identifiers and public operations, not another context's persistence entities or tables. A bounded context owns a vocabulary and data; an aggregate protects local invariants when state changes.

### Proposed aggregates and workflows

**Account** protects ownership and non-negative balance. **Transfer** identifies an internal operation and prevents repeated execution. **Bill** cannot be paid twice. **Card** protects available limit. **Invoice** groups purchases by card and month and initially accepts only full payment. Entries and purchases are stored as separate records rather than unbounded in-memory collections.

An internal transfer updates both accounts, records two entries and the transfer atomically in one database. Paying a bill debits the account and marks the bill paid atomically. A card purchase uses limit and is recorded on an invoice without debiting the account. Paying an invoice debits the account, records an entry, pays the invoice and releases the matching card limit atomically. Demo funding records a clearly labeled entry. Bill and invoice payments are fictional outgoing settlements, not credits to another app user.

Commands that change funds or limit take an idempotency key. The same key and payload return the existing outcome; the same key with different data is rejected. A database uniqueness constraint also protects concurrent requests. Users can view and operate only their own resources.

### Proposed monthly billing defaults

- A billing cycle is a calendar month in `America/Sao_Paulo`; a purchase enters that month's open invoice.
- Closing occurs at the end of the month's last day; the invoice is due on the **10th of the following month**. Closing is idempotent and is applied before reading or changing an invoice after the close date, including after an application restart.
- A closed invoice can be paid in full even after its due date. There are no fees, interest, partial payments, installments, or refunds in this version.
- Purchases on all unpaid invoices use the card limit; full payment releases only that invoice's amount. Retrying payment cannot debit twice.

**To review:** month-end closing and a due date on the 10th are proposed defaults, not user-confirmed dates.
