# Payment Element 技术方案调研报告

## 概述

本报告调研了主流支付服务商的 Element/Components 产品实现方案，为 NexusPay Elements 设计提供参考。

调研对象：
- Stripe Elements / Payment Element
- CityPay Elements
- Adyen Components / Drop-in
- Fiserv Payment Elements SDK
- Braintree Hosted Fields

---

## 1. Stripe Elements

### 1.1 产品概述

Stripe Elements 是最成熟的支付 UI 组件库，提供：
- **Card Element**: 卡片输入组件
- **Payment Element**: 统一支付组件（支持 100+ 支付方式）
- **Express Checkout**: 快捷支付（Apple Pay / Google Pay / Link）

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面 (merchant.com)                                         │
│                                                                 │
│  <script src="https://js.stripe.com/v3/"></script>              │
│  const stripe = Stripe('pk_test_xxx');                          │
│  const elements = stripe.elements({ clientSecret });            │
│  const paymentElement = elements.create('payment');             │
│  paymentElement.mount('#payment-element');                      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  iframe (js.stripe.com)                                  │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Card Number │ Expiry │ CVC │ Zip                  │  │   │
│  │  │  (商户页面无法访问 DOM，无法获取明文卡号)           │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                                                          │   │
│  │  Payment Method Selector (卡片/钱包/银行转账...)         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
         │ postMessage / MessageChannel
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  Stripe Backend                                                 │
│                                                                 │
│  POST /v1/payment_intents                                       │
│  POST /v1/setup_intents                                         │
│  POST /v1/payment_methods (tokenization)                        │
│                                                                 │
│  返回: client_secret, payment_method.id                         │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 关键 API

```javascript
// 初始化
const stripe = Stripe('pk_test_xxx');
const elements = stripe.elements({
  clientSecret: 'pi_xxx_secret_xxx',  // PaymentIntent client secret
  appearance: { theme: 'stripe' },
  locale: 'zh-CN'
});

// 创建 Payment Element
const paymentElement = elements.create('payment', {
  layout: 'tabs',  // tabs | accordion | auto
  paymentMethodTypes: ['card', 'alipay', 'wechat_pay']
});
paymentElement.mount('#payment-element');

// 创建 Card Element（传统方式）
const cardElement = elements.create('card', {
  style: {
    base: { fontSize: '16px', color: '#32325d' }
  }
});
cardElement.mount('#card-element');

// 提交支付
const { error, paymentIntent } = await stripe.confirmPayment({
  elements,
  confirmParams: { return_url: 'https://merchant.com/success' }
});

// 或仅获取 token
const { token, error } = await stripe.createToken(cardElement);
```

### 1.4 安全模型

| 安全措施 | 说明 |
|----------|------|
| iframe 隔离 | 敏感输入字段在 Stripe 域名的 iframe 中，商户无法访问 DOM |
| 公开密钥 | 使用 `pk_xxx` 公开密钥初始化，secret key 永不暴露给前端 |
| client_secret | PaymentIntent 的 client_secret 用于一次性授权，有时效性 |
| postMessage 通信 | 跨 iframe 通信使用 MessageChannel，可验证 origin |
| PCI DSS | 商户仅需填写 SAQ A，最低级别的 PCI 合规要求 |

### 1.5 核心特性

- **动态支付方式**: Payment Element 自动根据金额/币种/地区显示可用支付方式
- **内置验证**: 卡号校验、过期日期校验、CVC 校验
- **品牌识别**: 自动识别卡组织（Visa/Mastercard/Amex）
- **3DS 处理**: 自动处理 3D Secure 认证流程
- **样式定制**: 通过 `appearance` 对象自定义样式
- **事件监听**: `onChange`, `onReady`, `onFocus`, `onBlur`

---

## 2. CityPay Elements

### 2.1 产品概述

CityPay Elements 是 CityPay 提供的安全支付组件，设计理念与 Stripe 非常相似：
- Card Element: 完整卡片表单
- Card Fields Element: 分离式字段（PAN/Expiry/CVC/Name）
- Apple Pay: Apple Pay 按钮
- Payment Flow / Verify Flow: 预构建的完整支付流程

### 2.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面                                                        │
│                                                                 │
│  import { CityPayPromise } from '@citypay/sdk';                 │
│                                                                 │
│  const citypay = await CityPayPromise();                        │
│  const elements = await citypay.elements({                      │
│    pubKey: 'XXZZYY',                                            │
│    createServerIntent: async () => {                            │
│      const res = await fetch('/api/payments/intent-session');   │
│      return res.json();  // { paymentIntentId, sessionToken }   │
│    }                                                            │
│  });                                                            │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  iframe (citypay.com)                                    │   │
│  │  Card Number │ Expiry │ CVC                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 关键 API

```javascript
// 创建 Card Element
const card = elements.cardElement({
  element: '#card-form',
  layout: 'stack',  // stack | row | column | row-minimal
  language: 'en',
  theme: 'auto'
});

await card.init();
await card.awaitReady();  // 等待 iframe 握手完成

// 监听状态变化
card.onChange((state) => {
  setCanSubmit(state.complete);
});

// Tokenize
const tokeniseResponse = await card.tokenise();
const token = tokeniseResponse.data.cp_card_token;

// Attach token to PaymentIntent
await card.attach({ intentId, token });

// Confirm and handle 3DS
const confirmResponse = await card.confirm({ intentId });

// Authorise (后端)
// POST /api/payments/authorise { intentId }
```

### 2.4 两种集成模式

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| **Direct Mode** | SDK 直接调用 CityPay API | 快速集成、POC、小型项目 |
| **Middleware Mode** | SDK 调用商户后端，商户后端转发到 CityPay | 生产环境、需要审计、需要控制权 |

```javascript
// Middleware Mode 配置
const elements = await citypay.elements({
  pubKey: 'XXZZYY',
  createServerIntent: async () => {...},
  middleware: {
    attach: '/api/payments/attach',
    confirm: '/api/payments/confirm',
    authorise: '/api/payments/authorise'
  }
});
```

### 2.5 核心特性

- **PaymentIntent 生命周期**: `open → requires_payment_method → requires_customer_confirmation → requires_authorisation → succeeded`
- **自动 3DS 处理**: `autoHandle3DS` 选项自动处理 3D Secure
- **布局选项**: stack, row, row-minimal, row-compact, column, column-compact
- **事件驱动**: `cpe:ready`, `cpe:change`, `cpe:processing:start`, `cpe:error`
- **Apple Pay 支持**: 内置 Apple Pay 集成

---

## 3. Adyen Components

### 3.1 产品概述

Adyen 提供两种前端集成方式：
- **Drop-in**: 完整的支付 UI，包含支付方式选择
- **Components**: 模块化的支付组件，可单独使用

### 3.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面                                                        │
│                                                                 │
│  import { AdyenCheckout, Card } from '@adyen/adyen-web';        │
│                                                                 │
│  const checkout = await AdyenCheckout({                        │
│    environment: 'test',                                         │
│    clientKey: 'pk_xxx',                                         │
│    session: { id: 'session_xxx' }  // 后端创建                  │
│  });                                                            │
│                                                                 │
│  const card = new Card(checkout, cardConfig);                  │
│  card.mount('#card-container');                                 │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  iframe (checkoutshopper-live.adyen.com)                 │   │
│  │  Card Number │ Expiry │ CVC                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 关键 API

```javascript
// v6 API
import { AdyenCheckout, Card } from '@adyen/adyen-web';

const checkout = await AdyenCheckout({
  environment: 'test',
  clientKey: 'pk_test_xxx',
  session: {
    id: 'CS_xxx',
    sessionData: '...'
  },
  onPaymentCompleted: (result, component) => {
    console.log(result);
  },
  onError: (error, component) => {
    console.error(error);
  }
});

// 创建 Card Component
const card = new Card(checkout, {
  showBrands: true,
  showBrandIcon: true,
  billingAddressRequired: false,
  onChange: (state) => console.log(state),
  onBrand: (brand) => console.log(brand)
});
card.mount('#card-container');

// Drop-in（完整 UI）
import { Dropin } from '@adyen/adyen-web';
const dropin = new Dropin(checkout, {
  paymentMethodsConfiguration: {
    card: {
      showBrands: true
    }
  }
});
dropin.mount('#dropin-container');
```

### 3.4 安全模型

Adyen 使用 **JSON Web Encryption (JWE)** 进行卡号加密：

1. 后端创建 Session，返回 `session.id` 和公钥
2. 前端使用公钥加密卡号
3. 加密后的数据发送到 Adyen
4. Adyen 解密并处理

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│  Browser   │────▶│  Merchant  │────▶│   Adyen    │
│            │     │   Backend  │     │            │
│ JWE 加密   │     │ 转发请求   │     │ 解密处理   │
└────────────┘     └────────────┘     └────────────┘
```

### 3.5 核心特性

- **Sessions API**: 后端创建 session，前端只持有 session ID
- **100+ 支付方式**: 卡片、钱包、银行转账、BNPL 等
- **品牌识别**: 自动识别卡组织并显示 Logo
- **BIN 检测**: 返回前 6-8 位用于风险评估
- **分期付款**: 支持信用卡分期配置
- **地址查找**: 集成地址自动完成功能

---

## 4. Fiserv Payment Elements SDK

### 4.1 产品概述

Fiserv (前身 First Data) 提供 Payment Elements SDK，用于安全地收集支付信息。

### 4.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面                                                        │
│                                                                 │
│  <script src="https://sandbox-cdn.pci.getfwd.com/sdk/forward.js">│
│                                                                 │
│  // SDK 自动创建 iframe                                          │
│  // iframe src: https://sandbox-cdn.pci.getfwd.com/...          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  iframe (pci.getfwd.com)                                 │   │
│  │  Credit Card Form                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 安全模型

- iframe 完全托管在 Fiserv 的 PCI 认证环境
- 商户页面无法访问 iframe 内的 DOM
- 通过 `postMessage` 返回 payment token
- 支持样式自定义（CSS 注入）

---

## 5. Braintree Hosted Fields

### 5.1 产品概述

Braintree（PayPal 旗下）提供 Hosted Fields，是最早推出 iframe 支付方案的服务商之一。

### 5.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面                                                        │
│                                                                 │
│  <script src="https://js.braintreegateway.com/web/3.x/js/...">  │
│                                                                 │
│  braintree.client.create({                                      │
│    authorization: 'clientToken'  // 后端生成                    │
│  }, (err, clientInstance) => {...});                           │
│                                                                 │
│  braintree.hostedFields.create({                                │
│    client: clientInstance,                                      │
│    fields: {                                                    │
│      number: { selector: '#card-number' },                     │
│      cvv: { selector: '#cvv' },                                │
│      expirationDate: { selector: '#expiration' }               │
│    }                                                            │
│  }, (err, hostedFieldsInstance) => {...});                     │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  多个 iframe (braintreegateway.com)                      │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐                    │   │
│  │  │ Number  │ │ Expiry  │ │  CVC    │                    │   │
│  │  └─────────┘ └─────────┘ └─────────┘                    │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 关键 API

```javascript
// 后端生成 clientToken
// GET /api/braintree/client-token

// 前端初始化
braintree.client.create({
  authorization: clientToken
}, (err, clientInstance) => {
  
  braintree.hostedFields.create({
    client: clientInstance,
    styles: {
      'input': { 'font-size': '16px' }
    },
    fields: {
      number: { 
        selector: '#card-number',
        placeholder: '4111 1111 1111 1111'
      },
      cvv: { 
        selector: '#cvv',
        placeholder: '123'
      },
      expirationDate: { 
        selector: '#expiration',
        placeholder: 'MM/YY'
      }
    }
  }, (err, hostedFieldsInstance) => {
    
    // 提交
    hostedFieldsInstance.tokenize((err, payload) => {
      // payload.nonce = 'token_xxx'
      // 发送 nonce 到后端
    });
    
  });
});
```

### 5.4 核心特性

- **分离式 iframe**: 每个字段一个 iframe，布局灵活性高
- **nonce 机制**: token 称为 `nonce`，一次性使用
- **样式定制**: 支持注入 CSS 样式
- **事件监听**: `on`, `off`, `getState`
- **PayPal 集成**: 同一 SDK 支持 PayPal 按钮

---

## 6. 架构对比总结

### 6.1 共同设计模式

| 设计要点 | 所有方案的共识 |
|----------|----------------|
| **iframe 隔离** | 敏感字段托管在支付服务商域名，商户无法访问 DOM |
| **公开密钥** | 使用 pk_xxx / clientKey / pubKey，不暴露 secret |
| **postMessage 通信** | 跨 iframe 通信使用 postMessage + MessageChannel |
| **Tokenization** | 前端获取 token/nonce，后端使用 token 完成支付 |
| **Session 机制** | 后端创建 session/intent，前端获取引用 |
| **PCI DSS 降级** | 商户仅需 SAQ A 合规 |

### 6.2 Token 格式对比

| Provider | Token 格式 | 说明 |
|----------|------------|------|
| Stripe | `pm_xxx` (PaymentMethod) | 可复用 |
| Stripe | `tok_xxx` (Token) | 一次性 |
| CityPay | `cp_card_token` | 一次性 |
| Adyen | JWE encrypted data | 加密卡号 |
| Fiserv | `payment_token` | 一次性 |
| Braintree | `nonce` | 一次性 |

### 6.3 iframe 部署方式

| Provider | iframe 域名 | SDK 域名 |
|----------|-------------|----------|
| Stripe | `js.stripe.com` | `js.stripe.com` |
| CityPay | `*.citypay.com` | `cdn.citypay.com` |
| Adyen | `checkoutshopper-live.adyen.com` | `checkoutshopper-live.adyen.com` |
| Fiserv | `pci.getfwd.com` | `cdn.pci.getfwd.com` |
| Braintree | `assets.braintreegateway.com` | `js.braintreegateway.com` |

### 6.4 集成模式对比

| Provider | 模式 | 说明 |
|----------|------|------|
| Stripe | client_secret | PaymentIntent 的 client_secret 传递给前端 |
| CityPay | Direct / Middleware | 可选直连或通过商户后端转发 |
| Adyen | Session | 后端创建 session，前端持有 session ID |
| Fiserv | clientToken | 后端生成 clientToken |
| Braintree | clientToken | 后端生成 clientToken |

---

## 7. NexusPay Elements 设计建议

基于以上调研，NexusPay Elements 应采用以下设计：

### 7.1 架构

```
┌─────────────────────────────────────────────────────────────────┐
│  商户页面                                                        │
│                                                                 │
│  <script src="https://js.nexuspay.com/v3/nexuspay.js">          │
│  const nexuspay = Nexuspay('pk_test_xxx');                      │
│  const elements = nexuspay.elements();                          │
│  const card = elements.create('card');                          │
│  card.mount('#card-element');                                   │
│                                                                 │
│  // 提交                                                         │
│  const { token } = await nexuspay.createToken(card);            │
│  // token = 'gw_tok_xxx'                                        │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  iframe (js.nexuspay.com/elements/card.html)             │   │
│  │  Card Number │ Expiry │ CVC                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
         │ postMessage
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  NexusPay Backend                                               │
│                                                                 │
│  POST /pub/tokenize                                             │
│  - 验证 pk_xxx                                                  │
│  - 调用底层 Provider (Stripe/Square/Braintree)                  │
│  - 返回 gw_tok_xxx                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 后端 API 设计

| 端点 | 方法 | 说明 |
|------|------|------|
| `/pub/tokenize` | POST | 卡号 → gw_tok_xxx |
| `/pub/config` | GET | 返回可用 providers 和配置 |
| `/pub/elements/card.html` | GET | Card Element iframe 页面 |

### 7.3 前端 SDK 设计

```javascript
// API 参考 Stripe / CityPay
const nexuspay = Nexuspay('pk_test_xxx');
const elements = nexuspay.elements();

// Card Element
const card = elements.create('card', {
  style: { base: { fontSize: '16px' } }
});
card.mount('#card-element');

// 或分离式字段
const cardNumber = elements.create('cardNumber');
const cardExpiry = elements.create('cardExpiry');
const cardCvc = elements.create('cardCvc');

// Tokenize
const { token, error } = await nexuspay.createToken(card);
```

### 7.4 项目结构

```
nexuspay-js/              # 独立 npm 包
├── src/
│   ├── core/
│   │   ├── Nexuspay.ts
│   │   ├── Elements.ts
│   │   └── Element.ts
│   ├── elements/
│   │   ├── CardElement.ts
│   │   ├── CardNumberElement.ts
│   │   ├── CardExpiryElement.ts
│   │   └── CardCvcElement.ts
│   ├── iframe/
│   │   └── IframeController.ts
│   └── utils/
│       └── postMessage.ts
├── dist/
│   ├── nexuspay.umd.js
│   └── nexuspay.esm.js
└── package.json

nexus-pay-java/           # 后端（现有工程）
├── nexuspay-web/
│   └── controller/
│       ├── PublicTokenizeController.java  # 已有，增强
│       └── PublicElementController.java   # 新增
└── frontend/
    └── elements/         # iframe 页面
        ├── card.html
        └── card.js
```

---

## 8. 参考资料

- [Stripe Elements Documentation](https://docs.stripe.com/payments/elements)
- [CityPay Elements API](https://docs.citypay.com/elements)
- [Adyen Web Components](https://docs.adyen.com/payment-methods/cards/web-component)
- [Fiserv Payment Elements SDK](https://isvportal.fiserv.com/docs/software-development-kits/payment-elements-sdk)
- [Braintree Hosted Fields](https://developer.paypal.com/braintree/docs/guides/hosted-fields/overview/javascript/v3)
