/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { TextField } from '@vaadin/text-field/src/vaadin-text-field.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';

let memoizedTemplate;

class BigDecimalField extends TextField {
  static get template() {
    if (!memoizedTemplate) {
      memoizedTemplate = super.template.cloneNode(true);
      memoizedTemplate.innerHTML += `<style>
              :host {
                width: 8em;
              }

              :host([dir="rtl"]) [part="input-field"] {
                direction: ltr;
              }

              :host([dir="rtl"]) [part="input-field"] ::slotted(input) {
                --_lumo-text-field-overflow-mask-image: linear-gradient(to left, transparent, #000 1.25em) !important;
              }
        </style>`;
    }
    return memoizedTemplate;
  }

  static get is() {
    return 'vaadin-big-decimal-field';
  }

  static get properties() {
    return {
      _decimalSeparator: {
        type: String,
        value: '.',
        observer: '__decimalSeparatorChanged'
      }
    };
  }

  ready() {
    super.ready();
    this.inputElement.setAttribute('inputmode', 'decimal');
  }

  __decimalSeparatorChanged(separator, oldSeparator) {
    this.allowedCharPattern = '[-+\\d' + separator + ']';

    if (this.value && oldSeparator) {
      this.value = this.value.split(oldSeparator).join(separator);
    }
  }
}

defineCustomElement(BigDecimalField);
