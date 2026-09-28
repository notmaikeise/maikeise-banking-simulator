# ADR-002 — Pix interno / Internal Pix transfers

**Data / Date:** 2026-09-27  
**Estado / Status:** implementado; testes H2 e PostgreSQL aprovados no JDK 21 com Docker em 27/09/2026 / implemented; H2 and PostgreSQL tests passed on JDK 21 with Docker on 27 September 2026

## Português

### Contexto

O Pix desta etapa move saldo apenas entre contas cadastradas no simulador. A operação precisa conservar o valor, criar movimentações para os dois titulares e devolver o mesmo resultado quando uma requisição é repetida. Contas e Pagamentos são módulos separados no mesmo processo e banco.

### Decisão

- O cliente informa o `destinationAccountId`; a conta de origem vem da sessão autenticada. A chave `Idempotency-Key` identifica uma transferência por titular.
- `payments` possui a Transferência, sua chave e o resultado persistido. Ele usa a operação pública `TransferFunds` de `accounts`; nenhum adaptador de Pagamentos acessa entidades JPA de Contas.
- O caso de uso obtém bloqueios pessimistas nas duas contas em ordem crescente de ID. Após os bloqueios, consulta a chave mais uma vez para resolver pedidos concorrentes. Chave igual com dados iguais devolve o resultado; chave igual com destino ou valor diferente causa conflito.
- Em uma transação local, Contas atualiza os dois saldos e acrescenta `PIX_SENT` e `PIX_RECEIVED`. Pagamentos registra a Transferência. A migração Flyway `V2` cria `internal_transfers` com restrição única por titular/chave e acrescenta `reference_id` ao extrato. Falha em qualquer passo desfaz a operação inteira.

### Consequências

Pix em sentidos opostos obtém os bloqueios na mesma ordem, evitando um ciclo de espera entre as duas contas. O destino precisa ser cadastrado e informado por ID; não há consulta de terceiros por email nem integração Pix real. Testes com H2 exercitam o fluxo HTTP; Testcontainers com PostgreSQL verifica concorrência e migração no banco escolhido.

## English

### Context

This stage's Pix moves funds only between accounts registered in the simulator. It must conserve the amount, create an entry for each owner, and return the same outcome for a retried request. Accounts and Payments are separate modules in the same process and database.

### Decision

- Clients provide `destinationAccountId`; the sender account comes from the authenticated session. `Idempotency-Key` identifies a transfer per owner.
- `payments` owns the Transfer, its key, and its persisted outcome. It calls Accounts' public `TransferFunds` operation; no Payments adapter reads Accounts' JPA entities.
- The use case acquires pessimistic locks on both accounts in ascending ID order, then checks the key again to resolve concurrent requests. Reusing a key with the same data returns the result; changing the destination or amount returns a conflict.
- In one local transaction, Accounts updates both balances and appends `PIX_SENT` and `PIX_RECEIVED`. Payments records the Transfer. Flyway migration `V2` adds `internal_transfers` with a per-owner/key unique constraint and adds `reference_id` to entries. A failure rolls back the entire operation.

### Consequences

Transfers in opposite directions lock the accounts in the same order, avoiding a wait cycle between those rows. Recipients must be registered and specified by account ID; there is no email-based user lookup or real Pix integration. H2 tests exercise the HTTP flow; PostgreSQL Testcontainers tests exercise concurrency and migration on the chosen database.
