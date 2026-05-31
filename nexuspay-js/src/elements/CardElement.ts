import { Element } from '../core/Element';
import { CardElementOptions, TokenResult, ElementChangeEvent } from '../types';
import { createIframe, sendMessage, receiveMessages } from '../utils/postMessage';

export class CardElement extends Element {
  private options: CardElementOptions;
  private config: any;
  private iframe: HTMLIFrameElement | null = null;
  private iframeOrigin: string;

  constructor(config: any, options: CardElementOptions = {}) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'card';
    this.iframeOrigin = config.apiBase || 'http://localhost:8080';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);

    const iframeUrl = `${this.iframeOrigin}/pub/elements/card.html`;
    this.iframe = createIframe(iframeUrl, {
      width: '100%',
      height: '100%',
      border: 'none',
    });

    this.container.appendChild(this.iframe);

    // Set up postMessage communication
    receiveMessages(this.iframeOrigin, (message) => {
      this.handleIframeMessage(message);
    });

    // Send initial config when iframe is ready
    this.on('ready', () => {
      this.sendConfig();
    });
  }

  private sendConfig(): void {
    if (!this.iframe?.contentWindow) return;

    sendMessage(this.iframe.contentWindow, this.iframeOrigin, {
      type: 'nexuspay:config',
      payload: {
        publishableKey: this.config.publishableKey,
        style: this.options.style,
        locale: this.config.locale,
      },
    });
  }

  private handleIframeMessage(message: any): void {
    switch (message.type) {
      case 'nexuspay:ready':
        this.emit('ready');
        break;

      case 'nexuspay:change':
        this.updateState({
          complete: message.payload.complete,
          empty: message.payload.empty,
          error: message.payload.error,
          brand: message.payload.brand,
        });
        break;

      case 'nexuspay:focus':
        this.emit('focus');
        break;

      case 'nexuspay:blur':
        this.emit('blur');
        break;

      case 'nexuspay:tokenize:success':
        // Handled in tokenize()
        break;

      case 'nexuspay:tokenize:error':
        // Handled in tokenize()
        break;
    }
  }

  async tokenize(): Promise<TokenResult> {
    if (!this.iframe?.contentWindow) {
      return {
        error: { type: 'invalid_request_error', message: 'Element not mounted' },
      };
    }

    return new Promise((resolve) => {
      const handleResponse = (message: any) => {
        if (message.type === 'nexuspay:tokenize:success') {
          resolve({ token: message.payload.token });
          window.removeEventListener('message', handleResponse as any);
        } else if (message.type === 'nexuspay:tokenize:error') {
          resolve({ error: message.payload.error });
          window.removeEventListener('message', handleResponse as any);
        }
      };

      window.addEventListener('message', (event) => {
        if (event.origin === this.iframeOrigin) {
          handleResponse(event.data);
        }
      });

      // Request tokenization
      sendMessage(this.iframe.contentWindow!, this.iframeOrigin, {
        type: 'nexuspay:tokenize',
      });
    });
  }

  destroy(): void {
    if (this.iframe && this.iframe.parentNode) {
      this.iframe.parentNode.removeChild(this.iframe);
    }
    this.iframe = null;
    this.container = null;
    this.listeners.clear();
  }

  clear(): void {
    if (this.iframe?.contentWindow) {
      sendMessage(this.iframe.contentWindow, this.iframeOrigin, {
        type: 'nexuspay:clear',
      });
    }
  }

  focus(): void {
    if (this.iframe?.contentWindow) {
      sendMessage(this.iframe.contentWindow, this.iframeOrigin, {
        type: 'nexuspay:focus',
      });
    }
  }
}
