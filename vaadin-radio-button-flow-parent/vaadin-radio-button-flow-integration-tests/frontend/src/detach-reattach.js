import { html, LitElement } from 'lit';
import '@vaadin/radio-group/src/vaadin-radio-group.js';

class DetachReattach extends LitElement {
  render() {
    return html`<vaadin-radio-group id="testGroup"></vaadin-radio-group>`;
  }

  static get is() {
    return 'detach-reattach';
  }
}

customElements.define(DetachReattach.is, DetachReattach);
