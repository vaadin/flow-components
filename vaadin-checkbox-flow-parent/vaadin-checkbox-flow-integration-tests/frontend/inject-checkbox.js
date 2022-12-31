import { html, LitElement } from 'lit';
import '@vaadin/checkbox/vaadin-checkbox.js';

class InjectCheckbox extends LitElement {
  render() {
    return html`
      <vaadin-checkbox id="accept" label="Accept"></vaadin-checkbox>
      <div id="div">A</div>
    `;
  }

  static get is() {
    return 'inject-checkbox';
  }
}

customElements.define(InjectCheckbox.is, InjectCheckbox);
