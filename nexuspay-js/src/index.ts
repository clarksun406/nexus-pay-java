/**
 * NexusPay Elements SDK
 * 
 * A secure, embeddable payment UI component library.
 * Similar to Stripe Elements, supports card payments and APMs.
 * 
 * @example
 * ```typescript
 * const nexuspay = Nexuspay('pk_test_xxx');
 * const elements = nexuspay.elements();
 * const card = elements.create('card');
 * card.mount('#card-element');
 * const { token } = await nexuspay.createToken(card);
 * ```
 */

export { Nexuspay } from './core/Nexuspay';
export { Elements } from './core/Elements';
export { Element } from './core/Element';
export { CardElement } from './elements/CardElement';
export { PaymentElement } from './elements/PaymentElement';
export { SetupElement } from './elements/SetupElement';
export { ApplePayElement } from './elements/ApplePayElement';
export { GooglePayElement } from './elements/GooglePayElement';
export { AlipayElement } from './elements/AlipayElement';
export { WeChatPayElement } from './elements/WeChatPayElement';

export type {
  NexuspayOptions,
  Appearance,
  ElementsOptions,
  CardElementOptions,
  PaymentElementOptions,
  SetupElementOptions,
  ApplePayElementOptions,
  GooglePayElementOptions,
  AlipayElementOptions,
  WeChatPayElementOptions,
  PaymentMethodType,
  TokenResult,
  SetupResult,
  PaymentResult,
  NexuspayError,
  ElementChangeEvent,
  ConfirmPaymentOptions,
  ConfirmSetupOptions,
} from './types';

// Default export for UMD
export default Nexuspay;
