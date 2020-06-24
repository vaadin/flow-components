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
