import { LitElement, html, css } from 'lit';

class TestComponent extends LitElement {
  static get is() {
    return 'test-component';
  }

  static get styles() {
    return css`
      :host {
        color: var(--lumo-error-color); /* color */
        font-size: var(--lumo-font-size-xxxl); /* typography */
        border: var(--lumo-size-m) solid black; /* sizing */
        margin: var(--lumo-space-wide-l); /* spacing */
        border-radius: var(--lumo-border-radius-l); /* style */
        font-family: lumo-icons; /* icons */
      }
    `;
  }

  render() {
    return html`Test component`;
  }
}
customElements.define(TestComponent.is, TestComponent);
