# NexusPay 安全设计文档

## 安全架构

```
┌─────────────────────────────────────────────────────┐
│                    前端层                           │
│  Publishable Key (pk_xxx) + HTTPS                  │
└───────────────────┬─────────────────────────────────┘
                    │ TLS 1.3
                    ▼
┌─────────────────────────────────────────────────────┐
│                    API 网关                         │
│  Rate Limiting + WAF + DDoS 防护                   │
└───────────────────┬─────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────┐
│                    应用层                           │
│  JWT 认证 + RBAC 权限 + 输入验证                    │
└───────────────────┬─────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────┐
│                    数据层                           │
│  敏感数据加密 + SQL 注入防护                         │
└─────────────────────────────────────────────────────┘
```

---

## 认证机制

### JWT 认证

| 项目 | 说明 |
|------|------|
| 算法 | HS256 |
| 密钥长度 | 256 bits+ |
| Access Token 有效期 | 1 小时 |
| Refresh Token 有效期 | 7 天 |

**JWT Claims：**
```json
{
  "sub": "user_id",
  "merchantId": "xxx",
  "role": "MERCHANT_ADMIN",
  "iat": 1234567890,
  "exp": 1234571490
}
```

### API Key 认证

| 类型 | 前缀 | 用途 |
|------|------|------|
| Secret Key | sk_ | 后端 API 调用 |
| Publishable Key | pk_ | 前端 Element SDK |

---

## 数据安全

### 敏感数据加密

| 数据 | 加密方式 |
|------|----------|
| Provider API Key | AES-256-GCM（计划） |
| 用户密码 | BCrypt (cost=12) |
| MFA Secret | AES-256-GCM |

### PCI DSS 合规

- 不存储完整卡号 (PAN)
- 不存储 CVV/CVC
- 使用 Tokenization
- iframe 隔离敏感输入

---

## 防护措施

### Rate Limiting

| 端点 | 限制 |
|------|------|
| /auth/* | 10 次/分钟/IP |
| /pub/* | 100 次/分钟/IP |
| /api/* | 1000 次/分钟/Key |

### 输入验证

- Bean Validation 注解
- SQL 参数化查询
- XSS 过滤

### 安全响应头

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
Strict-Transport-Security: max-age=31536000
```

---

## RBAC 权限

### 商户角色

| 角色 | 权限 |
|------|------|
| OWNER | 全部权限 |
| ADMIN | 商户管理、成员管理 |
| DEVELOPER | 支付、Webhook |
| FINANCE | 退款、结算 |
| VIEWER | 只读 |

### 运营角色

| 角色 | 权限 |
|------|------|
| ORG_OWNER | 全部权限 |
| ORG_ADMIN | 商户审核、监控 |
| ORG_MEMBER | 只读 |

---

## 审计日志

记录内容：
- 登录/登出
- API 调用（trace_id）
- 敏感操作（创建/删除 API Key）
- 权限变更

---

## 安全清单

| 项目 | 状态 |
|------|------|
| HTTPS | ✅ |
| JWT 认证 | ✅ |
| API Key 认证 | ✅ |
| Rate Limiting | ✅ |
| RBAC | ⚠️ 需增强 |
| AES 加密 | ❌ 待实现 |
| 密码重置 | ❌ 待实现 |
| 审计日志 | ⚠️ 基础 |
| 渗透测试 | ❌ 待执行 |
