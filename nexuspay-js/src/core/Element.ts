import { TokenResult, ElementChangeEvent, NexuspayError } from '../types';

export type ElementEventListener = (event: ElementChangeEvent) => void;

export abstract class Element {
  protected container: HTMLElement | null = null;
  protected listeners: Map<string, Set<ElementEventListener>> = new Map();
  protected _state: ElementChangeEvent = {
    elementType: 'unknown',
    complete: false,
    empty: true,
  };

  abstract mount(selector: string | HTMLElement): void;
  abstract destroy(): void;
  abstract tokenize(): Promise<TokenResult>;

  on(event: 'change', listener: ElementEventListener): this;
  on(event: 'ready', listener: () => void): this;
  on(event: 'focus', listener: () => void): this;
  on(event: 'blur', listener: () => void): this;
  on(event: string, listener: any): this {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event)!.add(listener);
    return this;
  }

  off(event: string, listener?: any): this {
    if (!listener) {
      this.listeners.delete(event);
    } else {
      this.listeners.get(event)?.delete(listener);
    }
    return this;
  }

  protected emit(event: string, data?: any): void {
    const listeners = this.listeners.get(event);
    if (listeners) {
      listeners.forEach(listener => listener(data));
    }
  }

  protected resolveContainer(selector: string | HTMLElement): HTMLElement {
    if (typeof selector === 'string') {
      const el = document.querySelector<HTMLElement>(selector);
      if (!el) {
        throw new Error(`Element not found: ${selector}`);
      }
      return el;
    }
    return selector;
  }

  getState(): ElementChangeEvent {
    return { ...this._state };
  }

  protected updateState(updates: Partial<ElementChangeEvent>): void {
    this._state = { ...this._state, ...updates };
    this.emit('change', this._state);
  }

  addEventListener(event: string, listener: ElementEventListener): void {
    this.on(event, listener);
  }

  removeEventListener(event: string, listener: ElementEventListener): void {
    this.off(event, listener);
  }
}
