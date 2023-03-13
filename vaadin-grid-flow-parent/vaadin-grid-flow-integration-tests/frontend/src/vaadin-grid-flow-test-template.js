import { html, LitElement } from 'lit';

class TestTemplate extends LitElement {
  render() {
    return html`
      <div id="container" style="height:20px;"></div>
      <button id="btn">Click me!</button>
    `;
  }

  static get is() {
    return 'vaadin-grid-flow-test-template';
  }
}

customElements.define(TestTemplate.is, TestTemplate);
