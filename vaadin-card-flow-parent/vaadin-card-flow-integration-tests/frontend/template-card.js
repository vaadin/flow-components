import { html, LitElement } from 'lit';
import '@vaadin/card/vaadin-card.js';

class TemplateCard extends LitElement {
  render() {
    return html`
      <vaadin-card id="card">Template content</vaadin-card>
    `;
  }

  static get is() {
    return 'template-card';
  }
}

customElements.define(TemplateCard.is, TemplateCard);
