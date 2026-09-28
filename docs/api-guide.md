# Guia da API / API guide

## Português

Execute os passos com um cliente HTTP que **guarde e reenvie o cookie `JSESSIONID`**. Use a mesma sessão em todos os pedidos. A API roda em `http://localhost:8080` depois dos comandos do README. Ainda não há tela de cadastro: a página `/login` é gerada pelo Spring Security.

1. Faça `GET /api/csrf`. Guarde o valor `token` e o nome `headerName` da resposta. Envie esse token no cabeçalho indicado em toda requisição `POST`.
2. Faça `POST /api/users` com `Content-Type: application/json` e o cabeçalho CSRF. Corpo: `{"name":"Ana Teste","email":"ana@example.test","password":"senhaDeTeste123"}`. O resultado é `201` com `userId`; a conta é criada com saldo zero.
3. Faça `POST /login` com formulário `application/x-www-form-urlencoded`: `username=ana@example.test&password=senhaDeTeste123`. Envie o token CSRF no cabeçalho. A resposta `302` indica login correto; mantenha o cookie recebido.
4. Faça **outro** `GET /api/csrf` após o login, pois o token anterior deixa de valer quando a autenticação muda.
5. Faça `GET /api/accounts/me` com o cookie da sessão para consultar o saldo.
6. Faça `POST /api/accounts/me/demo-credits` com cookie, cabeçalho CSRF, `Idempotency-Key: 8d83fc3c-7ba2-4e92-b1fa-881e899c42b1`, `Content-Type: application/json` e corpo `{"amount":25.00}`. O resultado inclui o identificador da movimentação e o saldo após o crédito. Repetir a mesma chave e valor retorna o mesmo resultado, sem crédito adicional. Repetir a chave com outro valor retorna `409`.
7. Faça `GET /api/accounts/me/entries?page=0&size=20` com o cookie para ver as movimentações da sua conta. `size` aceita de 1 a 100.
8. Em **uma segunda sessão**, cadastre e autentique outra pessoa (repita os passos 1 a 4). Consulte `GET /api/accounts/me` nessa sessão e copie o `accountId` da pessoa destinatária.
9. De volta à sessão de Ana, faça `POST /api/transfers` com cookie, token CSRF, `Idempotency-Key: 4ce614b7-d99f-46fb-a746-f7660e52e27d`, `Content-Type: application/json` e corpo `{"destinationAccountId":"<accountId da pessoa destinatária>","amount":10.00}`. Substitua o texto entre `<...>` pelo UUID real. A resposta `200` traz `transferId`, os IDs das duas movimentações, `amount`, `balanceAfter` da conta de origem e `occurredAt`.
10. Repetir a mesma chave com o mesmo destino e valor devolve exatamente a transferência anterior. Trocar o destino ou o valor com a mesma chave retorna `409`. Saldo insuficiente também retorna `409`; conta inexistente retorna `404` e transferência para a própria conta retorna `400`.
11. Consulte o extrato das duas sessões: a origem recebe `PIX_SENT` e o destino `PIX_RECEIVED`, ambos com `referenceId` igual ao `transferId`. Créditos de demonstração têm `referenceId` nulo.

O identificador de idempotência deve ser um **UUID novo para cada crédito ou Pix novo**; preserve o mesmo UUID ao repetir uma requisição. Uma chave pode aparecer em tipos diferentes de operação, pois a unicidade vale por usuário e tipo. A conta de origem vem da sessão autenticada; o cliente informa apenas a conta de destino. Para sair, use `POST /logout` com cookie e token CSRF. O banco e as credenciais do Compose servem apenas ao desenvolvimento local.

## English

Use an HTTP client that **stores and resends the `JSESSIONID` cookie**. Keep the same session for all requests. The API runs at `http://localhost:8080` after the README setup. There is no registration UI yet; Spring Security generates the `/login` page.

1. Send `GET /api/csrf`. Save the response's `token` and `headerName`; send the token in that header for every `POST` request.
2. Send `POST /api/users` with `Content-Type: application/json` and the CSRF header. Body: `{"name":"Ana Test","email":"ana@example.test","password":"testPassword123"}`. A `201` response includes `userId`; the new account has a zero balance.
3. Send `POST /login` as `application/x-www-form-urlencoded`: `username=ana@example.test&password=testPassword123`. Include the CSRF header. A `302` response indicates a successful login; keep the returned cookie.
4. Send **another** `GET /api/csrf` after logging in because authentication changes invalidate the earlier token.
5. Send `GET /api/accounts/me` with the session cookie to read the balance.
6. Send `POST /api/accounts/me/demo-credits` with the cookie, CSRF header, `Idempotency-Key: 8d83fc3c-7ba2-4e92-b1fa-881e899c42b1`, `Content-Type: application/json`, and body `{"amount":25.00}`. The response contains the entry ID and balance after the credit. Retrying with the same key and amount returns the same result without adding funds twice; a different amount with the same key returns `409`.
7. Send `GET /api/accounts/me/entries?page=0&size=20` with the cookie to see your account entries. `size` must be from 1 to 100.
8. In **a separate session**, register and sign in a second user (repeat steps 1–4). Request `GET /api/accounts/me` in that session and copy the recipient's `accountId`.
9. Back in Ana's session, send `POST /api/transfers` with the cookie, CSRF token, `Idempotency-Key: 4ce614b7-d99f-46fb-a746-f7660e52e27d`, `Content-Type: application/json`, and body `{"destinationAccountId":"<recipient's accountId>","amount":10.00}`. Replace the placeholder with the real UUID. A `200` response includes `transferId`, both entry IDs, `amount`, the sender's `balanceAfter`, and `occurredAt`.
10. Retrying with the same key, destination, and amount returns the same transfer. Changing the amount or destination for the same key returns `409`. Insufficient funds also returns `409`; an unknown destination returns `404`, and transferring to your own account returns `400`.
11. Read both statements: the sender sees `PIX_SENT` and the recipient sees `PIX_RECEIVED`, both with `referenceId` equal to `transferId`. Demo funding has a null `referenceId`.

Use a **new UUID for each new credit or Pix transfer**; keep the same UUID for retries. A key can be reused across different operation types because uniqueness is per user and operation type. The sender account comes from the authenticated session; clients provide only the destination account. To sign out, send `POST /logout` with the cookie and CSRF token. The Compose database and credentials are intended only for local development.
