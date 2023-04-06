import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class ValidationConnector extends PolymerElement {
  static get template() {
    return html`
    <div id="injected"></div>
    <slot></slot>
`;
  }

  static get is() { return 'validation-connector'; }
}

window.customElements.define(ValidationConnector.is, ValidationConnector);
