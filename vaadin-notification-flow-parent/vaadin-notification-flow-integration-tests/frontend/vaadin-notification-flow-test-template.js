import { html, LitElement } from 'lit';

class TemplateButton extends LitElement {
  render() {
    return html`
      <div id="container"></div>
      <button id="btn">Click me!</button>
    `;
  }

  static get is() {
    return 'vaadin-notification-flow-test-template';
  }
}

customElements.define(TemplateButton.is, TemplateButton);
