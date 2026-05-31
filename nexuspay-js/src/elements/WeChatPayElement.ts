import { Element } from '../core/Element';
import { WeChatPayElementOptions, TokenResult } from '../types';

export class WeChatPayElement extends Element {
  private options: WeChatPayElementOptions;
  private config: any;

  constructor(config: any, options: WeChatPayElementOptions) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'wechatPay';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);

    const button = document.createElement('button');
    button.className = 'nexuspay-wechatpay-button';
    button.style.cssText = `
      width: 100%;
      height: 44px;
      background: #07C160;
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
        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.32.32 0 0 0 .168-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.27.27 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982z"/>
      </svg>
      WeChat Pay
    `;
    button.onclick = () => this.startPayment();

    this.container.appendChild(button);
    this.emit('ready');
  }

  private async startPayment(): Promise<void> {
    try {
      const response = await fetch(`${this.config.apiBase}/pub/elements/wechatpay/create`, {
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

      if (data.h5_url) {
        // H5 payment (mobile browser)
        window.location.href = data.h5_url;
      } else if (data.code_url) {
        // Native QR code
        this.showQRCode(data.code_url);
      } else if (data.redirect_url) {
        window.location.href = data.redirect_url;
      }
    } catch (e: any) {
      this.updateState({ error: { type: 'api_error', message: e.message } });
    }
  }

  private showQRCode(codeUrl: string): void {
    if (!this.container) return;
    this.container.innerHTML = `
      <div style="text-align: center; padding: 20px;">
        <p style="margin-bottom: 12px;">Scan with WeChat</p>
        <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(codeUrl)}" alt="WeChat Pay QR" style="max-width: 200px;" />
      </div>
    `;
  }

  async tokenize(): Promise<TokenResult> {
    return { token: undefined, error: { type: 'api_error', message: 'WeChat Pay uses redirect flow' } };
  }

  destroy(): void {
    this.container!.innerHTML = '';
  }
}
