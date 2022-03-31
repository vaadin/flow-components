import '@vaadin/vaadin-grid/vaadin-grid-column.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
import { GridColumnElement } from '@vaadin/vaadin-grid/src/vaadin-grid-column.js';
{
  class GridFlowSelectionColumnElement extends GridColumnElement {
    static get template() {
      return html`
    <template class="header" id="defaultHeaderTemplate">
      <style>
        /* Fix a grid web-component style that sets the font-size to small for all header contents */
        #selectAllCheckbox {
          font-size: var(--lumo-font-size-m);
        }
      </style>
      <vaadin-checkbox id="selectAllCheckbox" aria-label="Select All" hidden\$="[[selectAllHidden]]" on-click="_onSelectAllClick" checked="[[selectAll]]" indeterminate="[[indeterminate]]">
      </vaadin-checkbox>
    </template>
    <template id="defaultBodyTemplate">
      <vaadin-checkbox aria-label="Select Row" checked="[[selected]]" on-click="_onSelectClick">
      </vaadin-checkbox>
    </template>
`;
    }

    static get is() {
      return 'vaadin-grid-flow-selection-column';
    }

    static get properties() {
      return {

        /**
         * Automatically sets the width of the column based on the column contents when this is set to `true`.
         */
        autoWidth: {
          type: Boolean,
          value: true
        },

        /**
         * Width of the cells for this column.
         */
        width: {
          type: String,
          value: '56px'
        },

        /**
         * Flex grow ratio for the cell widths. When set to 0, cell width is fixed.
         */
        flexGrow: {
          type: Number,
          value: 0
        },

        /**
         * When true, all the items are selected.
         */
        selectAll: {
          type: Boolean,
          value: false,
          notify: true
        },

        /**
         * Whether to display the select all checkbox in indeterminate state,
         * which means some, but not all, items are selected
         */
        indeterminate: {
          type: Boolean,
          value: false
        },

        selectAllHidden: Boolean
      };
    }

    constructor() {
      super();
      this._boundOnSelectEvent = this._onSelectEvent.bind(this);
      this._boundOnDeselectEvent = this._onDeselectEvent.bind(this);
    }

    _prepareHeaderTemplate() {
      return this._prepareTemplatizer(this.$.defaultHeaderTemplate);
    }

    _prepareBodyTemplate() {
      return this._prepareTemplatizer(this.$.defaultBodyTemplate);
    }

    /** @private */
    connectedCallback() {
      super.connectedCallback();
      if (this._grid) {
        this._grid.addEventListener('select', this._boundOnSelectEvent);
        this._grid.addEventListener('deselect', this._boundOnDeselectEvent);
      }
    }

    /** @private */
    disconnectedCallback() {
      super.disconnectedCallback();
      if (this._grid) {
        this._grid.removeEventListener('select', this._boundOnSelectEvent);
        this._grid.removeEventListener('deselect', this._boundOnDeselectEvent);

        const isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
        if (isSafari && window.ShadyDOM && this.parentElement) {
          // Detach might have been caused by order change.
          // Shady on safari doesn't restore isAttached so we'll need to do it manually.
          const parent = this.parentElement;
          const nextSibling = this.nextElementSibling;
          parent.removeChild(this);
          if (nextSibling) {
            parent.insertBefore(this, nextSibling);
          } else {
            parent.appendChild(this);
          }
        }
      }
    }

    _onSelectClick(e) {
      e.target.checked ? this._grid.$connector.doDeselection([e.model.item], true) : this._grid.$connector.doSelection([e.model.item], true);
      e.target.checked = !e.target.checked;
    }

    _onSelectAllClick(e) {
      e.preventDefault();
      if (this._grid.hasAttribute('disabled')) {
        e.target.checked = !e.target.checked;
        return;
      }
      this.selectAll ? this.$server.deselectAll() : this.$server.selectAll();
    }

    _onSelectEvent(e) {
    }

    _onDeselectEvent(e) {
      if (e.detail.userOriginated) {
        this.selectAll = false;
      }
    }
  }

  customElements.define(GridFlowSelectionColumnElement.is, GridFlowSelectionColumnElement);

  Vaadin.GridFlowSelectionColumnElement = GridFlowSelectionColumnElement;
}
