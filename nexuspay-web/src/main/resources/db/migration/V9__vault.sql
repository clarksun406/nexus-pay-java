-- ============================================================
-- V9: Card Vault - PCI-compliant sensitive data storage
-- ============================================================
CREATE TABLE vault_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    entry_type VARCHAR(20) NOT NULL,  -- CARD, BANK_ACCOUNT, WALLET, GENERIC
    token VARCHAR(255) NOT NULL UNIQUE,  -- Public reference token (non-sensitive)
    token_hash VARCHAR(128) NOT NULL UNIQUE,  -- SHA-256 hash of token for lookup
    encrypted_data TEXT NOT NULL,  -- JWE-style AES-256-GCM encrypted JSON with sensitive fields
    encrypted_data_key TEXT NOT NULL,  -- Wrapped per-entry data key
    key_id VARCHAR(100) NOT NULL,  -- Key version used to wrap the data key
    encryption_algorithm VARCHAR(50) NOT NULL,  -- JWE_A256GCM
    key_model VARCHAR(50) NOT NULL,  -- MASTER_CUSTODIAN
    data_signature VARCHAR(128),  -- HMAC-SHA256 signature for integrity verification
    fingerprint VARCHAR(128),  -- Hash of sensitive data to detect duplicates
    brand VARCHAR(50),
    last4 VARCHAR(4),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, REVOKED, EXPIRED
    metadata JSONB,
    last_used_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vault_entries_merchant_id ON vault_entries(merchant_id);
CREATE INDEX idx_vault_entries_customer_id ON vault_entries(customer_id);
CREATE INDEX idx_vault_entries_token ON vault_entries(token);
CREATE INDEX idx_vault_entries_token_hash ON vault_entries(token_hash);
CREATE INDEX idx_vault_entries_fingerprint ON vault_entries(fingerprint);
CREATE INDEX idx_vault_entries_merchant_fingerprint ON vault_entries(merchant_id, fingerprint);
CREATE INDEX idx_vault_entries_type_status ON vault_entries(entry_type, status);

-- ============================================================
-- Vault audit log for compliance
-- ============================================================
CREATE TABLE vault_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_entry_id UUID REFERENCES vault_entries(id) ON DELETE SET NULL,
    merchant_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,  -- CREATE, READ, UPDATE, DELETE, TOKENIZE, DETOKENIZE
    actor_id UUID,  -- User who performed the action
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vault_audit_logs_vault_entry ON vault_audit_logs(vault_entry_id);
CREATE INDEX idx_vault_audit_logs_merchant ON vault_audit_logs(merchant_id);
CREATE INDEX idx_vault_audit_logs_created ON vault_audit_logs(created_at);
