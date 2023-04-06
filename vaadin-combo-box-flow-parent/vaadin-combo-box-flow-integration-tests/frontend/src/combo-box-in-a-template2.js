import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class ComboBoxInATemplate2 extends PolymerElement {
  static get template() {
    return html`
        <vaadin-combo-box id="comboBox2"></vaadin-combo-box>
`;
  }

  static get is() {
      return 'combo-box-in-a-template2'
  }
}
customElements.define(ComboBoxInATemplate2.is, ComboBoxInATemplate2);
