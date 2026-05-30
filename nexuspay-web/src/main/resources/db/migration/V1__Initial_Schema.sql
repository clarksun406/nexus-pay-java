-- Enable pgcrypto for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(64),
    token_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- Organizations
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_organizations_status ON organizations(status);

-- Merchants
CREATE TABLE merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchants_organization_id ON merchants(organization_id);

-- Organization Users
CREATE TABLE organization_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    role VARCHAR(20) NOT NULL DEFAULT 'ORG_MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, organization_id)
);

CREATE INDEX idx_organization_users_user_id ON organization_users(user_id);
CREATE INDEX idx_organization_users_organization_id ON organization_users(organization_id);

-- Merchant Users
CREATE TABLE merchant_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    role VARCHAR(20) NOT NULL,
    invited_by UUID REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_INVITE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, merchant_id)
);

CREATE INDEX idx_merchant_users_user_id ON merchant_users(user_id);
CREATE INDEX idx_merchant_users_merchant_id ON merchant_users(merchant_id);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- API Keys
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    mode VARCHAR(10) NOT NULL,
    type VARCHAR(15) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    plaintext_key VARCHAR(255),
    prefix VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    last_used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_api_keys_merchant_id ON api_keys(merchant_id);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);

-- Provider Accounts (Connectors)
CREATE TABLE provider_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    provider VARCHAR(30) NOT NULL,
    mode VARCHAR(10) NOT NULL DEFAULT 'TEST',
    label VARCHAR(100) NOT NULL,
    encrypted_secret_key TEXT,
    encrypted_publishable_key TEXT,
    encrypted_credentials TEXT,
    provider_config TEXT,
    secret_key_hint VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    weight INTEGER NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    fee_config TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    connector_account_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_provider_accounts_merchant_id ON provider_accounts(merchant_id);
CREATE INDEX idx_provider_accounts_merchant_provider ON provider_accounts(merchant_id, provider);

-- Routing Rules
CREATE TABLE routing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    currencies TEXT,
    amount_min BIGINT,
    amount_max BIGINT,
    country_codes TEXT,
    payment_method_types TEXT,
    target_provider VARCHAR(20) NOT NULL,
    target_account_id UUID REFERENCES provider_accounts(id),
    fallback_provider VARCHAR(20),
    fallback_account_id UUID REFERENCES provider_accounts(id),
    weight INTEGER NOT NULL DEFAULT 1,
    max_cost_bps INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routing_rules_merchant_id ON routing_rules(merchant_id);

-- Payment Intents
CREATE TABLE payment_intents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    mode VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUIRES_PAYMENT_METHOD',
    capture_method VARCHAR(20) NOT NULL DEFAULT 'AUTOMATIC',
    idempotency_key VARCHAR(255) NOT NULL,
    resolved_provider VARCHAR(20),
    connector_account_id UUID REFERENCES provider_accounts(id),
    provider_payment_id VARCHAR(255),
    provider_response TEXT,
    payment_method_type VARCHAR(50),
    metadata TEXT,
    order_id VARCHAR(255),
    description TEXT,
    billing_details JSONB,
    shipping_details JSONB,
    success_url VARCHAR(500),
    cancel_url VARCHAR(500),
    failure_url VARCHAR(500),
    three_ds_action_url VARCHAR(1000),
    trace_id VARCHAR(64),
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(merchant_id, idempotency_key)
);

CREATE INDEX idx_payment_intents_merchant_id ON payment_intents(merchant_id);
CREATE INDEX idx_payment_intents_status ON payment_intents(status);

-- Payment Requests
CREATE TABLE payment_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_intent_id UUID NOT NULL REFERENCES payment_intents(id) ON DELETE CASCADE,
    connector_account_id UUID REFERENCES provider_accounts(id),
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_request_id VARCHAR(255),
    provider_response TEXT,
    failure_code VARCHAR(100),
    failure_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_requests_payment_intent_id ON payment_requests(payment_intent_id);

-- Refunds
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_intent_id UUID NOT NULL REFERENCES payment_intents(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    mode VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(30),
    provider_refund_id VARCHAR(255),
    failure_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refunds_payment_intent_id ON refunds(payment_intent_id);
CREATE INDEX idx_refunds_merchant_id ON refunds(merchant_id);

-- Payment Links
CREATE TABLE payment_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    token VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    mode VARCHAR(10) NOT NULL DEFAULT 'TEST',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    redirect_url VARCHAR(500),
    pinned_connector_id UUID REFERENCES provider_accounts(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payment_links_merchant_mode ON payment_links(merchant_id, mode);
CREATE INDEX idx_payment_links_token ON payment_links(token);

-- Webhook Endpoints
CREATE TABLE webhook_endpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    url VARCHAR(500) NOT NULL,
    events TEXT,
    signing_secret VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_triggered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_endpoints_merchant_id ON webhook_endpoints(merchant_id);

-- Webhook Deliveries
CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id UUID NOT NULL REFERENCES webhook_endpoints(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_deliveries_endpoint_id ON webhook_deliveries(endpoint_id);
