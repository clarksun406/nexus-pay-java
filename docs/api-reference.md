# NexusPay API 文档

## 基础信息

| 项目 | 说明 |
|------|------|
| Base URL | `https://api.nexuspay.com/api/v1` |
| 认证方式 | JWT Bearer Token / API Key |
| 内容格式 | JSON |
| 字符编码 | UTF-8 |

---

## 认证

### 1. JWT 认证（商户后台）

```
Authorization: Bearer <jwt_token>
```

**获取 Token：**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "rt_...",
  "expiresIn": 3600
}
```

### 2. API Key 认证（支付 API）

```
Authorization: sk_test_xxx
```

**API Key 类型：**
- `sk_xxx` - Secret Key（后端使用）
- `pk_xxx` - Publishable Key（前端使用）

---

## 支付 API

### 创建 PaymentIntent

```http
POST /payment-intents
Authorization: sk_xxx
Content-Type: application/json

{
  "amount": 9900,
  "currency": "cny",
  "captureMethod": "AUTOMATIC",
  "idempotencyKey": "order-12345",
  "metadata": {
    "orderId": "ORD-12345"
  }
}

Response:
{
  "id": "pi_abc123",
  "status": "REQUIRES_PAYMENT_METHOD",
  "amount": 9900,
  "currency": "cny",
  "clientSecret": "pi_abc123_secret_xxx",
  "createdAt": "2026-05-31T12:00:00Z"
}
```

### 确认支付

```http
POST /payment-intents/{id}/confirm
Authorization: sk_xxx
Content-Type: application/json

{
  "paymentMethodId": "gw_tok_xxx",
  "paymentMethodType": "card"
}

Response:
{
  "id": "pi_abc123",
  "status": "SUCCEEDED",
  "providerPaymentId": "ch_xxx",
  "provider": "STRIPE"
}
```

### 查询 PaymentIntent

```http
GET /payment-intents/{id}
Authorization: Bearer <jwt>
```

### 取消支付

```http
POST /payment-intents/{id}/cancel
Authorization: Bearer <jwt>
```

---

## 退款 API

### 创建退款

```http
POST /refunds
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "paymentIntentId": "pi_abc123",
  "amount": 5000,
  "reason": "requested_by_customer"
}
```

---

## 客户 API

### 创建客户

```http
POST /merchants/{merchantId}/customers
Authorization: Bearer <jwt>

{
  "email": "customer@example.com",
  "name": "张三",
  "phone": "13800138000"
}
```

### 添加支付方式

```http
POST /merchants/{merchantId}/customers/{customerId}/payment-methods
Authorization: Bearer <jwt>

{
  "providerPaymentMethodId": "pm_xxx",
  "type": "CARD",
  "last4": "4242",
  "brand": "VISA",
  "expiryMonth": 12,
  "expiryYear": 2026
}
```

---

## 订阅 API

### 创建订阅

```http
POST /merchants/{merchantId}/subscriptions
Authorization: Bearer <jwt>

{
  "customerId": "cus_xxx",
  "paymentMethodId": "pm_xxx",
  "planId": "pro-monthly",
  "name": "Pro 订阅",
  "amount": 9900,
  "currency": "cny",
  "interval": "MONTH",
  "trialDays": 7
}
```

### 激活订阅

```http
POST /merchants/{merchantId}/subscriptions/{id}/activate
Authorization: Bearer <jwt>
```

### 取消订阅

```http
POST /merchants/{merchantId}/subscriptions/{id}/cancel?immediately=true
Authorization: Bearer <jwt>
```

---

## 连接器 API

### 创建连接器

```http
POST /merchants/{merchantId}/connectors
Authorization: Bearer <jwt>

{
  "provider": "STRIPE",
  "name": "主 Stripe 账户",
  "credentials": {
    "apiKey": "sk_test_xxx"
  },
  "feeConfig": {
    "fixed": 30,
    "percentage": 2.9
  }
}
```

---

## 路由规则 API

### 创建路由规则

```http
POST /merchants/{merchantId}/routing-rules
Authorization: Bearer <jwt>

{
  "name": "大额路由",
  "priority": 1,
  "criteria": {
    "minAmount": 100000,
    "currency": "cny"
  },
  "connectorIds": ["conn_stripe_1"],
  "strategy": "WEIGHTED"
}
```

---

## Webhook API

### 创建 Webhook 端点

```http
POST /merchants/{merchantId}/webhook-endpoints
Authorization: Bearer <jwt>

{
  "url": "https://example.com/webhooks",
  "events": ["payment.succeeded", "payment.failed"],
  "secret": "whsec_xxx"
}
```

---

## 运营管理 API

### 登录

```http
POST /admin/auth/login

{
  "email": "admin@nexuspay.com",
  "password": "password123"
}
```

### 全局概览

```http
GET /admin/overview
Authorization: Bearer <admin_jwt>
```

### 待审核商户

```http
GET /admin/pending-approvals
Authorization: Bearer <admin_jwt>
```

### 审核商户

```http
POST /admin/merchants/{id}/approve
Authorization: Bearer <admin_jwt>

POST /admin/merchants/{id}/reject
{
  "reason": "资料不完整"
}
```

---

## Public API（Element SDK 使用）

### Tokenize

```http
POST /pub/elements/tokenize
Authorization: pk_xxx

{
  "card": {
    "number": "4242424242424242",
    "expMonth": 12,
    "expYear": 2026,
    "cvc": "123"
  }
}

Response:
{
  "token": "gw_tok_xxx",
  "brand": "VISA",
  "last4": "4242"
}
```

### 获取 Provider 配置

```http
GET /pub/elements/config
Authorization: pk_xxx

Response:
{
  "providers": ["STRIPE", "SQUARE", "BRAINTREE"],
  "publishableKey": "pk_test_xxx"
}
```

---

## 错误响应

```json
{
  "error": {
    "type": "validation_error",
    "code": "INVALID_CARD_NUMBER",
    "message": "卡号无效",
    "param": "card.number"
  }
}
```

**错误类型：**
- `validation_error` - 参数验证失败
- `authentication_error` - 认证失败
- `api_error` - 服务端错误
- `card_error` - 卡片错误

---

## HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求过于频繁 |
| 500 | 服务端错误 |

---

## 分页

```http
GET /payment-intents?page=0&size=20&sort=createdAt,desc

Response:
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Webhook 事件

| 事件 | 说明 |
|------|------|
| `payment_intent.created` | PaymentIntent 创建 |
| `payment_intent.succeeded` | 支付成功 |
| `payment_intent.failed` | 支付失败 |
| `payment_intent.canceled` | 支付取消 |
| `refund.created` | 退款创建 |
| `subscription.created` | 订阅创建 |
| `subscription.updated` | 订阅更新 |
| `subscription.canceled` | 订阅取消 |

**Webhook 签名：**
```
X-NexusPay-Signature: sha256=<hmac_sha256_signature>
```
