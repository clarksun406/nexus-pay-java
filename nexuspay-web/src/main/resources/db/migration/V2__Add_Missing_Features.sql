-- Disputes
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_intent_id UUID NOT NULL REFERENCES payment_intents(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    provider_dispute_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reason VARCHAR(30),
    evidence TEXT,
    evidence_submitted_at TIMESTAMP WITH TIME ZONE,
    due_by TIMESTAMP WITH TIME ZONE,
    amount BIGINT,
    currency VARCHAR(10),
    provider_response TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_disputes_merchant_id ON disputes(merchant_id);
CREATE INDEX idx_disputes_provider_dispute_id ON disputes(provider_dispute_id);

-- Payouts
CREATE TABLE payouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    connector_id UUID REFERENCES provider_accounts(id),
    currency VARCHAR(10) NOT NULL,
    mode VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    fee_amount BIGINT,
    net_amount BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    items_count INTEGER,
    provider_payout_id VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    breakdown TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    settled_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payouts_merchant_id ON payouts(merchant_id);
CREATE INDEX idx_payouts_idempotency_key ON payouts(idempotency_key);

-- Outbox Events
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outbox_events_status ON outbox_events(status);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);

-- Gateway Logs
CREATE TABLE gateway_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID,
    trace_id VARCHAR(64),
    method VARCHAR(10) NOT NULL,
    path VARCHAR(500) NOT NULL,
    status_code INTEGER NOT NULL,
    request_body TEXT,
    response_body TEXT,
    duration_ms BIGINT NOT NULL,
    error VARCHAR(100),
    user_id UUID,
    api_key_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gateway_logs_merchant_id ON gateway_logs(merchant_id);
CREATE INDEX idx_gateway_logs_trace_id ON gateway_logs(trace_id);
CREATE INDEX idx_gateway_logs_created_at ON gateway_logs(created_at DESC);

-- Processed Webhook Events (idempotency)
CREATE TABLE processed_webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(30) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_event_id)
);
