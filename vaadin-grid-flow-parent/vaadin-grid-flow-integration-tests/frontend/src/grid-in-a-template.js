import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';

class GridInATemplate extends PolymerElement {
  static get template() {
    return html`
        <vaadin-grid id="grid"></vaadin-grid>
`;
  }

  static get is() {
      return 'grid-in-a-template'
  }
}
customElements.define(GridInATemplate.is, GridInATemplate);
