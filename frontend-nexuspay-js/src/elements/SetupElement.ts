import { Element } from '../core/Element';
import { SetupElementOptions, TokenResult } from '../types';
import { createIframe, sendMessage, receiveMessages } from '../utils/postMessage';

export class SetupElement extends Element {
  private options: SetupElementOptions;
  private config: any;
  private iframe: HTMLIFrameElement | null = null;
  private iframeOrigin: string;

  constructor(config: any, options: SetupElementOptions = {}) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'setup';
    this.iframeOrigin = config.apiBase || 'http://localhost:8080';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);
    const iframeUrl = `${this.iframeOrigin}/pub/elements/card.html?mode=setup`;
    this.iframe = createIframe(iframeUrl);
    this.container.appendChild(this.iframe);

    receiveMessages(this.iframeOrigin, (message) => {
      if (message.type === 'nexuspay:ready') {
        this.emit('ready');
        this.sendConfig();
      }
      if (message.type === 'nexuspay:change') this.updateState(message.payload);
    });
  }

  private sendConfig(): void {
    if (!this.iframe?.contentWindow) return;

    sendMessage(this.iframe.contentWindow, this.iframeOrigin, {
      type: 'nexuspay:config',
      payload: {
        publishableKey: this.config.publishableKey,
        apiBase: this.config.apiBase,
        locale: this.config.locale,
      },
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
      const win = this.iframe?.contentWindow;
      if (!win) {
        resolve({ error: { type: 'invalid_request_error', message: 'Element not mounted' } });
        return;
      }
      sendMessage(win, this.iframeOrigin, { type: 'nexuspay:tokenize' });
    });
  }

  destroy(): void {
    this.iframe?.remove();
    this.iframe = null;
  }
}

