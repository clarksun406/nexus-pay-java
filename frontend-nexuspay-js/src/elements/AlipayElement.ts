import { Element } from '../core/Element';
import { AlipayElementOptions, TokenResult } from '../types';

export class AlipayElement extends Element {
  private options: AlipayElementOptions;
  private config: any;

  constructor(config: any, options: AlipayElementOptions) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'alipay';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);

    const button = document.createElement('button');
    button.className = 'nexuspay-alipay-button';
    button.style.cssText = `
      width: 100%;
      height: 44px;
      background: #1677FF;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    `;
    button.innerHTML = `
      <svg width="20" height="20" viewBox="0 0 24 24" fill="white">
        <path d="M21.422 15.358c-3.248-1.21-6.118-2.59-7.801-3.455.386-.696.746-1.443 1.056-2.235h-4.18V8.345h5.073V7.12H10.5V4.875H8.25v2.245H3.188v1.225H8.25v1.323H4.5v1.222h8.1c-.24.56-.5 1.097-.78 1.602-2.48-.87-5.26-1.42-7.52-.99-3.27.62-4.16 2.71-3.81 4.28.35 1.57 1.87 2.87 4.05 2.87 2.88 0 5.3-1.64 7.08-4.02 2.45 1.12 6.46 2.82 10.38 4.11l.42-2.41z"/>
      </svg>
      Alipay
    `;
    button.onclick = () => this.startPayment();

    this.container.appendChild(button);
    this.emit('ready');
  }

  private async startPayment(): Promise<void> {
    try {
      const response = await fetch(`${this.config.apiBase}/pub/elements/alipay/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': this.config.publishableKey,
        },
        body: JSON.stringify({
          amount: this.options.amount,
          currency: this.options.currency,
          return_url: this.options.returnUrl,
        }),
      });

      const data = await response.json();

      if (data.redirect_url) {
        window.location.href = data.redirect_url;
      } else if (data.qr_code) {
        this.showQRCode(data.qr_code);
      }
    } catch (e: any) {
      this.updateState({ error: { type: 'api_error', message: e.message } });
    }
  }

  private showQRCode(qrCode: string): void {
    if (!this.container) return;
    this.container.innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p style="margin-bottom: 12px;">Scan with Alipay</p>
        <img src="${qrCode}" alt="Alipay QR Code" style="max-width: 200px;" />
      </div>
    `;
  }

  async tokenize(): Promise<TokenResult> {
    // Alipay uses redirect flow, no direct tokenization
    return { token: undefined, error: { type: 'api_error', message: 'Alipay uses redirect flow' } };
  }

  destroy(): void {
    this.container!.innerHTML = '';
  }
}
