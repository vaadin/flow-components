import { html, LitElement } from 'lit';
import '@vaadin/button/vaadin-button.js';
import '@vaadin/icon/vaadin-icon.js';

class TemplateButton extends LitElement {
  render() {
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
