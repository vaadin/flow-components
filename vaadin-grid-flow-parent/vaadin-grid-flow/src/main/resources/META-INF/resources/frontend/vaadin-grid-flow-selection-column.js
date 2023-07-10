import '@vaadin/grid/vaadin-grid-column.js';
import { GridColumn } from '@vaadin/grid/src/vaadin-grid-column.js';
import { GridSelectionColumnBaseMixin } from '@vaadin/grid/src/vaadin-grid-selection-column-base-mixin.js';

class GridFlowSelectionColumn extends GridSelectionColumnBaseMixin(GridColumn) {

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
      }
    };
  }

  /**
   * Renders the Select All checkbox to the header cell.
   *
   * @override
   */
  _defaultHeaderRenderer(root, _column) {
    super._defaultHeaderRenderer(root, _column);
    const checkbox = root.firstElementChild;
    if (checkbox) {
      // TODO: Remove id and use CSS class selector in TestBench
      checkbox.id = 'selectAllCheckbox';
    }
  }


  /**
   * Override a method from `GridSelectionColumnBaseMixin` to handle the user
   * selecting all items.
   *
   * @protected
   * @override
   */
  _selectAll() {
    this.selectAll = true;
    this.$server.selectAll();
  }

  /**
   * Override a method from `GridSelectionColumnBaseMixin` to handle the user
   * deselecting all items.
   *
   * @protected
   * @override
   */
  _deselectAll() {
    this.selectAll = false;
    this.$server.deselectAll();
  }

  /**
   * Override a method from `GridSelectionColumnBaseMixin` to handle the user
   * selecting an item.
   *
   * @param {Object} item the item to select
   * @protected
   * @override
   */
  _selectItem(item) {
    this._grid.$connector.doSelection([item], true);
  }

  /**
   * Override a method from `GridSelectionColumnBaseMixin` to handle the user
   * deselecting an item.
   *
   * @param {Object} item the item to deselect
   * @protected
   * @override
   */
  _deselectItem(item) {
    this._grid.$connector.doDeselection([item], true);
    // Optimistically update select all state
    this.selectAll = false;
  }
}

customElements.define(GridFlowSelectionColumn.is, GridFlowSelectionColumn);
