# NexusPay 数据库设计文档

## ER 概览

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│ Organization│────<│ Merchant     │────<│ MerchantUser│
└─────────────┘     └──────┬───────┘     └──────┬──────┘
                           │                     │
                           │     ┌───────────────┘
                           │     │
                    ┌──────┴─────┴──────┐
                    │                   │
              ┌─────┴─────┐       ┌─────┴─────┐
              │Customer   │       │ApiKey     │
              └─────┬─────┘       └───────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
  ┌─────┴───┐ ┌─────┴───┐ ┌─────┴─────┐
  │Payment  │ │Subscription│ │PaymentMethod│
  │Intent   │ └───────────┘ └───────────┘
  └─────┬───┘
        │
  ┌─────┴───┐
  │Refund   │
  └─────────┘
```

---

## 核心表结构

### 组织与商户

| 表 | 说明 |
|----|------|
| `organizations` | 组织 |
| `organization_users` | 组织成员 |
| `merchants` | 商户 |
| `merchant_users` | 商户成员 |

### 用户与认证

| 表 | 说明 |
|----|------|
| `users` | 用户基础信息 |
| `refresh_tokens` | 刷新令牌 |
| `api_keys` | API 密钥 |

### 支付

| 表 | 说明 |
|----|------|
| `payment_intents` | 支付意图 |
| `payment_requests` | 支付请求记录 |
| `refunds` | 退款 |
| `disputes` | 争议 |
| `payouts` | 结算 |

### 连接器与路由

| 表 | 说明 |
|----|------|
| `provider_accounts` | Provider 账户 |
| `routing_rules` | 路由规则 |

### 客户与订阅

| 表 | 说明 |
|----|------|
| `customers` | 客户 |
| `payment_methods` | 支付方式 |
| `subscriptions` | 订阅 |

### Webhook

| 表 | 说明 |
|----|------|
| `webhook_endpoints` | Webhook 端点 |
| `outbox_events` | 出站事件 |

### 其他

| 表 | 说明 |
|----|------|
| `payment_links` | 支付链接 |
| `gateway_logs` | 网关日志 |

---

## 表详情

### users

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| email | VARCHAR(255) | 邮箱（唯一） |
| password_hash | VARCHAR(255) | 密码哈希 |
| name | VARCHAR(255) | 姓名 |
| status | VARCHAR(20) | 状态：ACTIVE, INACTIVE |
| mfa_enabled | BOOLEAN | 是否启用 MFA |
| mfa_secret | VARCHAR(255) | MFA 密钥 |
| created_at | TIMESTAMP | 创建时间 |

### merchants

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| organization_id | UUID | 组织 ID |
| name | VARCHAR(255) | 商户名称 |
| status | VARCHAR(20) | 状态：ACTIVE, INACTIVE, SUSPENDED |

### payment_intents

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| merchant_id | UUID | 商户 ID |
| amount | BIGINT | 金额（分） |
| currency | VARCHAR(10) | 币种 |
| status | VARCHAR(30) | 状态 |
| capture_method | VARCHAR(20) | 捕获方式 |
| resolved_provider | VARCHAR(20) | 解析的 Provider |
| provider_payment_id | VARCHAR(255) | Provider 支付 ID |
| idempotency_key | VARCHAR(255) | 幂等键 |
| metadata | JSONB | 元数据 |

### customers

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| merchant_id | UUID | 商户 ID |
| email | VARCHAR(255) | 邮箱 |
| name | VARCHAR(255) | 姓名 |
| provider_customer_id | VARCHAR(255) | Provider 客户 ID |
| status | VARCHAR(20) | 状态 |

### subscriptions

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| customer_id | UUID | 客户 ID |
| payment_method_id | UUID | 支付方式 ID |
| plan_id | VARCHAR(255) | 计划 ID |
| amount | BIGINT | 金额 |
| currency | VARCHAR(10) | 币种 |
| interval | VARCHAR(20) | 周期：DAY, WEEK, MONTH, YEAR |
| status | VARCHAR(20) | 状态 |
| current_period_start | TIMESTAMP | 当前周期开始 |
| current_period_end | TIMESTAMP | 当前周期结束 |

---

## 索引策略

| 表 | 索引 | 类型 |
|----|------|------|
| payment_intents | (merchant_id, idempotency_key) | UNIQUE |
| payment_intents | (merchant_id) | BTREE |
| customers | (merchant_id, email) | BTREE |
| subscriptions | (customer_id) | BTREE |
| subscriptions | (status, current_period_end) | BTREE |
| gateway_logs | (merchant_id, created_at) | BTREE |

---

## 数据迁移

使用 Flyway 管理：

```
db/migration/
├── V1__init_schema.sql
├── V2__add_mfa.sql
├── V3__add_disputes.sql
├── V4__add_payouts.sql
└── V5__create_subscription_tables.sql
```
