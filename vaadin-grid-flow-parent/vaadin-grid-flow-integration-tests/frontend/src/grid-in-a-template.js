import { html, LitElement } from 'lit';

class GridInATemplate extends LitElement {
  render() {
    return html`<vaadin-grid id="grid"></vaadin-grid>`;
  }

  static get is() {
    return 'grid-in-a-template';
  }
}

customElements.define(GridInATemplate.is, GridInATemplate);
