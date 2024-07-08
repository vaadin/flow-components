/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
(function() {

  let memoizedTemplate;

  customElements.whenDefined('vaadin-text-field').then(() => {

    class BigDecimalFieldElement extends customElements.get('vaadin-text-field') {

      static get template() {
        if (!memoizedTemplate) {
          memoizedTemplate = super.template.cloneNode(true);
          memoizedTemplate.innerHTML +=
            `<style>
                  :host {
                    width: 8em;
                  }

                  :host([dir="rtl"]) [part="input-field"] {
                    direction: ltr;
                  }

                  :host([dir="rtl"]) [part="value"]::placeholder {
                    direction: rtl;
                  }

                  :host([dir="rtl"]) [part="input-field"] ::slotted(input)::placeholder {
                    direction: rtl;
                  }

                  :host([dir="rtl"]) [part="value"]:-ms-input-placeholder,
                  :host([dir="rtl"]) [part="input-field"] ::slotted(input):-ms-input-placeholder {
                    direction: rtl;
                  }

                  :host([dir="rtl"]:not([has-controls])) [part="value"]::placeholder {
                    text-align: left;
                  }

                  :host([dir="rtl"]:not([has-controls])) [part="input-field"] ::slotted(input)::placeholder {
                    text-align: left;
                  }

                  :host([dir="rtl"]:not([has-controls])) [part="value"]:-ms-input-placeholder,
                  :host([dir="rtl"]:not([has-controls])) [part="input-field"] ::slotted(input):-ms-input-placeholder {
                    text-align: left;
                  }

                  :host([dir="rtl"]) [part="value"],
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
        }
      }

      ready() {
        super.ready();
        this.inputElement.setAttribute('inputmode', 'decimal');
      }

      __decimalSeparatorChanged(separator, oldSeparator) {
        this._enabledCharPattern = '[\\d-+' + separator + ']';

        if (this.value && oldSeparator) {
          this.value = this.value.split(oldSeparator).join(separator);
        }
      }

    }

    customElements.define(BigDecimalFieldElement.is, BigDecimalFieldElement);

  });
})();
