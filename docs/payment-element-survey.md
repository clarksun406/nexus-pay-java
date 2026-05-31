# Payment Element 鎶€鏈柟妗堣皟鐮旀姤鍛?
## 姒傝堪

鏈姤鍛婅皟鐮斾簡涓绘祦鏀粯鏈嶅姟鍟嗙殑 Element/Components 浜у搧瀹炵幇鏂规锛屼负 NexusPay Elements 璁捐鎻愪緵鍙傝€冦€?
璋冪爺瀵硅薄锛?- Stripe Elements / Payment Element
- CityPay Elements
- Adyen Components / Drop-in
- Fiserv Payment Elements SDK
- Braintree Hosted Fields

---

## 1. Stripe Elements

### 1.1 浜у搧姒傝堪

Stripe Elements 鏄渶鎴愮啛鐨勬敮浠?UI 缁勪欢搴擄紝鎻愪緵锛?- **Card Element**: 鍗＄墖杈撳叆缁勪欢
- **Payment Element**: 缁熶竴鏀粯缁勪欢锛堟敮鎸?100+ 鏀粯鏂瑰紡锛?- **Express Checkout**: 蹇嵎鏀粯锛圓pple Pay / Google Pay / Link锛?
### 1.2 鏋舵瀯璁捐

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰 (merchant.com)                                         鈹?鈹?                                                                鈹?鈹? <script src="https://js.stripe.com/v3/"></script>              鈹?鈹? const stripe = Stripe('pk_test_xxx');                          鈹?鈹? const elements = stripe.elements({ clientSecret });            鈹?鈹? const paymentElement = elements.create('payment');             鈹?鈹? paymentElement.mount('#payment-element');                      鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? iframe (js.stripe.com)                                  鈹?  鈹?鈹? 鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹?  鈹?鈹? 鈹? 鈹? Card Number 鈹?Expiry 鈹?CVC 鈹?Zip                  鈹? 鈹?  鈹?鈹? 鈹? 鈹? (鍟嗘埛椤甸潰鏃犳硶璁块棶 DOM锛屾棤娉曡幏鍙栨槑鏂囧崱鍙?           鈹? 鈹?  鈹?鈹? 鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹?  鈹?鈹? 鈹?                                                         鈹?  鈹?鈹? 鈹? Payment Method Selector (鍗＄墖/閽卞寘/閾惰杞处...)         鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?         鈹?postMessage / MessageChannel
         鈻?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? Stripe Backend                                                 鈹?鈹?                                                                鈹?鈹? POST /v1/payment_intents                                       鈹?鈹? POST /v1/setup_intents                                         鈹?鈹? POST /v1/payment_methods (tokenization)                        鈹?鈹?                                                                鈹?鈹? 杩斿洖: client_secret, payment_method.id                         鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 1.3 鍏抽敭 API

```javascript
// 鍒濆鍖?const stripe = Stripe('pk_test_xxx');
const elements = stripe.elements({
  clientSecret: 'pi_xxx_secret_xxx',  // PaymentIntent client secret
  appearance: { theme: 'stripe' },
  locale: 'zh-CN'
});

// 鍒涘缓 Payment Element
const paymentElement = elements.create('payment', {
  layout: 'tabs',  // tabs | accordion | auto
  paymentMethodTypes: ['card', 'alipay', 'wechat_pay']
});
paymentElement.mount('#payment-element');

// 鍒涘缓 Card Element锛堜紶缁熸柟寮忥級
const cardElement = elements.create('card', {
  style: {
    base: { fontSize: '16px', color: '#32325d' }
  }
});
cardElement.mount('#card-element');

// 鎻愪氦鏀粯
const { error, paymentIntent } = await stripe.confirmPayment({
  elements,
  confirmParams: { return_url: 'https://merchant.com/success' }
});

// 鎴栦粎鑾峰彇 token
const { token, error } = await stripe.createToken(cardElement);
```

### 1.4 瀹夊叏妯″瀷

| 瀹夊叏鎺柦 | 璇存槑 |
|----------|------|
| iframe 闅旂 | 鏁忔劅杈撳叆瀛楁鍦?Stripe 鍩熷悕鐨?iframe 涓紝鍟嗘埛鏃犳硶璁块棶 DOM |
| 鍏紑瀵嗛挜 | 浣跨敤 `pk_xxx` 鍏紑瀵嗛挜鍒濆鍖栵紝secret key 姘镐笉鏆撮湶缁欏墠绔?|
| client_secret | PaymentIntent 鐨?client_secret 鐢ㄤ簬涓€娆℃€ф巿鏉冿紝鏈夋椂鏁堟€?|
| postMessage 閫氫俊 | 璺?iframe 閫氫俊浣跨敤 MessageChannel锛屽彲楠岃瘉 origin |
| PCI DSS | 鍟嗘埛浠呴渶濉啓 SAQ A锛屾渶浣庣骇鍒殑 PCI 鍚堣瑕佹眰 |

### 1.5 鏍稿績鐗规€?
- **鍔ㄦ€佹敮浠樻柟寮?*: Payment Element 鑷姩鏍规嵁閲戦/甯佺/鍦板尯鏄剧ず鍙敤鏀粯鏂瑰紡
- **鍐呯疆楠岃瘉**: 鍗″彿鏍￠獙銆佽繃鏈熸棩鏈熸牎楠屻€丆VC 鏍￠獙
- **鍝佺墝璇嗗埆**: 鑷姩璇嗗埆鍗＄粍缁囷紙Visa/Mastercard/Amex锛?- **3DS 澶勭悊**: 鑷姩澶勭悊 3D Secure 璁よ瘉娴佺▼
- **鏍峰紡瀹氬埗**: 閫氳繃 `appearance` 瀵硅薄鑷畾涔夋牱寮?- **浜嬩欢鐩戝惉**: `onChange`, `onReady`, `onFocus`, `onBlur`

---

## 2. CityPay Elements

### 2.1 浜у搧姒傝堪

CityPay Elements 鏄?CityPay 鎻愪緵鐨勫畨鍏ㄦ敮浠樼粍浠讹紝璁捐鐞嗗康涓?Stripe 闈炲父鐩镐技锛?- Card Element: 瀹屾暣鍗＄墖琛ㄥ崟
- Card Fields Element: 鍒嗙寮忓瓧娈碉紙PAN/Expiry/CVC/Name锛?- Apple Pay: Apple Pay 鎸夐挳
- Payment Flow / Verify Flow: 棰勬瀯寤虹殑瀹屾暣鏀粯娴佺▼

### 2.2 鏋舵瀯璁捐

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰                                                        鈹?鈹?                                                                鈹?鈹? import { CityPayPromise } from '@citypay/sdk';                 鈹?鈹?                                                                鈹?鈹? const citypay = await CityPayPromise();                        鈹?鈹? const elements = await citypay.elements({                      鈹?鈹?   pubKey: 'XXZZYY',                                            鈹?鈹?   createServerIntent: async () => {                            鈹?鈹?     const res = await fetch('/api/payments/intent-session');   鈹?鈹?     return res.json();  // { paymentIntentId, sessionToken }   鈹?鈹?   }                                                            鈹?鈹? });                                                            鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? iframe (citypay.com)                                    鈹?  鈹?鈹? 鈹? Card Number 鈹?Expiry 鈹?CVC                              鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 2.3 鍏抽敭 API

```javascript
// 鍒涘缓 Card Element
const card = elements.cardElement({
  element: '#card-form',
  layout: 'stack',  // stack | row | column | row-minimal
  language: 'en',
  theme: 'auto'
});

await card.init();
await card.awaitReady();  // 绛夊緟 iframe 鎻℃墜瀹屾垚

// 鐩戝惉鐘舵€佸彉鍖?card.onChange((state) => {
  setCanSubmit(state.complete);
});

// Tokenize
const tokeniseResponse = await card.tokenise();
const token = tokeniseResponse.data.cp_card_token;

// Attach token to PaymentIntent
await card.attach({ intentId, token });

// Confirm and handle 3DS
const confirmResponse = await card.confirm({ intentId });

// Authorise (鍚庣)
// POST /api/payments/authorise { intentId }
```

### 2.4 涓ょ闆嗘垚妯″紡

| 妯″紡 | 璇存槑 | 閫傜敤鍦烘櫙 |
|------|------|----------|
| **Direct Mode** | SDK 鐩存帴璋冪敤 CityPay API | 蹇€熼泦鎴愩€丳OC銆佸皬鍨嬮」鐩?|
| **Middleware Mode** | SDK 璋冪敤鍟嗘埛鍚庣锛屽晢鎴峰悗绔浆鍙戝埌 CityPay | 鐢熶骇鐜銆侀渶瑕佸璁°€侀渶瑕佹帶鍒舵潈 |

```javascript
// Middleware Mode 閰嶇疆
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

### 2.5 鏍稿績鐗规€?
- **PaymentIntent 鐢熷懡鍛ㄦ湡**: `open 鈫?requires_payment_method 鈫?requires_customer_confirmation 鈫?requires_authorisation 鈫?succeeded`
- **鑷姩 3DS 澶勭悊**: `autoHandle3DS` 閫夐」鑷姩澶勭悊 3D Secure
- **甯冨眬閫夐」**: stack, row, row-minimal, row-compact, column, column-compact
- **浜嬩欢椹卞姩**: `cpe:ready`, `cpe:change`, `cpe:processing:start`, `cpe:error`
- **Apple Pay 鏀寔**: 鍐呯疆 Apple Pay 闆嗘垚

---

## 3. Adyen Components

### 3.1 浜у搧姒傝堪

Adyen 鎻愪緵涓ょ鍓嶇闆嗘垚鏂瑰紡锛?- **Drop-in**: 瀹屾暣鐨勬敮浠?UI锛屽寘鍚敮浠樻柟寮忛€夋嫨
- **Components**: 妯″潡鍖栫殑鏀粯缁勪欢锛屽彲鍗曠嫭浣跨敤

### 3.2 鏋舵瀯璁捐

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰                                                        鈹?鈹?                                                                鈹?鈹? import { AdyenCheckout, Card } from '@adyen/adyen-web';        鈹?鈹?                                                                鈹?鈹? const checkout = await AdyenCheckout({                        鈹?鈹?   environment: 'test',                                         鈹?鈹?   clientKey: 'pk_xxx',                                         鈹?鈹?   session: { id: 'session_xxx' }  // 鍚庣鍒涘缓                  鈹?鈹? });                                                            鈹?鈹?                                                                鈹?鈹? const card = new Card(checkout, cardConfig);                  鈹?鈹? card.mount('#card-container');                                 鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? iframe (checkoutshopper-live.adyen.com)                 鈹?  鈹?鈹? 鈹? Card Number 鈹?Expiry 鈹?CVC                              鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 3.3 鍏抽敭 API

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

// 鍒涘缓 Card Component
const card = new Card(checkout, {
  showBrands: true,
  showBrandIcon: true,
  billingAddressRequired: false,
  onChange: (state) => console.log(state),
  onBrand: (brand) => console.log(brand)
});
card.mount('#card-container');

// Drop-in锛堝畬鏁?UI锛?import { Dropin } from '@adyen/adyen-web';
const dropin = new Dropin(checkout, {
  paymentMethodsConfiguration: {
    card: {
      showBrands: true
    }
  }
});
dropin.mount('#dropin-container');
```

### 3.4 瀹夊叏妯″瀷

Adyen 浣跨敤 **JSON Web Encryption (JWE)** 杩涜鍗″彿鍔犲瘑锛?
1. 鍚庣鍒涘缓 Session锛岃繑鍥?`session.id` 鍜屽叕閽?2. 鍓嶇浣跨敤鍏挜鍔犲瘑鍗″彿
3. 鍔犲瘑鍚庣殑鏁版嵁鍙戦€佸埌 Adyen
4. Adyen 瑙ｅ瘑骞跺鐞?
```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?    鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?    鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? Browser   鈹傗攢鈹€鈹€鈹€鈻垛攤  Merchant  鈹傗攢鈹€鈹€鈹€鈻垛攤   Adyen    鈹?鈹?           鈹?    鈹?  Backend  鈹?    鈹?           鈹?鈹?JWE 鍔犲瘑   鈹?    鈹?杞彂璇锋眰   鈹?    鈹?瑙ｅ瘑澶勭悊   鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?    鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?    鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 3.5 鏍稿績鐗规€?
- **Sessions API**: 鍚庣鍒涘缓 session锛屽墠绔彧鎸佹湁 session ID
- **100+ 鏀粯鏂瑰紡**: 鍗＄墖銆侀挶鍖呫€侀摱琛岃浆璐︺€丅NPL 绛?- **鍝佺墝璇嗗埆**: 鑷姩璇嗗埆鍗＄粍缁囧苟鏄剧ず Logo
- **BIN 妫€娴?*: 杩斿洖鍓?6-8 浣嶇敤浜庨闄╄瘎浼?- **鍒嗘湡浠樻**: 鏀寔淇＄敤鍗″垎鏈熼厤缃?- **鍦板潃鏌ユ壘**: 闆嗘垚鍦板潃鑷姩瀹屾垚鍔熻兘

---

## 4. Fiserv Payment Elements SDK

### 4.1 浜у搧姒傝堪

Fiserv (鍓嶈韩 First Data) 鎻愪緵 Payment Elements SDK锛岀敤浜庡畨鍏ㄥ湴鏀堕泦鏀粯淇℃伅銆?
### 4.2 鏋舵瀯璁捐

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰                                                        鈹?鈹?                                                                鈹?鈹? <script src="https://sandbox-cdn.pci.getfwd.com/sdk/forward.js">鈹?鈹?                                                                鈹?鈹? // SDK 鑷姩鍒涘缓 iframe                                          鈹?鈹? // iframe src: https://sandbox-cdn.pci.getfwd.com/...          鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? iframe (pci.getfwd.com)                                 鈹?  鈹?鈹? 鈹? Credit Card Form                                        鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 4.3 瀹夊叏妯″瀷

- iframe 瀹屽叏鎵樼鍦?Fiserv 鐨?PCI 璁よ瘉鐜
- 鍟嗘埛椤甸潰鏃犳硶璁块棶 iframe 鍐呯殑 DOM
- 閫氳繃 `postMessage` 杩斿洖 payment token
- 鏀寔鏍峰紡鑷畾涔夛紙CSS 娉ㄥ叆锛?
---

## 5. Braintree Hosted Fields

### 5.1 浜у搧姒傝堪

Braintree锛圥ayPal 鏃椾笅锛夋彁渚?Hosted Fields锛屾槸鏈€鏃╂帹鍑?iframe 鏀粯鏂规鐨勬湇鍔″晢涔嬩竴銆?
### 5.2 鏋舵瀯璁捐

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰                                                        鈹?鈹?                                                                鈹?鈹? <script src="https://js.braintreegateway.com/web/3.x/js/...">  鈹?鈹?                                                                鈹?鈹? braintree.client.create({                                      鈹?鈹?   authorization: 'clientToken'  // 鍚庣鐢熸垚                    鈹?鈹? }, (err, clientInstance) => {...});                           鈹?鈹?                                                                鈹?鈹? braintree.hostedFields.create({                                鈹?鈹?   client: clientInstance,                                      鈹?鈹?   fields: {                                                    鈹?鈹?     number: { selector: '#card-number' },                     鈹?鈹?     cvv: { selector: '#cvv' },                                鈹?鈹?     expirationDate: { selector: '#expiration' }               鈹?鈹?   }                                                            鈹?鈹? }, (err, hostedFieldsInstance) => {...});                     鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? 澶氫釜 iframe (braintreegateway.com)                      鈹?  鈹?鈹? 鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                   鈹?  鈹?鈹? 鈹? 鈹?Number  鈹?鈹?Expiry  鈹?鈹? CVC    鈹?                   鈹?  鈹?鈹? 鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                   鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 5.3 鍏抽敭 API

```javascript
// 鍚庣鐢熸垚 clientToken
// GET /api/braintree/client-token

// 鍓嶇鍒濆鍖?braintree.client.create({
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
    
    // 鎻愪氦
    hostedFieldsInstance.tokenize((err, payload) => {
      // payload.nonce = 'token_xxx'
      // 鍙戦€?nonce 鍒板悗绔?    });
    
  });
});
```

### 5.4 鏍稿績鐗规€?
- **鍒嗙寮?iframe**: 姣忎釜瀛楁涓€涓?iframe锛屽竷灞€鐏垫椿鎬ч珮
- **nonce 鏈哄埗**: token 绉颁负 `nonce`锛屼竴娆℃€т娇鐢?- **鏍峰紡瀹氬埗**: 鏀寔娉ㄥ叆 CSS 鏍峰紡
- **浜嬩欢鐩戝惉**: `on`, `off`, `getState`
- **PayPal 闆嗘垚**: 鍚屼竴 SDK 鏀寔 PayPal 鎸夐挳

---

## 6. 鏋舵瀯瀵规瘮鎬荤粨

### 6.1 鍏卞悓璁捐妯″紡

| 璁捐瑕佺偣 | 鎵€鏈夋柟妗堢殑鍏辫瘑 |
|----------|----------------|
| **iframe 闅旂** | 鏁忔劅瀛楁鎵樼鍦ㄦ敮浠樻湇鍔″晢鍩熷悕锛屽晢鎴锋棤娉曡闂?DOM |
| **鍏紑瀵嗛挜** | 浣跨敤 pk_xxx / clientKey / pubKey锛屼笉鏆撮湶 secret |
| **postMessage 閫氫俊** | 璺?iframe 閫氫俊浣跨敤 postMessage + MessageChannel |
| **Tokenization** | 鍓嶇鑾峰彇 token/nonce锛屽悗绔娇鐢?token 瀹屾垚鏀粯 |
| **Session 鏈哄埗** | 鍚庣鍒涘缓 session/intent锛屽墠绔幏鍙栧紩鐢?|
| **PCI DSS 闄嶇骇** | 鍟嗘埛浠呴渶 SAQ A 鍚堣 |

### 6.2 Token 鏍煎紡瀵规瘮

| Provider | Token 鏍煎紡 | 璇存槑 |
|----------|------------|------|
| Stripe | `pm_xxx` (PaymentMethod) | 鍙鐢?|
| Stripe | `tok_xxx` (Token) | 涓€娆℃€?|
| CityPay | `cp_card_token` | 涓€娆℃€?|
| Adyen | JWE encrypted data | 鍔犲瘑鍗″彿 |
| Fiserv | `payment_token` | 涓€娆℃€?|
| Braintree | `nonce` | 涓€娆℃€?|

### 6.3 iframe 閮ㄧ讲鏂瑰紡

| Provider | iframe 鍩熷悕 | SDK 鍩熷悕 |
|----------|-------------|----------|
| Stripe | `js.stripe.com` | `js.stripe.com` |
| CityPay | `*.citypay.com` | `cdn.citypay.com` |
| Adyen | `checkoutshopper-live.adyen.com` | `checkoutshopper-live.adyen.com` |
| Fiserv | `pci.getfwd.com` | `cdn.pci.getfwd.com` |
| Braintree | `assets.braintreegateway.com` | `js.braintreegateway.com` |

### 6.4 闆嗘垚妯″紡瀵规瘮

| Provider | 妯″紡 | 璇存槑 |
|----------|------|------|
| Stripe | client_secret | PaymentIntent 鐨?client_secret 浼犻€掔粰鍓嶇 |
| CityPay | Direct / Middleware | 鍙€夌洿杩炴垨閫氳繃鍟嗘埛鍚庣杞彂 |
| Adyen | Session | 鍚庣鍒涘缓 session锛屽墠绔寔鏈?session ID |
| Fiserv | clientToken | 鍚庣鐢熸垚 clientToken |
| Braintree | clientToken | 鍚庣鐢熸垚 clientToken |

---

## 7. NexusPay Elements 璁捐寤鸿

鍩轰簬浠ヤ笂璋冪爺锛孨exusPay Elements 搴旈噰鐢ㄤ互涓嬭璁★細

### 7.1 鏋舵瀯

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? 鍟嗘埛椤甸潰                                                        鈹?鈹?                                                                鈹?鈹? <script src="https://js.nexuspay.com/v3/nexuspay.js">          鈹?鈹? const nexuspay = Nexuspay('pk_test_xxx');                      鈹?鈹? const elements = nexuspay.elements();                          鈹?鈹? const card = elements.create('card');                          鈹?鈹? card.mount('#card-element');                                   鈹?鈹?                                                                鈹?鈹? // 鎻愪氦                                                         鈹?鈹? const { token } = await nexuspay.createToken(card);            鈹?鈹? // token = 'gw_tok_xxx'                                        鈹?鈹?                                                                鈹?鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹? 鈹? iframe (js.nexuspay.com/elements/card.html)             鈹?  鈹?鈹? 鈹? Card Number 鈹?Expiry 鈹?CVC                              鈹?  鈹?鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?         鈹?postMessage
         鈻?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹? NexusPay Backend                                               鈹?鈹?                                                                鈹?鈹? POST /pub/tokenize                                             鈹?鈹? - 楠岃瘉 pk_xxx                                                  鈹?鈹? - 璋冪敤搴曞眰 Provider (Stripe/Square/Braintree)                  鈹?鈹? - 杩斿洖 gw_tok_xxx                                              鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

### 7.2 鍚庣 API 璁捐

| 绔偣 | 鏂规硶 | 璇存槑 |
|------|------|------|
| `/pub/tokenize` | POST | 鍗″彿 鈫?gw_tok_xxx |
| `/pub/config` | GET | 杩斿洖鍙敤 providers 鍜岄厤缃?|
| `/pub/elements/card.html` | GET | Card Element iframe 椤甸潰 |

### 7.3 鍓嶇 SDK 璁捐

```javascript
// API 鍙傝€?Stripe / CityPay
const nexuspay = Nexuspay('pk_test_xxx');
const elements = nexuspay.elements();

// Card Element
const card = elements.create('card', {
  style: { base: { fontSize: '16px' } }
});
card.mount('#card-element');

// 鎴栧垎绂诲紡瀛楁
const cardNumber = elements.create('cardNumber');
const cardExpiry = elements.create('cardExpiry');
const cardCvc = elements.create('cardCvc');

// Tokenize
const { token, error } = await nexuspay.createToken(card);
```

### 7.4 椤圭洰缁撴瀯

```
frontend-nexuspay-js/              # 鐙珛 npm 鍖?鈹溾攢鈹€ src/
鈹?  鈹溾攢鈹€ core/
鈹?  鈹?  鈹溾攢鈹€ Nexuspay.ts
鈹?  鈹?  鈹溾攢鈹€ Elements.ts
鈹?  鈹?  鈹斺攢鈹€ Element.ts
鈹?  鈹溾攢鈹€ elements/
鈹?  鈹?  鈹溾攢鈹€ CardElement.ts
鈹?  鈹?  鈹溾攢鈹€ CardNumberElement.ts
鈹?  鈹?  鈹溾攢鈹€ CardExpiryElement.ts
鈹?  鈹?  鈹斺攢鈹€ CardCvcElement.ts
鈹?  鈹溾攢鈹€ iframe/
鈹?  鈹?  鈹斺攢鈹€ IframeController.ts
鈹?  鈹斺攢鈹€ utils/
鈹?      鈹斺攢鈹€ postMessage.ts
鈹溾攢鈹€ dist/
鈹?  鈹溾攢鈹€ nexuspay.umd.js
鈹?  鈹斺攢鈹€ nexuspay.esm.js
鈹斺攢鈹€ package.json

nexus-pay-java/           # 鍚庣锛堢幇鏈夊伐绋嬶級
鈹溾攢鈹€ nexuspay-web/
鈹?  鈹斺攢鈹€ controller/
鈹?      鈹溾攢鈹€ PublicTokenizeController.java  # 宸叉湁锛屽寮?鈹?      鈹斺攢鈹€ PublicElementController.java   # 鏂板
鈹斺攢鈹€ frontend-dashboard/
    鈹斺攢鈹€ elements/         # iframe 椤甸潰
        鈹溾攢鈹€ card.html
        鈹斺攢鈹€ card.js
```

---

## 8. 鍙傝€冭祫鏂?
- [Stripe Elements Documentation](https://docs.stripe.com/payments/elements)
- [CityPay Elements API](https://docs.citypay.com/elements)
- [Adyen Web Components](https://docs.adyen.com/payment-methods/cards/web-component)
- [Fiserv Payment Elements SDK](https://isvportal.fiserv.com/docs/software-development-kits/payment-elements-sdk)
- [Braintree Hosted Fields](https://developer.paypal.com/braintree/docs/guides/hosted-fields/overview/javascript/v3)


