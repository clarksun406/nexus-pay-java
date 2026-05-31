import { Element } from '../core/Element';
import { ApplePayElementOptions, TokenResult } from '../types';

export class ApplePayElement extends Element {
  private options: ApplePayElementOptions;
  private config: any;
  private button: HTMLButtonElement | null = null;
  private session: any = null;

  constructor(config: any, options: ApplePayElementOptions) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'applePay';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);

    // Check if Apple Pay is available
    if (!window.ApplePaySession || !ApplePaySession.canMakePayments()) {
      this.container.innerHTML = '<p>Apple Pay is not available</p>';
      return;
    }

    this.button = document.createElement('button');
    this.button.setAttribute('lang', this.config.locale || 'en');
    this.button.className = 'apple-pay-button';
    this.button.style.cssText = `
      -webkit-appearance: -apple-pay-button;
      -apple-pay-button-type: ${this.options.buttonType || 'plain'};
      -apple-pay-button-style: ${this.options.buttonStyle || 'black'};
      width: 100%;
      height: 44px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    `;
    this.button.onclick = () => this.startSession();
    this.container.appendChild(this.button);
    this.emit('ready');
  }

  private async startSession(): Promise<void> {
    const request = {
      countryCode: 'US',
      currencyCode: 'USD',
      supportedNetworks: ['visa', 'masterCard', 'amex'],
      merchantCapabilities: ['supports3DS'],
      total: this.options.total,
    };

    try {
      const session = new ApplePaySession(3, request);
      
      session.onvalidatemerchant = async (event: any) => {
        const response = await fetch(`${this.config.apiBase}/pub/elements/apple-pay/validate`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': this.config.publishableKey,
          },
          body: JSON.stringify({ validationURL: event.validationURL }),
        });
        const data = await response.json();
        session.completeMerchantValidation(data.merchantSession);
      };

      session.onpaymentauthorized = async (event: any) => {
        const token = btoa(JSON.stringify(event.payment.token));
        this.updateState({ complete: true, empty: false });
        this.emit('change', this._state);
        session.completePayment(ApplePaySession.STATUS_SUCCESS);
      };

      session.begin();
    } catch (e: any) {
      this.updateState({ error: { type: 'api_error', message: e.message } });
    }
  }

  async tokenize(): Promise<TokenResult> {
    // Apple Pay tokenization happens in onpaymentauthorized
    // This method would return the stored token
    return { token: undefined, error: { type: 'api_error', message: 'Use onpaymentauthorized callback' } };
  }

  destroy(): void {
    this.button?.remove();
    this.button = null;
  }
}

// Declare ApplePaySession for TypeScript
declare global {
  interface Window {
    ApplePaySession: any;
  }
  const ApplePaySession: any;
}
