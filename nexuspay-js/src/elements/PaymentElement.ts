import { Element } from '../core/Element';
import { PaymentElementOptions, TokenResult } from '../types';
import { createIframe, sendMessage, receiveMessages } from '../utils/postMessage';

export class PaymentElement extends Element {
  private options: PaymentElementOptions;
  private config: any;
  private iframe: HTMLIFrameElement | null = null;
  private iframeOrigin: string;

  constructor(config: any, options: PaymentElementOptions = {}) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'payment';
    this.iframeOrigin = config.apiBase || 'http://localhost:8080';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);
    
    const params = new URLSearchParams({
      amount: String(this.options.amount || 0),
      currency: this.options.currency || 'usd',
      layout: this.options.layout || 'auto',
      methods: (this.options.paymentMethodTypes || ['card']).join(','),
    });
    
    const iframeUrl = `${this.iframeOrigin}/pub/elements/payment.html?${params}`;
    this.iframe = createIframe(iframeUrl, { height: '400px' });
    this.container.appendChild(this.iframe);

    receiveMessages(this.iframeOrigin, (message) => {
      if (message.type === 'nexuspay:ready') this.emit('ready');
      if (message.type === 'nexuspay:change') this.updateState(message.payload);
      if (message.type === 'nexuspay:payment_method_selected') {
        this.updateState({ ...this._state, brand: message.payload.method });
      }
    });
  }

  async tokenize(): Promise<TokenResult> {
    if (!this.iframe?.contentWindow) {
      return { error: { type: 'invalid_request_error', message: 'Element not mounted' } };
    }

    return new Promise((resolve) => {
      const handler = (e: MessageEvent) => {
        if (e.origin !== this.iframeOrigin) return;
        if (e.data.type === 'nexuspay:tokenize:success') {
          resolve({ token: e.data.payload.token });
          window.removeEventListener('message', handler);
        } else if (e.data.type === 'nexuspay:tokenize:error') {
          resolve({ error: e.data.payload.error });
          window.removeEventListener('message', handler);
        }
      };
      window.addEventListener('message', handler);
      sendMessage(this.iframe.contentWindow, this.iframeOrigin, { type: 'nexuspay:tokenize' });
    });
  }

  destroy(): void {
    this.iframe?.remove();
    this.iframe = null;
  }
}
