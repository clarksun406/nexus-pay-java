/**
 * NexusPay Card Element - iframe script
 * Handles card input, validation, and tokenization
 */

(function() {
  'use strict';

  // State
  let config = {};
  let cardBrand = null;
  
  // Elements
  const cardNumberInput = document.getElementById('cardNumber');
  const expiryInput = document.getElementById('expiry');
  const cvcInput = document.getElementById('cvc');
  const cardBrandEl = document.getElementById('cardBrand');

  // Card brands (simplified patterns)
  const CARD_BRANDS = {
    visa: { pattern: /^4/, cvcLength: 3 },
    mastercard: { pattern: /^(5[1-5]|2[2-7])/, cvcLength: 3 },
    amex: { pattern: /^3[47]/, cvcLength: 4 },
    discover: { pattern: /^6(?:011|5)/, cvcLength: 3 },
    jcb: { pattern: /^35/, cvcLength: 3 },
  };

  // Initialize
  function init() {
    setupEventListeners();
    sendToParent('nexuspay:ready');
  }

  // Setup input event listeners
  function setupEventListeners() {
    // Card number formatting and validation
    cardNumberInput.addEventListener('input', (e) => {
      let value = e.target.value.replace(/\s/g, '').replace(/\D/g, '');
      value = value.substring(0, 16);
      
      // Format with spaces
      const formatted = value.replace(/(\d{4})(?=\d)/g, '$1 ');
      e.target.value = formatted;
      
      // Detect brand
      detectBrand(value);
      
      // Validate
      const isValid = luhnCheck(value) && value.length >= 13;
      setFieldState(cardNumberInput, value.length === 0 ? null : isValid);
      
      emitChange();
    });

    // Expiry formatting
    expiryInput.addEventListener('input', (e) => {
      let value = e.target.value.replace(/\D/g, '');
      
      if (value.length >= 2) {
        value = value.substring(0, 2) + ' / ' + value.substring(2, 4);
      }
      e.target.value = value;
      
      const isValid = validateExpiry(value);
      setFieldState(expiryInput, value.length === 0 ? null : isValid);
      
      emitChange();
    });

    // CVC validation
    cvcInput.addEventListener('input', (e) => {
      let value = e.target.value.replace(/\D/g, '');
      const maxLength = cardBrand === 'amex' ? 4 : 3;
      e.target.value = value.substring(0, maxLength);
      
      const isValid = value.length >= 3;
      setFieldState(cvcInput, value.length === 0 ? null : isValid);
      
      emitChange();
    });

    // Focus events
    [cardNumberInput, expiryInput, cvcInput].forEach(input => {
      input.addEventListener('focus', () => sendToParent('nexuspay:focus'));
      input.addEventListener('blur', () => sendToParent('nexuspay:blur'));
    });
  }

  // Detect card brand
  function detectBrand(number) {
    cardBrand = null;
    
    for (const [brand, data] of Object.entries(CARD_BRANDS)) {
      if (data.pattern.test(number)) {
        cardBrand = brand;
        break;
      }
    }
    
    // Update brand icon (simplified - would use actual SVGs)
    if (cardBrand) {
      cardBrandEl.style.background = `url('data:image/svg+xml,...')`;
      cardBrandEl.title = cardBrand.toUpperCase();
    } else {
      cardBrandEl.style.background = '';
    }
  }

  // Luhn algorithm
  function luhnCheck(num) {
    if (num.length < 13) return false;
    
    let sum = 0;
    let isEven = false;
    
    for (let i = num.length - 1; i >= 0; i--) {
      let digit = parseInt(num[i], 10);
      
      if (isEven) {
        digit *= 2;
        if (digit > 9) digit -= 9;
      }
      
      sum += digit;
      isEven = !isEven;
    }
    
    return sum % 10 === 0;
  }

  // Validate expiry
  function validateExpiry(value) {
    const match = value.match(/^(\d{2})\s*\/\s*(\d{2})$/);
    if (!match) return false;
    
    const month = parseInt(match[1], 10);
    const year = parseInt('20' + match[2], 10);
    
    if (month < 1 || month > 12) return false;
    
    const now = new Date();
    const expDate = new Date(year, month - 1);
    
    return expDate >= now;
  }

  // Set field visual state
  function setFieldState(input, isValid) {
    input.classList.remove('error', 'valid');
    if (isValid === true) input.classList.add('valid');
    if (isValid === false) input.classList.add('error');
  }

  // Check if form is complete
  function isComplete() {
    const cardNumber = cardNumberInput.value.replace(/\s/g, '');
    const expiry = expiryInput.value;
    const cvc = cvcInput.value;
    
    return luhnCheck(cardNumber) && validateExpiry(expiry) && cvc.length >= 3;
  }

  // Emit change event to parent
  function emitChange() {
    sendToParent('nexuspay:change', {
      complete: isComplete(),
      empty: !cardNumberInput.value && !expiryInput.value && !cvcInput.value,
      brand: cardBrand,
    });
  }

  // Tokenize card
  async function tokenize() {
    const cardNumber = cardNumberInput.value.replace(/\s/g, '');
    const expiry = expiryInput.value.match(/(\d{2})\s*\/\s*(\d{2})/);
    const cvc = cvcInput.value;

    if (!isComplete()) {
      sendToParent('nexuspay:tokenize:error', {
        error: { type: 'validation_error', message: 'Card details incomplete' }
      });
      return;
    }

    try {
      const response = await fetch(`${config.apiBase || ''}/pub/elements/tokenize`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': config.publishableKey,
        },
        body: JSON.stringify({
          card: {
            number: cardNumber,
            exp_month: expiry[1],
            exp_year: '20' + expiry[2],
            cvc: cvc,
          },
          brand: cardBrand,
        }),
      });

      const data = await response.json();

      if (response.ok && data.token) {
        sendToParent('nexuspay:tokenize:success', { token: data.token });
      } else {
        sendToParent('nexuspay:tokenize:error', {
          error: data.error || { type: 'api_error', message: 'Tokenization failed' }
        });
      }
    } catch (e) {
      sendToParent('nexuspay:tokenize:error', {
        error: { type: 'api_error', message: e.message || 'Network error' }
      });
    }
  }

  // Send message to parent window
  function sendToParent(type, payload = {}) {
    window.parent.postMessage({ type, payload }, '*');
  }

  // Listen for messages from parent
  window.addEventListener('message', (event) => {
    const { type, payload } = event.data;

    switch (type) {
      case 'nexuspay:config':
        config = payload;
        break;
      case 'nexuspay:tokenize':
        tokenize();
        break;
      case 'nexuspay:clear':
        cardNumberInput.value = '';
        expiryInput.value = '';
        cvcInput.value = '';
        cardBrand = null;
        emitChange();
        break;
      case 'nexuspay:focus':
        cardNumberInput.focus();
        break;
    }
  });

  // Start
  init();
})();
