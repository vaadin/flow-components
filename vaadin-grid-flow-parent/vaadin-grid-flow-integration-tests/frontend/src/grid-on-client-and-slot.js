import { css, html, LitElement } from 'lit';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-tree-toggle.js';

class GridOnClientAndSlot extends LitElement {
  static get styles() {
    return css`
      :host {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
      }
    `;
  }

  render() {
    return html`
      <vaadin-grid id="tree">
        <vaadin-grid-column>
          <template>
            <vaadin-grid-tree-toggle leaf="[[item.leaf]]" expanded="{{expanded}}" level="[[level]]">
              <div class="tree-cell">[[item.name]]</div>
            </vaadin-grid-tree-toggle>
          </template>
        </vaadin-grid-column>
      </vaadin-grid>
      <button>This is a button</button>
      <slot></slot>
    `;
  }

  static get is() {
    return 'grid-on-client-and-slot';
  }

  static get properties() {
    return {
      items: {
        type: Array
      }
    };
  }

  constructor() {
    super();

    this.items = [
      {
        name: 'item 1',
        leaf: false,
        children: [
          {
            name: 'child 1-1',
            leaf: true
          },
          {
            name: 'child 1-2',
            leaf: true
          }
        ]
      },
      {
        name: 'item 2',
        children: [
          {
            name: 'child 2-1',
            leaf: true
          },
          {
            name: 'child 2-2',
            leaf: true
          }
        ]
      }
    ];
  }

  firstUpdated() {
    super.firstUpdated();

    this.shadowRoot.getElementById('tree').dataProvider = (params, callback) => {
      if (params.parentItem != null) {
        callback(params.parentItem.children, params.parentItem.children.length);
      } else {
        callback(this.items, this.items.length);
      }
    };
  }
}

customElements.define(GridOnClientAndSlot.is, GridOnClientAndSlot);
