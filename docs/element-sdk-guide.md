# NexusPay Element SDK 集成指南

## 快速开始

### 1. 安装

```html
<!-- CDN -->
<script src="https://js.nexuspay.com/v3/nexuspay.umd.js"></script>
```

```bash
# NPM
npm install @nexuspay/js

# Yarn
yarn add @nexuspay/js
```

### 2. 初始化

```javascript
import { Nexuspay } from '@nexuspay/js';

const nexuspay = Nexuspay('pk_test_xxx', {
  apiBase: 'https://api.nexuspay.com',
  locale: 'zh-CN'
});
```

---

## Card Element

### 基础用法

```html
<form id="payment-form">
  <div id="card-element"></div>
  <button type="submit">支付</button>
</form>

<script>
const elements = nexuspay.elements();
const card = elements.create('card', {
  style: {
    base: {
      fontSize: '16px',
      color: '#32325d',
      fontFamily: 'system-ui'
    }
  }
});
card.mount('#card-element');

// 监听变化
card.on('change', (event) => {
  if (event.complete) {
    // 卡片信息完整
  }
  if (event.error) {
    console.error(event.error.message);
  }
});

// 提交
document.getElementById('payment-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const { token, error } = await nexuspay.createToken(card);
  
  if (error) {
    console.error(error.message);
    return;
  }
  
  // 发送 token 到后端
  await fetch('/api/payment-intents', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      amount: 9900,
      currency: 'cny',
      paymentMethodId: token
    })
  });
});
</script>
```

### 样式定制

```javascript
const card = elements.create('card', {
  style: {
    base: {
      color: '#32325d',
      fontWeight: '500',
      fontFamily: 'Inter, system-ui',
      fontSize: '16px',
      fontSmoothing: 'antialiased',
      '::placeholder': {
        color: '#a0aec0'
      }
    },
    invalid: {
      color: '#fa755a',
      iconColor: '#fa755a'
    },
    complete: {
      color: '#22c55e'
    }
  },
  hidePostalCode: true
});
```

---

## Payment Element

### 统一支付组件

```html
<div id="payment-element"></div>
<button id="submit-btn">支付 ¥99</button>

<script>
const payment = elements.create('payment', {
  paymentMethodTypes: ['card', 'alipay', 'wechat_pay'],
  amount: 9900,
  currency: 'cny',
  layout: 'tabs'  // 'tabs' | 'accordion' | 'auto'
});
payment.mount('#payment-element');

document.getElementById('submit-btn').addEventListener('click', async () => {
  const result = await nexuspay.confirmPayment({
    elements,
    confirmParams: {
      return_url: window.location.origin + '/success'
    }
  });
  
  if (result.error) {
    console.error(result.error.message);
  }
});
</script>
```

---

## Setup Element（保存卡片）

### 用法

```javascript
const setup = elements.create('setup', {
  customer: 'cus_xxx',
  usage: 'off_session'  // 'on_session' | 'off_session'
});
setup.mount('#setup-element');

document.getElementById('save-btn').addEventListener('click', async () => {
  const { setupIntent, error } = await nexuspay.confirmSetup({
    elements,
    confirmParams: {
      return_url: window.location.origin + '/card-saved'
    }
  });
  
  if (setupIntent) {
    console.log('PaymentMethod saved:', setupIntent.paymentMethod);
  }
});
```

---

## Apple Pay

```javascript
const applePay = elements.create('applePay', {
  total: {
    label: 'Order #12345',
    amount: 9900
  },
  buttonType: 'buy',  // 'plain' | 'buy' | 'donate'
  buttonStyle: 'black'  // 'black' | 'white' | 'white-outline'
});
applePay.mount('#applepay-button');

applePay.on('authorized', (event) => {
  // 支付完成
  console.log('Payment successful');
});
```

---

## Google Pay

```javascript
const googlePay = elements.create('googlePay', {
  total: {
    label: 'Order #12345',
    amount: 9900
  },
  buttonType: 'long',  // 'long' | 'short'
  buttonColor: 'black'  // 'black' | 'white'
});
googlePay.mount('#googlepay-button');
```

---

## 支付宝 / 微信支付

```javascript
// 支付宝
const alipay = elements.create('alipay', {
  amount: 9900,
  currency: 'cny',
  returnUrl: 'https://example.com/success'
});
alipay.mount('#alipay-button');

// 微信支付
const wechatPay = elements.create('wechatPay', {
  amount: 9900,
  currency: 'cny',
  returnUrl: 'https://example.com/success'
});
wechatPay.mount('#wechat-button');
```

---

## 事件监听

```javascript
// ready - 元素加载完成
card.on('ready', () => {
  console.log('Card Element ready');
});

// change - 输入变化
card.on('change', (event) => {
  console.log('Complete:', event.complete);
  console.log('Empty:', event.empty);
  console.log('Brand:', event.brand);  // 'VISA', 'MASTERCARD'
  console.log('Error:', event.error);
});

// focus - 获得焦点
card.on('focus', () => {});

// blur - 失去焦点
card.on('blur', () => {});
```

---

## React 集成

```jsx
import { useEffect, useRef } from 'react';
import { Nexuspay } from '@nexuspay/js';

function PaymentForm() {
  const cardRef = useRef(null);
  const cardElementRef = useRef(null);
  
  useEffect(() => {
    const nexuspay = Nexuspay('pk_test_xxx');
    const elements = nexuspay.elements();
    const card = elements.create('card');
    card.mount(cardRef.current);
    cardElementRef.current = card;
    
    return () => card.destroy();
  }, []);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    const { token, error } = await nexuspay.createToken(cardElementRef.current);
    // ...
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <div ref={cardRef} />
      <button type="submit">支付</button>
    </form>
  );
}
```

---

## Vue 3 集成

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <div ref="cardElement"></div>
    <button type="submit">支付</button>
  </form>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Nexuspay } from '@nexuspay/js';

const cardElement = ref(null);
let card = null;
let nexuspay = null;

onMounted(() => {
  nexuspay = Nexuspay('pk_test_xxx');
  const elements = nexuspay.elements();
  card = elements.create('card');
  card.mount(cardElement.value);
});

onUnmounted(() => {
  card?.destroy();
});

const handleSubmit = async () => {
  const { token, error } = await nexuspay.createToken(card);
  // ...
};
</script>
```

---

## 错误处理

```javascript
const { token, error } = await nexuspay.createToken(card);

if (error) {
  switch (error.type) {
    case 'validation_error':
      // 参数验证失败
      console.log(error.param, error.message);
      break;
    case 'card_error':
      // 卡片错误
      console.log(error.code, error.message);
      break;
    case 'api_error':
      // 服务端错误
      console.error('Payment service error');
      break;
  }
  return;
}
```

---

## 安全最佳实践

1. **只使用 Publishable Key (`pk_xxx`) 在前端**
2. **Secret Key (`sk_xxx`) 仅在后端使用**
3. **验证后端收到的 token**
4. **使用 HTTPS**
5. **实现 idempotency key**

---

## TypeScript 支持

```typescript
import { Nexuspay, CardElement, TokenResult } from '@nexuspay/js';

const nexuspay = Nexuspay('pk_test_xxx');
const elements = nexuspay.elements();

const card: CardElement = elements.create('card', {
  style: { base: { fontSize: '16px' } }
});

const result: TokenResult = await nexuspay.createToken(card);
if (result.token) {
  // result.token: string
}
```
