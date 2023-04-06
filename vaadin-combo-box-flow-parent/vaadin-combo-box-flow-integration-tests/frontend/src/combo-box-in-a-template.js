import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class ComboBoxInATemplate extends PolymerElement {
  static get template() {
    return html`
        <vaadin-combo-box id="comboBox"></vaadin-combo-box>
`;
  }

  static get is() {
      return 'combo-box-in-a-template'
  }
}
customElements.define(ComboBoxInATemplate.is, ComboBoxInATemplate);
