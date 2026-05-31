import { CardElement } from '../elements/CardElement';
import { PaymentElement } from '../elements/PaymentElement';
import { SetupElement } from '../elements/SetupElement';
import { ApplePayElement } from '../elements/ApplePayElement';
import { GooglePayElement } from '../elements/GooglePayElement';
import { AlipayElement } from '../elements/AlipayElement';
import { WeChatPayElement } from '../elements/WeChatPayElement';
import { Element } from './Element';
import {
  CardElementOptions,
  PaymentElementOptions,
  SetupElementOptions,
  ApplePayElementOptions,
  GooglePayElementOptions,
  AlipayElementOptions,
  WeChatPayElementOptions,
} from '../types';

export interface ElementsConfig {
  publishableKey: string;
  apiBase: string;
  locale: string;
  appearance?: any;
  clientSecret?: string;
}

export class Elements {
  private config: ElementsConfig;
  private activeElement: Element | null = null;
  private mountedElements: Map<string, Element> = new Map();

  constructor(config: ElementsConfig) {
    this.config = config;
  }

  create(type: 'card', options?: CardElementOptions): CardElement;
  create(type: 'payment', options?: PaymentElementOptions): PaymentElement;
  create(type: 'setup', options?: SetupElementOptions): SetupElement;
  create(type: 'applePay', options?: ApplePayElementOptions): ApplePayElement;
  create(type: 'googlePay', options?: GooglePayElementOptions): GooglePayElement;
  create(type: 'alipay', options?: AlipayElementOptions): AlipayElement;
  create(type: 'wechatPay', options?: WeChatPayElementOptions): WeChatPayElement;
  create(type: string, options?: any): Element {
    const elementConfig = {
      ...this.config,
      ...options,
    };

    let element: Element;

    switch (type) {
      case 'card':
        element = new CardElement(elementConfig, options);
        break;
      case 'payment':
        element = new PaymentElement(elementConfig, options);
        break;
      case 'setup':
        element = new SetupElement(elementConfig, options);
        break;
      case 'applePay':
        element = new ApplePayElement(elementConfig, options);
        break;
      case 'googlePay':
        element = new GooglePayElement(elementConfig, options);
        break;
      case 'alipay':
        element = new AlipayElement(elementConfig, options);
        break;
      case 'wechatPay':
        element = new WeChatPayElement(elementConfig, options);
        break;
      default:
        throw new Error(`Unknown element type: ${type}`);
    }

    this.mountedElements.set(type, element);
    this.activeElement = element;
    return element;
  }

  getActiveElement(): Element | null {
    return this.activeElement;
  }

  getClientSecret(): string | undefined {
    return this.config.clientSecret;
  }

  setClientSecret(secret: string): void {
    this.config.clientSecret = secret;
  }

  getConfig(): ElementsConfig {
    return this.config;
  }

  submit(): Promise<void> {
    if (!this.activeElement) {
      return Promise.reject(new Error('No active element'));
    }
    return this.activeElement.tokenize().then(() => {});
  }
}
