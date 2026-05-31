import { Elements } from './Elements';
import { Element } from './Element';
import {
  NexuspayOptions,
  TokenResult,
  PaymentResult,
  SetupResult,
  ConfirmPaymentOptions,
  ConfirmSetupOptions,
} from '../types';

const DEFAULT_API_BASE = 'http://localhost:8080';

/**
 * Nexuspay SDK Entry Point
 * 
 * @example
 * const nexuspay = Nexuspay('pk_test_xxx');
 */
export function Nexuspay(publishableKey: string, options: NexuspayOptions = {}): NexuspayInstance {
  if (!publishableKey || !publishableKey.startsWith('pk_')) {
    throw new Error('Invalid publishable key. Must start with "pk_".');
  }

  const apiBase = options.apiBase || DEFAULT_API_BASE;
  const locale = options.locale || 'en';

  return {
    publishableKey,
    apiBase,
    locale,
    options,

    elements(elementsOptions = {}) {
      return new Elements({
        publishableKey,
        apiBase,
        locale: elementsOptions.locale || locale,
        appearance: elementsOptions.appearance || options.appearance,
        clientSecret: elementsOptions.clientSecret,
      });
    },

    async createToken(element: Element): Promise<TokenResult> {
      return element.tokenize();
    },

    async confirmPayment(opts: ConfirmPaymentOptions): Promise<PaymentResult> {
      const { elements, confirmParams, redirect = 'if_required' } = opts;
      
      try {
        const tokenResult = await elements.getActiveElement()?.tokenize();
        if (tokenResult?.error) {
          return { error: tokenResult.error };
        }

        const clientSecret = elements.getClientSecret();
        if (!clientSecret) {
          return { error: { type: 'invalid_request_error', message: 'No client secret provided' } };
        }

        const response = await fetch(`${apiBase}/pub/elements/confirm`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': publishableKey,
          },
          body: JSON.stringify({
            client_secret: clientSecret,
            payment_method: tokenResult?.token,
            return_url: confirmParams?.return_url,
            receipt_email: confirmParams?.receipt_email,
          }),
        });

        const data = await response.json();
        
        if (!response.ok) {
          return { error: data.error || { type: 'api_error', message: 'Payment failed' } };
        }

        if (data.requires_action && redirect === 'if_required') {
          // Handle 3DS or redirect
          if (data.next_action?.url) {
            window.location.href = data.next_action.url;
            return { paymentIntent: data.payment_intent };
          }
        }

        return { paymentIntent: data.payment_intent };
      } catch (e: any) {
        return { error: { type: 'api_error', message: e.message || 'Network error' } };
      }
    },

    async confirmSetup(opts: ConfirmSetupOptions): Promise<SetupResult> {
      const { elements, confirmParams, redirect = 'if_required' } = opts;
      
      try {
        const tokenResult = await elements.getActiveElement()?.tokenize();
        if (tokenResult?.error) {
          return { error: tokenResult.error };
        }

        const clientSecret = elements.getClientSecret();
        if (!clientSecret) {
          return { error: { type: 'invalid_request_error', message: 'No client secret provided' } };
        }

        const response = await fetch(`${apiBase}/pub/elements/setup/confirm`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': publishableKey,
          },
          body: JSON.stringify({
            client_secret: clientSecret,
            payment_method: tokenResult?.token,
            return_url: confirmParams?.return_url,
          }),
        });

        const data = await response.json();
        
        if (!response.ok) {
          return { error: data.error || { type: 'api_error', message: 'Setup failed' } };
        }

        return { setupIntent: data.setup_intent };
      } catch (e: any) {
        return { error: { type: 'api_error', message: e.message || 'Network error' } };
      }
    },
  };
}

export interface NexuspayInstance {
  publishableKey: string;
  apiBase: string;
  locale: string;
  options: NexuspayOptions;
  elements(options?: { clientSecret?: string; appearance?: any; locale?: string }): Elements;
  createToken(element: Element): Promise<TokenResult>;
  confirmPayment(options: ConfirmPaymentOptions): Promise<PaymentResult>;
  confirmSetup(options: ConfirmSetupOptions): Promise<SetupResult>;
}
