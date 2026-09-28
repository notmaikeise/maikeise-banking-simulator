ALTER TABLE account_entries ADD COLUMN reference_id UUID;

CREATE TABLE internal_transfers (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES bank_users(id),
    idempotency_key UUID NOT NULL,
    source_account_id UUID NOT NULL REFERENCES accounts(id),
    destination_account_id UUID NOT NULL REFERENCES accounts(id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    source_entry_id UUID NOT NULL UNIQUE REFERENCES account_entries(id),
    destination_entry_id UUID NOT NULL UNIQUE REFERENCES account_entries(id),
    source_balance_after NUMERIC(19, 2) NOT NULL CHECK (source_balance_after >= 0),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT internal_transfers_distinct_accounts CHECK (source_account_id <> destination_account_id),
    CONSTRAINT internal_transfers_distinct_entries CHECK (source_entry_id <> destination_entry_id),
    CONSTRAINT internal_transfers_request_key UNIQUE (owner_id, idempotency_key)
);
