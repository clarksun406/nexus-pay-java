# NexusPay Elements SDK 实现总结

## 项目概述

实现了 NexusPay Elements SDK，一个类似 Stripe Elements 的前端支付组件库，支持银行卡支付、APM（替代支付方式）和订阅功能。

---

## 项目结构

```
nexuspay-js/                          # 前端 SDK
├── src/
│   ├── index.ts                      # 导出入口
│   ├── types.ts                      # 类型定义
│   ├── core/
│   │   ├── Nexuspay.ts               # 主入口类
│   │   ├── Elements.ts               # Elements 容器
│   │   └── Element.ts                # Element 基类
│   ├── elements/
│   │   ├── CardElement.ts            # 银行卡组件
│   │   ├── PaymentElement.ts         # 统一支付组件
│   │   ├── SetupElement.ts           # 保存卡片组件
│   │   ├── ApplePayElement.ts        # Apple Pay
│   │   ├── GooglePayElement.ts       # Google Pay
│   │   ├── AlipayElement.ts          # 支付宝
│   │   └── WeChatPayElement.ts       # 微信支付
│   └── utils/
│       └── postMessage.ts            # iframe 通信工具
├── package.json
├── tsconfig.json
└── vite.config.ts

frontend/elements/                    # iframe 页面
├── card.html                         # 卡片输入 UI
├── card.js                           # 验证逻辑
└── demo.html                         # 演示页面

nexuspay-domain/                      # 领域实体
└── entity/
    ├── Customer.java                 # 客户实体
    ├── PaymentMethod.java            # 支付方式实体
    └── Subscription.java             # 订阅实体

nexuspay-repository/                  # 数据访问
└── repository/
    ├── CustomerRepository.java
    ├── PaymentMethodRepository.java
    └── SubscriptionRepository.java

nexuspay-service/                     # 业务服务
└── service/
    ├── CustomerService.java
    └── SubscriptionService.java

nexuspay-web/                         # Web 控制器
└── controller/
    ├── CustomerController.java
    └── SubscriptionController.java
```

---

## 前端 SDK 功能

### 支持的 Element 类型

| Element | 说明 | 实现方式 |
|---------|------|----------|
| `card` | 银行卡支付 | iframe + postMessage |
| `payment` | 统一支付组件 | iframe 动态加载 |
| `setup` | 保存卡片 | iframe |
| `applePay` | Apple Pay | Apple Pay JS |
| `googlePay` | Google Pay | Google Pay API |
| `alipay` | 支付宝 | 重定向/二维码 |
| `wechatPay` | 微信支付 | 重定向/二维码 |

### API 使用示例

```javascript
// 初始化
const nexuspay = Nexuspay('pk_test_xxx', {
  apiBase: 'https://api.nexuspay.com',
  locale: 'zh-CN'
});

// 方式一：Card Element
const elements = nexuspay.elements();
const card = elements.create('card', {
  style: { base: { fontSize: '16px' } }
});
card.mount('#card-element');

const { token, error } = await nexuspay.createToken(card);

// 方式二：Payment Element
const payment = elements.create('payment', {
  paymentMethodTypes: ['card', 'alipay', 'wechat_pay'],
  amount: 9900,
  currency: 'cny'
});
payment.mount('#payment-element');

const result = await nexuspay.confirmPayment({
  elements,
  confirmParams: { return_url: 'https://merchant.com/success' }
});

// 方式三：Setup Element（保存卡片）
const setup = elements.create('setup', {
  customer: 'cus_xxx',
  usage: 'off_session'
});
setup.mount('#setup-element');
```

---

## 后端订阅功能

### 新增实体

**Customer（客户）**
- 存储客户信息：email, name, phone
- 关联商户和 Provider Customer ID

**PaymentMethod（支付方式）**
- 存储保存的卡片：last4, brand, expiry
- 支持设置为默认支付方式

**Subscription（订阅）**
- 订阅计划：interval, amount, currency
- 状态管理：ACTIVE, TRIALING, CANCELED, PAST_DUE
- 周期计算：currentPeriodStart, currentPeriodEnd

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/merchants/{id}/customers` | CRUD | 客户管理 |
| `/api/v1/merchants/{id}/customers/{id}/payment-methods` | CRUD | 支付方式管理 |
| `/api/v1/merchants/{id}/subscriptions` | CRUD | 订阅管理 |
| `/api/v1/merchants/{id}/subscriptions/{id}/activate` | POST | 激活订阅 |
| `/api/v1/merchants/{id}/subscriptions/{id}/cancel` | POST | 取消订阅 |

---

## 安全模型

1. **iframe 隔离** - 敏感卡号输入在 NexusPay 域名的 iframe 中
2. **公开密钥** - 前端使用 `pk_xxx`，secret key 仅后端使用
3. **postMessage 通信** - 跨 iframe 安全通信
4. **Tokenization** - 前端获取 token，后端使用 token 完成支付
5. **PCI DSS SAQ A** - 商户仅需最低级别合规

---

## 文件统计

| 类别 | 文件数 | 代码行数 |
|------|--------|----------|
| 前端 SDK | 12 | ~1,200 |
| iframe 页面 | 3 | ~450 |
| 后端实体 | 3 | ~220 |
| 后端 Repository | 3 | ~75 |
| 后端 Service | 2 | ~280 |
| 后端 Controller | 2 | ~140 |
| 数据库迁移 | 1 | ~72 |
| **总计** | **26** | **~2,500** |

---

## 后续工作

1. **SDK 完善**
   - 添加更多样式定制选项
   - 完善 Payment Element 动态加载逻辑
   - 添加 React/Vue 组件封装

2. **后端增强**
   - 实现定时扣款任务
   - 添加 Invoice 实体
   - Webhook 通知

3. **测试**
   - 单元测试
   - 集成测试
   - E2E 测试

4. **部署**
   - SDK 发布到 npm
   - iframe 页面部署到 CDN
   - API 文档生成
