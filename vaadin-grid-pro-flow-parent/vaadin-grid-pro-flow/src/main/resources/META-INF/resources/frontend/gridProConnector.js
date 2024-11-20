/**
 * @license
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
function isEditedRow(grid, rowData) {
  return grid.__edited && grid.__edited.model.item.key === rowData.item.key;
}

const LOADING_EDITOR_CELL_ATTRIBUTE = 'loading-editor';

window.Vaadin.Flow.gridProConnector = {
  selectAll: (editor, itemKey, grid) => {
    if (editor.__itemKey !== itemKey) {
      // This is an outdated call that can occur if the user starts editing a cell,
      // and quickly starts editing another cell on the same column before the editor
      // is unhidden for the first cell.
      return;
    }

    grid.toggleAttribute(LOADING_EDITOR_CELL_ATTRIBUTE, false);

    if (editor instanceof HTMLInputElement) {
      editor.select();
    } else if (editor.focusElement && editor.focusElement instanceof HTMLInputElement) {
      editor.focusElement.select();
    }
  },

  setEditModeRenderer(column, component) {
    column.editModeRenderer = function editModeRenderer(root, _, rowData) {
      if (!isEditedRow(this._grid, rowData)) {
        this._grid._stopEdit();
        return;
      }

      if (component.parentNode === root) {
        return;
      }

      root.appendChild(component);
      this._grid._cancelStopEdit();
      component.focus();

      component.__itemKey = rowData.item.key;
      this._grid.toggleAttribute(LOADING_EDITOR_CELL_ATTRIBUTE, true);
    };

    // Not needed in case of custom editor as value is set on server-side.
    // Overridden in order to avoid blinking of the cell content.
    column._setEditorValue = function (editor, value) {};

    const stopCellEdit = column._stopCellEdit;
    column._stopCellEdit = function () {
      stopCellEdit.apply(this, arguments);
      this._grid.toggleAttribute(LOADING_EDITOR_CELL_ATTRIBUTE, false);
    };
  },

  patchEditModeRenderer(column) {
    column.__editModeRenderer = function __editModeRenderer(root, column, rowData) {
      const cell = root.assignedSlot.parentNode;
      const grid = column._grid;

      if (!isEditedRow(grid, rowData)) {
        grid._stopEdit();
        return;
      }

      const tagName = column._getEditorTagName(cell);
      if (!root.firstElementChild || root.firstElementChild.localName.toLowerCase() !== tagName) {
        root.innerHTML = `<${tagName}></${tagName}>`;
      }
    };
  },

  initCellEditableProvider(column) {
    column.isCellEditable = function(model) {
      // If there is no cell editable data, assume the cell is editable
      const isEditable = model.item.cellEditable && model.item.cellEditable[column._flowId];
      return isEditable === undefined || isEditable;
    };
  },
};
