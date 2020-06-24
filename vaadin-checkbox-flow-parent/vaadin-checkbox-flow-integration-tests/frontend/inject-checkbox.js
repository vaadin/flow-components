import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';
import '@vaadin/vaadin-checkbox/vaadin-checkbox.js';

class InjectChecbox extends PolymerElement {
    static get template() {
      return html`
    <vaadin-checkbox id="accept">Accept</vaadin-checkbox>
    <div id="div">A</div>
`;
  }
      static get is() {
      return 'inject-checkbox'
  }
}

customElements.define(InjectChecbox.is, InjectChecbox);