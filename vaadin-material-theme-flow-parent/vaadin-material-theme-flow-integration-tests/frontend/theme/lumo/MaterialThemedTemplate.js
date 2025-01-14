import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';

class MaterialThemedTemplate extends PolymerElement {
  static get is() {
    return 'material-themed-template';
  }

  static get template() {
    return html`
      <div id="div">Lumo themed Template</div>
      <style>
        div {
          color: var(--lumo-error-color); /* color */
          font-size: var(--lumo-font-size-xxxl); /* typography */
        }
      </style>
    `;
  }
}
customElements.define(MaterialThemedTemplate.is, MaterialThemedTemplate);
