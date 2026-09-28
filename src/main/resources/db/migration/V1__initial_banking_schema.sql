CREATE TABLE bank_users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL UNIQUE REFERENCES bank_users(id),
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0)
);

CREATE TABLE account_entries (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    kind VARCHAR(32) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX account_entries_statement_idx ON account_entries(account_id, occurred_at DESC, id DESC);

CREATE TABLE demo_credit_requests (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES bank_users(id),
    idempotency_key UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    entry_id UUID NOT NULL UNIQUE REFERENCES account_entries(id),
    balance_after NUMERIC(19, 2) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (owner_id, idempotency_key)
);
