-- Customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    email VARCHAR(255),
    name VARCHAR(255),
    phone VARCHAR(255),
    provider_customer_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_merchant_id ON customers(merchant_id);
CREATE INDEX idx_customers_email ON customers(merchant_id, email);
CREATE INDEX idx_customers_provider_id ON customers(provider_customer_id);

-- Payment Methods table
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    provider_payment_method_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    last4 VARCHAR(10),
    brand VARCHAR(20),
    expiry_month INTEGER,
    expiry_year INTEGER,
    card_holder_name VARCHAR(255),
    fingerprint VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_default BOOLEAN DEFAULT FALSE,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_methods_customer_id ON payment_methods(customer_id);
CREATE INDEX idx_payment_methods_merchant_id ON payment_methods(merchant_id);
CREATE INDEX idx_payment_methods_provider_id ON payment_methods(provider_payment_method_id);

-- Subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    payment_method_id UUID REFERENCES payment_methods(id),
    plan_id VARCHAR(255),
    name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE',
    interval VARCHAR(20) NOT NULL,
    interval_count INTEGER NOT NULL DEFAULT 1,
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    trial_start TIMESTAMP,
    trial_end TIMESTAMP,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    canceled_at TIMESTAMP,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    provider_subscription_id VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_customer_id ON subscriptions(customer_id);
CREATE INDEX idx_subscriptions_merchant_id ON subscriptions(merchant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_period_end ON subscriptions(current_period_end);
CREATE INDEX idx_subscriptions_provider_id ON subscriptions(provider_subscription_id);
