import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
import '@vaadin/button/vaadin-button.js';
import '@vaadin/icon/vaadin-icon.js';

class TemplateButton extends PolymerElement {
  static get template() {
    return html`
      <vaadin-button id="button">Template caption</vaadin-button>

      <vaadin-button id="icon-button">
        <vaadin-icon icon="lumo:edit"></vaadin-icon>
        <span>Template with icon</span>
      </vaadin-button>
    `;
  }

  static get is() {
    return 'template-button';
  }
}

customElements.define(TemplateButton.is, TemplateButton);
