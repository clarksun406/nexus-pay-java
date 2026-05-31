/**
 * PostMessage utilities for iframe communication
 */

export interface PostMessage {
  type: string;
  payload?: any;
}

export function createIframe(
  src: string,
  options: { width?: string; height?: string; border?: string } = {}
): HTMLIFrameElement {
  const iframe = document.createElement('iframe');
  iframe.src = src;
  iframe.style.width = options.width || '100%';
  iframe.style.height = options.height || '100%';
  iframe.style.border = options.border || 'none';
  iframe.style.overflow = 'hidden';
  iframe.setAttribute('frameborder', '0');
  iframe.setAttribute('scrolling', 'no');
  iframe.setAttribute('allowtransparency', 'true');
  return iframe;
}

export function sendMessage(
  target: Window,
  targetOrigin: string,
  message: PostMessage
): void {
  target.postMessage(message, targetOrigin);
}

export function receiveMessages(
  expectedOrigin: string,
  handler: (message: PostMessage) => void
): () => void {
  const listener = (event: MessageEvent) => {
    if (event.origin !== expectedOrigin) return;
    handler(event.data);
  };

  window.addEventListener('message', listener);

  // Return cleanup function
  return () => {
    window.removeEventListener('message', listener);
  };
}

export function sendMessageToParent(message: PostMessage): void {
  if (window.parent) {
    window.parent.postMessage(message, '*');
  }
}

export function receiveMessagesFromParent(
  handler: (message: PostMessage) => void
): () => void {
  const listener = (event: MessageEvent) => {
    handler(event.data);
  };

  window.addEventListener('message', listener);

  return () => {
    window.removeEventListener('message', listener);
  };
}
