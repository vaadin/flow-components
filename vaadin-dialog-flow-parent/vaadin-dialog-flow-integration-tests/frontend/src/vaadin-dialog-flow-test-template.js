import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class TemplateButton extends PolymerElement {
    static get template() {
      return html`
        <div id="container"></div>
        <button id="btn">Click me!</button>
`;
  }
    static get is() {
      return 'vaadin-dialog-flow-test-template'
  }
}
customElements.define(TemplateButton.is, TemplateButton);
