# NexusPay Elements SDK 瀹炵幇鎬荤粨

## 椤圭洰姒傝堪

瀹炵幇浜?NexusPay Elements SDK锛屼竴涓被浼?Stripe Elements 鐨勫墠绔敮浠樼粍浠跺簱锛屾敮鎸侀摱琛屽崱鏀粯銆丄PM锛堟浛浠ｆ敮浠樻柟寮忥級鍜岃闃呭姛鑳姐€?
---

## 椤圭洰缁撴瀯

```
frontend-nexuspay-js/                          # 鍓嶇 SDK
鈹溾攢鈹€ src/
鈹?  鈹溾攢鈹€ index.ts                      # 瀵煎嚭鍏ュ彛
鈹?  鈹溾攢鈹€ types.ts                      # 绫诲瀷瀹氫箟
鈹?  鈹溾攢鈹€ core/
鈹?  鈹?  鈹溾攢鈹€ Nexuspay.ts               # 涓诲叆鍙ｇ被
鈹?  鈹?  鈹溾攢鈹€ Elements.ts               # Elements 瀹瑰櫒
鈹?  鈹?  鈹斺攢鈹€ Element.ts                # Element 鍩虹被
鈹?  鈹溾攢鈹€ elements/
鈹?  鈹?  鈹溾攢鈹€ CardElement.ts            # 閾惰鍗＄粍浠?鈹?  鈹?  鈹溾攢鈹€ PaymentElement.ts         # 缁熶竴鏀粯缁勪欢
鈹?  鈹?  鈹溾攢鈹€ SetupElement.ts           # 淇濆瓨鍗＄墖缁勪欢
鈹?  鈹?  鈹溾攢鈹€ ApplePayElement.ts        # Apple Pay
鈹?  鈹?  鈹溾攢鈹€ GooglePayElement.ts       # Google Pay
鈹?  鈹?  鈹溾攢鈹€ AlipayElement.ts          # 鏀粯瀹?鈹?  鈹?  鈹斺攢鈹€ WeChatPayElement.ts       # 寰俊鏀粯
鈹?  鈹斺攢鈹€ utils/
鈹?      鈹斺攢鈹€ postMessage.ts            # iframe 閫氫俊宸ュ叿
鈹溾攢鈹€ package.json
鈹溾攢鈹€ tsconfig.json
鈹斺攢鈹€ vite.config.ts

frontend-dashboard/elements/                    # iframe 椤甸潰
鈹溾攢鈹€ card.html                         # 鍗＄墖杈撳叆 UI
鈹溾攢鈹€ card.js                           # 楠岃瘉閫昏緫
鈹斺攢鈹€ demo.html                         # 婕旂ず椤甸潰

nexuspay-domain/                      # 棰嗗煙瀹炰綋
鈹斺攢鈹€ entity/
    鈹溾攢鈹€ Customer.java                 # 瀹㈡埛瀹炰綋
    鈹溾攢鈹€ PaymentMethod.java            # 鏀粯鏂瑰紡瀹炰綋
    鈹斺攢鈹€ Subscription.java             # 璁㈤槄瀹炰綋

nexuspay-repository/                  # 鏁版嵁璁块棶
鈹斺攢鈹€ repository/
    鈹溾攢鈹€ CustomerRepository.java
    鈹溾攢鈹€ PaymentMethodRepository.java
    鈹斺攢鈹€ SubscriptionRepository.java

nexuspay-service/                     # 涓氬姟鏈嶅姟
鈹斺攢鈹€ service/
    鈹溾攢鈹€ CustomerService.java
    鈹斺攢鈹€ SubscriptionService.java

nexuspay-web/                         # Web 鎺у埗鍣?鈹斺攢鈹€ controller/
    鈹溾攢鈹€ CustomerController.java
    鈹斺攢鈹€ SubscriptionController.java
```

---

## 鍓嶇 SDK 鍔熻兘

### 鏀寔鐨?Element 绫诲瀷

| Element | 璇存槑 | 瀹炵幇鏂瑰紡 |
|---------|------|----------|
| `card` | 閾惰鍗℃敮浠?| iframe + postMessage |
| `payment` | 缁熶竴鏀粯缁勪欢 | iframe 鍔ㄦ€佸姞杞?|
| `setup` | 淇濆瓨鍗＄墖 | iframe |
| `applePay` | Apple Pay | Apple Pay JS |
| `googlePay` | Google Pay | Google Pay API |
| `alipay` | 鏀粯瀹?| 閲嶅畾鍚?浜岀淮鐮?|
| `wechatPay` | 寰俊鏀粯 | 閲嶅畾鍚?浜岀淮鐮?|

### API 浣跨敤绀轰緥

```javascript
// 鍒濆鍖?const nexuspay = Nexuspay('pk_test_xxx', {
  apiBase: 'https://api.nexuspay.com',
  locale: 'zh-CN'
});

// 鏂瑰紡涓€锛欳ard Element
const elements = nexuspay.elements();
const card = elements.create('card', {
  style: { base: { fontSize: '16px' } }
});
card.mount('#card-element');

const { token, error } = await nexuspay.createToken(card);

// 鏂瑰紡浜岋細Payment Element
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

// 鏂瑰紡涓夛細Setup Element锛堜繚瀛樺崱鐗囷級
const setup = elements.create('setup', {
  customer: 'cus_xxx',
  usage: 'off_session'
});
setup.mount('#setup-element');
```

---

## 鍚庣璁㈤槄鍔熻兘

### 鏂板瀹炰綋

**Customer锛堝鎴凤級**
- 瀛樺偍瀹㈡埛淇℃伅锛歟mail, name, phone
- 鍏宠仈鍟嗘埛鍜?Provider Customer ID

**PaymentMethod锛堟敮浠樻柟寮忥級**
- 瀛樺偍淇濆瓨鐨勫崱鐗囷細last4, brand, expiry
- 鏀寔璁剧疆涓洪粯璁ゆ敮浠樻柟寮?
**Subscription锛堣闃咃級**
- 璁㈤槄璁″垝锛歩nterval, amount, currency
- 鐘舵€佺鐞嗭細ACTIVE, TRIALING, CANCELED, PAST_DUE
- 鍛ㄦ湡璁＄畻锛歝urrentPeriodStart, currentPeriodEnd

### API 绔偣

| 绔偣 | 鏂规硶 | 璇存槑 |
|------|------|------|
| `/api/v1/merchants/{id}/customers` | CRUD | 瀹㈡埛绠＄悊 |
| `/api/v1/merchants/{id}/customers/{id}/payment-methods` | CRUD | 鏀粯鏂瑰紡绠＄悊 |
| `/api/v1/merchants/{id}/subscriptions` | CRUD | 璁㈤槄绠＄悊 |
| `/api/v1/merchants/{id}/subscriptions/{id}/activate` | POST | 婵€娲昏闃?|
| `/api/v1/merchants/{id}/subscriptions/{id}/cancel` | POST | 鍙栨秷璁㈤槄 |

---

## 瀹夊叏妯″瀷

1. **iframe 闅旂** - 鏁忔劅鍗″彿杈撳叆鍦?NexusPay 鍩熷悕鐨?iframe 涓?2. **鍏紑瀵嗛挜** - 鍓嶇浣跨敤 `pk_xxx`锛宻ecret key 浠呭悗绔娇鐢?3. **postMessage 閫氫俊** - 璺?iframe 瀹夊叏閫氫俊
4. **Tokenization** - 鍓嶇鑾峰彇 token锛屽悗绔娇鐢?token 瀹屾垚鏀粯
5. **PCI DSS SAQ A** - 鍟嗘埛浠呴渶鏈€浣庣骇鍒悎瑙?
---

## 鏂囦欢缁熻

| 绫诲埆 | 鏂囦欢鏁?| 浠ｇ爜琛屾暟 |
|------|--------|----------|
| 鍓嶇 SDK | 12 | ~1,200 |
| iframe 椤甸潰 | 3 | ~450 |
| 鍚庣瀹炰綋 | 3 | ~220 |
| 鍚庣 Repository | 3 | ~75 |
| 鍚庣 Service | 2 | ~280 |
| 鍚庣 Controller | 2 | ~140 |
| 鏁版嵁搴撹縼绉?| 1 | ~72 |
| **鎬昏** | **26** | **~2,500** |

---

## 鍚庣画宸ヤ綔

1. **SDK 瀹屽杽**
   - 娣诲姞鏇村鏍峰紡瀹氬埗閫夐」
   - 瀹屽杽 Payment Element 鍔ㄦ€佸姞杞介€昏緫
   - 娣诲姞 React/Vue 缁勪欢灏佽

2. **鍚庣澧炲己**
   - 瀹炵幇瀹氭椂鎵ｆ浠诲姟
   - 娣诲姞 Invoice 瀹炰綋
   - Webhook 閫氱煡

3. **娴嬭瘯**
   - 鍗曞厓娴嬭瘯
   - 闆嗘垚娴嬭瘯
   - E2E 娴嬭瘯

4. **閮ㄧ讲**
   - SDK 鍙戝竷鍒?npm
   - iframe 椤甸潰閮ㄧ讲鍒?CDN
   - API 鏂囨。鐢熸垚


