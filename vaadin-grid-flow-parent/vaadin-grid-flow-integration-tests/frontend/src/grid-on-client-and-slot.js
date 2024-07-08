/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import { PolymerElement, html } from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-grid/src/vaadin-grid.js';
import '@vaadin/vaadin-grid/src/vaadin-grid-column.js';
import '@vaadin/vaadin-grid/src/vaadin-grid-tree-toggle.js';

class GridOnClientAndSlot extends PolymerElement {

  static get template() {
    return html`
      <style>
        :host {
          display: flex;
          flex-direction: column;
          width: 100%;
          height: 100%;
          }
      </style>

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
        type: Array,
        value: [
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
        ]
      }
    }
  }

  connectedCallback() {
    super.connectedCallback();

    this.$.tree.dataProvider = (params, callback) => {
      if (params.parentItem != null) {
        callback(params.parentItem.children, params.parentItem.children.length);
      }
      else {
        callback(this.items, this.items.length);
      }
    };
  }

}

customElements.define(GridOnClientAndSlot.is, GridOnClientAndSlot);