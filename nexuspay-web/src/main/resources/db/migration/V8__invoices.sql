-- ============================================================
-- V8: Invoices table
-- ============================================================
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    customer_id UUID REFERENCES customers(id),
    subscription_id UUID REFERENCES subscriptions(id),
    payment_intent_id UUID REFERENCES payment_intents(id),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    tax_amount BIGINT DEFAULT 0,
    total_amount BIGINT NOT NULL,
    description TEXT,
    line_items TEXT,
    billing_reason VARCHAR(50),
    due_date TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_merchant_id ON invoices(merchant_id);
CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_subscription_id ON invoices(subscription_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);