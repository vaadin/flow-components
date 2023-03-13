import { html, LitElement } from 'lit';
import '@vaadin/date-picker/vaadin-date-picker.js';

class InjectedDatepickerI18n extends LitElement {
  render() {
    return html`<vaadin-date-picker id="date-picker"></vaadin-date-picker>`;
  }

  static get is() {
    return 'injected-datepicker-i18n';
  }
}

customElements.define(InjectedDatepickerI18n.is, InjectedDatepickerI18n);
