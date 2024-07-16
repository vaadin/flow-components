/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';
import '@vaadin/vaadin-date-picker/vaadin-date-picker.js';

class InjectedDatepickerI18n extends PolymerElement {
    static get template() {
      return html`
    <vaadin-date-picker id="date-picker"></vaadin-date-picker>
    `;
  }
  static get is() {
      return 'injected-datepicker-i18n'
  }
}

customElements.define(InjectedDatepickerI18n.is, InjectedDatepickerI18n); 
