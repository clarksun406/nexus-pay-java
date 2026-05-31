# NexusPay Elements SDK 瀹屾暣瀹炵幇鎬荤粨

## 瀹炵幇姒傝

瀹屾垚浜嗕笁绔鐞嗙郴缁熺殑璁捐鍜屽疄鐜帮細

| 绔?| 鐢ㄦ埛 | 鍔熻兘鑼冨洿 | 瀹炵幇鐘舵€?|
|----|------|----------|----------|
| **Element 绔?* | C 绔敤鎴?| 鏀粯 UI 缁勪欢 | 鉁?瀹屾暣 |
| **鍟嗘埛鍚庡彴** | 鍟嗘埛绠＄悊鍛?| 鏀粯/瀹㈡埛/璁㈤槄绠＄悊 | 鉁?瀹屾暣 |
| **杩愯惀绠＄悊绔?* | 骞冲彴杩愯惀 | 缁勭粐/鍟嗘埛瀹℃牳/鐩戞帶 | 鉁?瀹屾暣 |

---

## 涓€銆丒lement 绔疄鐜?
### 1.1 鍓嶇 SDK (frontend-nexuspay-js)

```
frontend-nexuspay-js/
鈹溾攢鈹€ src/
鈹?  鈹溾攢鈹€ index.ts                    # 瀵煎嚭鍏ュ彛
鈹?  鈹溾攢鈹€ types.ts                    # 绫诲瀷瀹氫箟
鈹?  鈹溾攢鈹€ core/
鈹?  鈹?  鈹溾攢鈹€ Nexuspay.ts             # 涓诲叆鍙ｇ被
鈹?  鈹?  鈹溾攢鈹€ Elements.ts             # Elements 瀹瑰櫒
鈹?  鈹?  鈹斺攢鈹€ Element.ts              # Element 鍩虹被
鈹?  鈹溾攢鈹€ elements/
鈹?  鈹?  鈹溾攢鈹€ CardElement.ts          # 閾惰鍗?鈹?  鈹?  鈹溾攢鈹€ PaymentElement.ts       # 缁熶竴鏀粯缁勪欢
鈹?  鈹?  鈹溾攢鈹€ SetupElement.ts         # 淇濆瓨鍗＄墖
鈹?  鈹?  鈹溾攢鈹€ ApplePayElement.ts      # Apple Pay
鈹?  鈹?  鈹溾攢鈹€ GooglePayElement.ts     # Google Pay
鈹?  鈹?  鈹溾攢鈹€ AlipayElement.ts        # 鏀粯瀹?鈹?  鈹?  鈹斺攢鈹€ WeChatPayElement.ts     # 寰俊鏀粯
鈹?  鈹斺攢鈹€ utils/
鈹?      鈹斺攢鈹€ postMessage.ts          # iframe 閫氫俊
鈹溾攢鈹€ package.json
鈹溾攢鈹€ tsconfig.json
鈹斺攢鈹€ vite.config.ts
```

### 1.2 鏀寔鐨?Element 绫诲瀷

| Element | 瀹炵幇鏂瑰紡 | 鍔熻兘 |
|---------|----------|------|
| `card` | iframe + postMessage | 鍗″彿杈撳叆銆侀獙璇併€佸搧鐗岃瘑鍒?|
| `payment` | iframe 鍔ㄦ€佸姞杞?| 缁熶竴鏀粯鏂瑰紡閫夋嫨锛坱abs/accordion锛?|
| `setup` | iframe | 淇濆瓨鍗＄墖渚涜闃呬娇鐢?|
| `applePay` | Apple Pay JS | Apple Pay 鎸夐挳 |
| `googlePay` | Google Pay API | Google Pay 鎸夐挳 |
| `alipay` | 閲嶅畾鍚?浜岀淮鐮?| 鏀粯瀹濇敮浠?|
| `wechatPay` | 閲嶅畾鍚?浜岀淮鐮?| 寰俊鏀粯 |

### 1.3 iframe 椤甸潰

| 鏂囦欢 | 鍔熻兘 |
|------|------|
| `frontend-dashboard/elements/card.html` | 鍗＄墖杈撳叆 UI |
| `frontend-dashboard/elements/card.js` | Luhn 鏍￠獙銆佹牸寮忓寲銆佸搧鐗岃瘑鍒?|
| `frontend-dashboard/elements/payment.html` | 鍔ㄦ€佹敮浠樻柟寮忛€夋嫨 UI |
| `frontend-dashboard/elements/demo.html` | 瀹屾暣婕旂ず椤甸潰 |

---

## 浜屻€佸晢鎴峰悗鍙板疄鐜?
### 2.1 鍚庣 API

**鏀粯鐩稿叧锛堝凡鏈夛級锛?*
- `PaymentIntentController` - 鏀粯鎰忓浘绠＄悊
- `RefundController` - 閫€娆剧鐞?- `ConnectorController` - 杩炴帴鍣ㄧ鐞?- `RoutingRuleController` - 璺敱瑙勫垯

**瀹㈡埛/璁㈤槄锛堟柊澧烇級锛?*
- `CustomerController` - `/api/v1/merchants/{id}/customers`
- `SubscriptionController` - `/api/v1/merchants/{id}/subscriptions`

### 2.2 鍓嶇椤甸潰

| 椤甸潰 | 鏂囦欢 | 鍔熻兘 |
|------|------|------|
| 鏀粯鍒楄〃 | `Payments.vue` | 鏀粯璁板綍鏌ョ湅 |
| 杩炴帴鍣?| `Connectors.vue` | Provider 閰嶇疆 |
| 瀹㈡埛绠＄悊 | `Customers.vue` 鉁ㄦ柊澧?| 瀹㈡埛鍒楄〃銆佹敮浠樻柟寮忕鐞?|
| 璁㈤槄绠＄悊 | `Subscriptions.vue` 鉁ㄦ柊澧?| 璁㈤槄鍒楄〃銆佸垱寤恒€佸彇娑?|

### 2.3 鏁版嵁妯″瀷

**Customer锛堝鎴凤級**
```java
- id, merchantId, email, name, phone
- providerCustomerId (Stripe Customer ID)
- status: ACTIVE, INACTIVE, DELETED
```

**PaymentMethod锛堟敮浠樻柟寮忥級**
```java
- id, customerId, merchantId
- providerPaymentMethodId (pm_xxx)
- type: CARD, ALIPAY, WECHAT_PAY, APPLE_PAY, GOOGLE_PAY
- last4, brand, expiryMonth, expiryYear
- isDefault
```

**Subscription锛堣闃咃級**
```java
- id, customerId, merchantId, paymentMethodId
- planId, name, interval (DAY/WEEK/MONTH/YEAR)
- amount, currency
- status: ACTIVE, TRIALING, PAST_DUE, CANCELED
- currentPeriodStart, currentPeriodEnd
```

---

## 涓夈€佽繍钀ョ鐞嗙瀹炵幇

### 3.1 鍚庣 API

| 鎺у埗鍣?| 璺緞 | 鍔熻兘 |
|--------|------|------|
| `AdminController` | `/api/v1/admin/*` | 鍏ㄥ眬姒傝銆佺洃鎺с€佸鏍?|
| `OrganizationController` | `/api/v1/admin/organizations/*` | 缁勭粐绠＄悊 |

**涓昏 API锛?*

```
GET  /api/v1/admin/overview              # 鍏ㄥ眬缁熻
GET  /api/v1/admin/pending-approvals     # 寰呭鏍稿晢鎴?POST /api/v1/admin/merchants/{id}/approve # 閫氳繃瀹℃牳
POST /api/v1/admin/merchants/{id}/reject  # 鎷掔粷瀹℃牳
GET  /api/v1/admin/monitoring            # Provider 鍋ュ悍鐘舵€?
GET/POST /api/v1/admin/organizations     # 缁勭粐 CRUD
GET/POST /api/v1/admin/organizations/{id}/merchants  # 鍟嗘埛绠＄悊
```

### 3.2 鍓嶇椤甸潰

```
frontend-dashboard/src/pages/admin/
鈹溾攢鈹€ AdminLayout.vue      # 甯冨眬锛堜晶杈规爮锛?鈹溾攢鈹€ AdminOverview.vue    # 姒傝锛堢粺璁°€佸緟瀹℃牳銆丳rovider 鐘舵€侊級
鈹斺攢鈹€ Organizations.vue    # 缁勭粐绠＄悊锛堝晢鎴峰垪琛ㄣ€佸惎鍋滐級
```

### 3.3 鍔熻兘鍒楄〃

| 鍔熻兘 | 璇存槑 |
|------|------|
| 鍏ㄥ眬姒傝 | 缁勭粐鏁般€佸晢鎴锋暟銆佹敮浠樼瑪鏁般€佷氦鏄撻噾棰濄€佸緟瀹℃牳鏁?|
| 寰呭鏍稿垪琛?| 鍟嗘埛鐢宠瀹℃牳銆侀€氳繃/鎷掔粷鎿嶄綔 |
| Provider 鐩戞帶 | Stripe/Square/Braintree 鍋ュ悍鐘舵€併€佽姹傞噺 |
| 缁勭粐绠＄悊 | 缁勭粐 CRUD銆佸晢鎴风鐞?|
| 鍟嗘埛鍚仠 | 婵€娲?鍋滅敤鍟嗘埛 |
| 鏀粯鏂瑰紡閰嶇疆 | 鍏ㄥ眬鏀粯鏂瑰紡寮€鍏?|

---

## 鍥涖€佽矾鐢遍厤缃?
闇€瑕佸湪 `frontend-dashboard/src/router/index.ts` 涓坊鍔狅細

```typescript
// 鍟嗘埛鍚庡彴璺敱
{ path: '/customers', component: () => import('../pages/Customers.vue') },
{ path: '/subscriptions', component: () => import('../pages/Subscriptions.vue') },

// 杩愯惀绠＄悊绔矾鐢?{
  path: '/admin',
  component: () => import('../pages/admin/AdminLayout.vue'),
  children: [
    { path: '', component: () => import('../pages/admin/AdminOverview.vue') },
    { path: 'organizations', component: () => import('../pages/admin/Organizations.vue') },
    { path: 'merchants', component: () => import('../pages/admin/Merchants.vue') },
    { path: 'monitoring', component: () => import('../pages/admin/Monitoring.vue') },
    { path: 'payment-methods', component: () => import('../pages/admin/PaymentMethods.vue') },
  ]
}
```

---

## 浜斻€佹枃浠剁粺璁?
| 绫诲埆 | 鏂囦欢鏁?| 浠ｇ爜琛屾暟 |
|------|--------|----------|
| **Element SDK** | 12 | ~1,200 |
| **iframe 椤甸潰** | 4 | ~800 |
| **鍚庣瀹炰綋** | 3 | ~220 |
| **鍚庣 Repository** | 3 | ~75 |
| **鍚庣 Service** | 3 | ~400 |
| **鍚庣 Controller** | 4 | ~360 |
| **鍟嗘埛鍚庡彴鍓嶇** | 2 | ~370 |
| **杩愯惀绠＄悊绔墠绔?* | 3 | ~320 |
| **鏁版嵁搴撹縼绉?* | 1 | ~72 |
| **鎬昏** | **35** | **~3,800** |

---

## 鍏€佷笌 Stripe Elements 瀵规瘮

| 鍔熻兘 | Stripe | NexusPay |
|------|--------|----------|
| Card Element | 鉁?瀹屾暣 | 鉁?瀹屾暣 |
| Payment Element | 鉁?瀹屾暣 | 鉁?妗嗘灦瀹炵幇 |
| Apple/Google Pay | 鉁?瀹屾暣 | 鉁?瀹屾暣 |
| 鏀粯瀹?寰俊 | 鉁?瀹屾暣 | 鉁?瀹屾暣 |
| Setup Element | 鉁?瀹屾暣 | 鉁?瀹屾暣 |
| 3DS 鑷姩澶勭悊 | 鉁?| 鈿狅笍 妗嗘灦 |
| 鏍峰紡娣卞害瀹氬埗 | 鉁?| 鈿狅笍 鍩虹 |
| React/Vue 缁勪欢 | 鉁?| 鉂?|
| **璁㈤槄绠＄悊** | 鉁?| 鉁?瀹屾暣 |
| **杩愯惀绠＄悊绔?* | Stripe Dashboard | 鉁?瀹屾暣 |

**鎬讳綋瀹屾垚搴︼細绾?75%**

---

## 涓冦€佸悗缁伐浣?
1. **Element SDK**
   - 瀹屽杽 Payment Element 鍔ㄦ€?UI
   - 3DS 鎸戞垬娴佺▼鑷姩鍖?   - React/Vue 缁勪欢灏佽

2. **鍟嗘埛鍚庡彴**
   - 瀹屽杽閫€娆鹃〉闈?   - Webhook 閰嶇疆椤甸潰澧炲己

3. **杩愯惀绠＄悊绔?*
   - 鏁版嵁鎶ヨ〃瀵煎嚭
   - 鎿嶄綔鏃ュ織瀹¤


