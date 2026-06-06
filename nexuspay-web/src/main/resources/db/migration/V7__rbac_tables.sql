-- ============================================================
-- V7: Role-Based Access Control tables and seed data
-- ============================================================

-- Permissions catalog
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    scope VARCHAR(20) NOT NULL DEFAULT 'MERCHANT',  -- MERCHANT | ORGANIZATION | SYSTEM
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_permissions_scope ON permissions(scope);

-- Roles
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    scope VARCHAR(20) NOT NULL DEFAULT 'MERCHANT',
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roles_scope ON roles(scope);

-- Role-to-Permission mapping
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- User-to-Role grants (scoped)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    scope_type VARCHAR(20) NOT NULL,  -- MERCHANT | ORGANIZATION | SYSTEM
    scope_id UUID,
    granted_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, role_id, scope_type, scope_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_scope ON user_roles(scope_type, scope_id);

-- ============================================================
-- Seed: Merchant Permissions
-- ============================================================
INSERT INTO permissions (code, name, scope, description) VALUES
('MERCHANT_READ', 'Read Merchant', 'MERCHANT', 'View merchant details'),
('MERCHANT_WRITE', 'Write Merchant', 'MERCHANT', 'Update merchant settings'),
('PAYMENT_READ', 'Read Payments', 'MERCHANT', 'View payment intents'),
('PAYMENT_CREATE', 'Create Payments', 'MERCHANT', 'Create payment intents'),
('PAYMENT_CAPTURE', 'Capture Payments', 'MERCHANT', 'Capture manual payments'),
('PAYMENT_CANCEL', 'Cancel Payments', 'MERCHANT', 'Cancel payments'),
('PAYMENT_REFUND', 'Refund Payments', 'MERCHANT', 'Create refunds'),
('CONNECTOR_MANAGE', 'Manage Connectors', 'MERCHANT', 'Configure provider connectors'),
('ROUTING_RULE_MANAGE', 'Manage Routing Rules', 'MERCHANT', 'Configure routing rules'),
('API_KEY_MANAGE', 'Manage API Keys', 'MERCHANT', 'Manage API keys'),
('WEBHOOK_MANAGE', 'Manage Webhooks', 'MERCHANT', 'Manage webhook endpoints'),
('MEMBER_MANAGE', 'Manage Members', 'MERCHANT', 'Manage team members'),
('CUSTOMER_MANAGE', 'Manage Customers', 'MERCHANT', 'Manage customers'),
('SUBSCRIPTION_MANAGE', 'Manage Subscriptions', 'MERCHANT', 'Manage subscriptions'),
('DISPUTE_READ', 'Read Disputes', 'MERCHANT', 'View disputes'),
('DISPUTE_MANAGE', 'Manage Disputes', 'MERCHANT', 'Submit dispute evidence'),
('PAYOUT_READ', 'Read Payouts', 'MERCHANT', 'View payouts'),
('REPORT_READ', 'Read Reports', 'MERCHANT', 'View merchant reports');

-- ============================================================
-- Seed: Platform/Admin Permissions
-- ============================================================
INSERT INTO permissions (code, name, scope, description) VALUES
('ORG_READ', 'Read Organization', 'ORGANIZATION', 'View organization details'),
('ORG_MANAGE', 'Manage Organization', 'ORGANIZATION', 'Manage organization settings'),
('MERCHANT_APPROVE', 'Approve Merchant', 'ORGANIZATION', 'Approve new merchants'),
('MERCHANT_SUSPEND', 'Suspend Merchant', 'ORGANIZATION', 'Suspend or reinstate merchants'),
('SYSTEM_MONITOR', 'Monitor System', 'SYSTEM', 'View system health and metrics'),
('SYSTEM_CONFIG', 'Configure System', 'SYSTEM', 'Manage system configuration'),
('AUDIT_LOG_READ', 'Read Audit Logs', 'SYSTEM', 'View audit logs'),
('REPORT_EXPORT', 'Export Reports', 'ORGANIZATION', 'Export reports');

-- ============================================================
-- Seed: Roles
-- ============================================================
INSERT INTO roles (code, name, scope, description) VALUES
('MERCHANT_OWNER', 'Merchant Owner', 'MERCHANT', 'Full merchant access'),
('MERCHANT_ADMIN', 'Merchant Admin', 'MERCHANT', 'Merchant configuration and operations'),
('MERCHANT_DEVELOPER', 'Merchant Developer', 'MERCHANT', 'Development and test operations'),
('MERCHANT_FINANCE', 'Merchant Finance', 'MERCHANT', 'Financial operations and reporting'),
('MERCHANT_VIEWER', 'Merchant Viewer', 'MERCHANT', 'Read-only merchant access'),
('ORG_OWNER', 'Organization Owner', 'ORGANIZATION', 'Full organization access'),
('ORG_ADMIN', 'Organization Admin', 'ORGANIZATION', 'Organization management'),
('ORG_MEMBER', 'Organization Member', 'ORGANIZATION', 'Read-only organization access'),
('SYSTEM_ADMIN', 'System Admin', 'SYSTEM', 'Full system administration');

-- ============================================================
-- Seed: Role Permissions - MERCHANT_OWNER (all merchant perms)
-- ============================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'MERCHANT_OWNER' AND p.scope = 'MERCHANT';

-- MERCHANT_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'MERCHANT_ADMIN'
  AND p.code IN ('MERCHANT_READ', 'MERCHANT_WRITE', 'PAYMENT_READ', 'PAYMENT_CREATE',
                 'PAYMENT_CAPTURE', 'PAYMENT_CANCEL', 'PAYMENT_REFUND',
                 'CONNECTOR_MANAGE', 'ROUTING_RULE_MANAGE', 'WEBHOOK_MANAGE',
                 'CUSTOMER_MANAGE', 'SUBSCRIPTION_MANAGE', 'DISPUTE_READ', 'DISPUTE_MANAGE',
                 'PAYOUT_READ', 'REPORT_READ');

-- MERCHANT_DEVELOPER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'MERCHANT_DEVELOPER'
  AND p.code IN ('PAYMENT_READ', 'PAYMENT_CREATE', 'API_KEY_MANAGE', 'WEBHOOK_MANAGE',
                 'MERCHANT_READ', 'CONNECTOR_MANAGE');

-- MERCHANT_FINANCE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'MERCHANT_FINANCE'
  AND p.code IN ('PAYMENT_READ', 'PAYMENT_REFUND', 'DISPUTE_READ', 'DISPUTE_MANAGE',
                 'PAYOUT_READ', 'REPORT_READ', 'MERCHANT_READ');

-- MERCHANT_VIEWER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'MERCHANT_VIEWER'
  AND p.code IN ('PAYMENT_READ', 'DISPUTE_READ', 'PAYOUT_READ', 'REPORT_READ', 'MERCHANT_READ');

-- ORG_OWNER (all org + admin perms)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ORG_OWNER' AND p.scope IN ('ORGANIZATION', 'SYSTEM');

-- ORG_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ORG_ADMIN'
  AND p.code IN ('ORG_READ', 'ORG_MANAGE', 'MERCHANT_APPROVE', 'MERCHANT_SUSPEND',
                 'SYSTEM_MONITOR', 'AUDIT_LOG_READ', 'REPORT_EXPORT');

-- ORG_MEMBER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ORG_MEMBER'
  AND p.code IN ('ORG_READ', 'SYSTEM_MONITOR', 'AUDIT_LOG_READ');

-- SYSTEM_ADMIN (all perms)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'SYSTEM_ADMIN';

-- ============================================================
-- Seed: Create a default system admin user for bootstrapping
-- Password: admin123 (bcrypt hash)
-- ============================================================
INSERT INTO users (id, email, password_hash, status, token_version)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@nexuspay.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ACTIVE', 0
) ON CONFLICT (email) DO NOTHING;

-- Grant SYSTEM_ADMIN role to the default admin
INSERT INTO user_roles (user_id, role_id, scope_type, scope_id)
SELECT '00000000-0000-0000-0000-000000000001', r.id, 'SYSTEM', NULL
FROM roles r WHERE r.code = 'SYSTEM_ADMIN'
ON CONFLICT (user_id, role_id, scope_type, scope_id) DO NOTHING;