import { Element } from '../core/Element';
import { GooglePayElementOptions, TokenResult } from '../types';

export class GooglePayElement extends Element {
  private options: GooglePayElementOptions;
  private config: any;
  private button: HTMLElement | null = null;

  constructor(config: any, options: GooglePayElementOptions) {
    super();
    this.config = config;
    this.options = options;
    this._state.elementType = 'googlePay';
  }

  mount(selector: string | HTMLElement): void {
    this.container = this.resolveContainer(selector);

    // Load Google Pay script
    if (!document.querySelector('script[src*="pay.google.com"]')) {
      const script = document.createElement('script');
      script.src = 'https://pay.google.com/gp/p/js/pay.js';
      script.onload = () => this.initGooglePay();
      document.head.appendChild(script);
    } else {
      this.initGooglePay();
    }
  }

  private async initGooglePay(): Promise<void> {
    if (!window.google?.payments) {
      this.container!.innerHTML = '<p>Google Pay is not available</p>';
      return;
    }

    const paymentsClient = new google.payments.api.PaymentsClient({
      environment: this.config.mode === 'live' ? 'PRODUCTION' : 'TEST',
    });

    const isReady = await paymentsClient.isReadyToPay({
      apiVersion: 2,
      apiVersionMinor: 0,
      allowedPaymentMethods: [{
        type: 'CARD',
        parameters: {
          allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
          allowedCardNetworks: ['VISA', 'MASTERCARD'],
        },
      }],
    });

    if (!isReady.result) {
      this.container!.innerHTML = '<p>Google Pay is not available</p>';
      return;
    }

    this.button = await paymentsClient.createButton({
      onClick: () => this.startPayment(paymentsClient),
      buttonType: this.options.buttonType || 'long',
      buttonColor: this.options.buttonColor || 'black',
    });

    this.container!.appendChild(this.button);
    this.emit('ready');
  }

  private async startPayment(client: any): Promise<void> {
    try {
      const paymentData = await client.loadPaymentData({
        apiVersion: 2,
        apiVersionMinor: 0,
        merchantInfo: { merchantName: 'NexusPay' },
        transactionInfo: {
          totalPriceStatus: 'FINAL',
          totalPrice: String(this.options.total.amount / 100),
          currencyCode: 'USD',
        },
        allowedPaymentMethods: [{
          type: 'CARD',
          parameters: {
            allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
            allowedCardNetworks: ['VISA', 'MASTERCARD'],
          },
          tokenizationSpecification: {
            type: 'PAYMENT_GATEWAY',
            parameters: { gateway: 'nexuspay' },
          },
        }],
      });

      const token = btoa(JSON.stringify(paymentData.paymentMethodData.tokenizationData.token));
      this.updateState({ complete: true });
    } catch (e: any) {
      this.updateState({ error: { type: 'api_error', message: e.message } });
    }
  }

  async tokenize(): Promise<TokenResult> {
    return { token: undefined, error: { type: 'api_error', message: 'Use callback instead' } };
  }

  destroy(): void {
    this.button?.remove();
    this.button = null;
  }
}

declare global {
  interface Window {
    google?: {
      payments: {
        api: any;
      };
    };
  }
  namespace google {
    namespace payments {
      namespace api {
        class PaymentsClient {
          constructor(options: any);
          isReadyToPay(options: any): Promise<any>;
          createButton(options: any): Promise<HTMLElement>;
          loadPaymentData(options: any): Promise<any>;
        }
      }
    }
  }
}
