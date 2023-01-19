import { css, html, LitElement, render } from 'lit';
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
      <vaadin-grid id="tree" .expandedItems="${this.expandedItems}">
        <vaadin-grid-column
          .renderer=${(root, _, model) => {
            render(
              html`<vaadin-grid-tree-toggle
                .__currentItem="${model.item}"
                .leaf="${model.item.leaf}"
                .expanded="${model.expanded}"
                @expanded-changed="${this.__expandedChanged}"
                .level="${model.level}"
              >
                <div class="tree-cell">${model.item.name}</div>
              </vaadin-grid-tree-toggle>`,
              root
            );
          }}
        >
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
      },
      expandedItems: {
        type: Array
      }
    };
  }

  constructor() {
    super();

    this.__expandedChanged = this.__expandedChanged.bind(this);

    this.expandedItems = [];

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

  __expandedChanged(e) {
    const item = e.currentTarget.__currentItem;
    const expanded = e.detail.value;
    const itemExpanded = this.expandedItems.includes(item);
  
    if (expanded && !itemExpanded) {
      this.expandedItems = [...this.expandedItems, item];
    } else if (!expanded && itemExpanded) {
      this.expandedItems = this.expandedItems.filter((i) => i !== item);
    }
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
