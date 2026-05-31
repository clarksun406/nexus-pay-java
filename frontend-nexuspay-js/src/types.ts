import type { Elements } from './core/Elements';

// NexusPay SDK Type Definitions

export interface NexuspayOptions {
  locale?: string;
  appearance?: Appearance;
  apiBase?: string;
}

export interface Appearance {
  theme?: 'light' | 'dark' | 'stripe';
  variables?: Record<string, string>;
  rules?: Record<string, Record<string, string>>;
}

export interface ElementsOptions {
  clientSecret?: string;
  appearance?: Appearance;
  locale?: string;
}

export interface CardElementOptions {
  style?: CardStyle;
  hidePostalCode?: boolean;
  iconStyle?: 'default' | 'solid';
  disabled?: boolean;
}

export interface CardStyle {
  base?: StyleVars;
  invalid?: StyleVars;
  complete?: StyleVars;
  empty?: StyleVars;
}

export interface StyleVars {
  color?: string;
  fontWeight?: string | number;
  fontFamily?: string;
  fontSize?: string;
  fontSmoothing?: string;
  '::placeholder'?: StyleVars;
  ':hover'?: StyleVars;
  ':focus'?: StyleVars;
}

export interface PaymentElementOptions {
  layout?: 'tabs' | 'accordion' | 'auto';
  paymentMethodTypes?: PaymentMethodType[];
  amount?: number;
  currency?: string;
  defaultValues?: {
    billingDetails?: BillingDetails;
  };
}

export interface SetupElementOptions {
  customer?: string;
  usage?: 'on_session' | 'off_session';
  style?: CardStyle;
}

export interface ApplePayElementOptions {
  total: { label: string; amount: number };
  buttonType?: 'plain' | 'buy' | 'donate';
  buttonStyle?: 'black' | 'white' | 'white-outline';
}

export interface GooglePayElementOptions {
  total: { label: string; amount: number };
  buttonType?: 'long' | 'short';
  buttonColor?: 'black' | 'white';
}

export interface AlipayElementOptions {
  amount: number;
  currency: string;
  returnUrl: string;
}

export interface WeChatPayElementOptions {
  amount: number;
  currency: string;
  returnUrl: string;
}

export type PaymentMethodType = 
  | 'card' 
  | 'apple_pay' 
  | 'google_pay' 
  | 'alipay' 
  | 'wechat_pay';

export interface BillingDetails {
  name?: string;
  email?: string;
  phone?: string;
  address?: {
    line1?: string;
    line2?: string;
    city?: string;
    state?: string;
    postal_code?: string;
    country?: string;
  };
}

export interface TokenResult {
  token?: string;
  error?: NexuspayError;
}

export interface SetupResult {
  setupIntent?: SetupIntent;
  error?: NexuspayError;
}

export interface SetupIntent {
  id: string;
  status: 'requires_payment_method' | 'requires_confirmation' | 'requires_action' | 'processing' | 'succeeded' | 'canceled';
  paymentMethod?: string;
}

export interface PaymentResult {
  paymentIntent?: PaymentIntent;
  error?: NexuspayError;
}

export interface PaymentIntent {
  id: string;
  status: 'requires_payment_method' | 'requires_confirmation' | 'requires_action' | 'processing' | 'requires_capture' | 'succeeded' | 'canceled' | 'failed';
  amount: number;
  currency: string;
}

export interface NexuspayError {
  type: 'validation_error' | 'api_error' | 'card_error' | 'invalid_request_error';
  code?: string;
  message: string;
  param?: string;
}

export interface ElementChangeEvent {
  elementType: string;
  complete: boolean;
  empty: boolean;
  error?: NexuspayError;
  value?: string;
  brand?: string;
}

export interface ConfirmPaymentOptions {
  elements: Elements;
  confirmParams?: {
    return_url?: string;
    receipt_email?: string;
  };
  redirect?: 'if_required' | 'always';
}

export interface ConfirmSetupOptions {
  elements: Elements;
  confirmParams?: {
    return_url?: string;
  };
  redirect?: 'if_required' | 'always';
}

