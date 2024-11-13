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

window.Vaadin.Flow.gridProConnector = {
  selectAll: (editor) => {
    if (editor instanceof HTMLInputElement) {
      editor.select();
    } else if (editor.focusElement && editor.focusElement instanceof HTMLInputElement) {
      editor.focusElement.select();
    }

    // Unhide the updated editor component
    editor.style.removeProperty('opacity');
    editor.style.removeProperty('pointer-events');
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

      // Hide the editor component until it is updated with the correct value (after a server roundtrip)
      // Using "visibility" or "display" is not an option as it would prevent the component from being focused.
      // Let's use "opacity" and "pointer-events" as the best compromise.
      component.style.opacity = '0';
      component.style.pointerEvents = 'none';

      root.appendChild(component);
      this._grid._cancelStopEdit();
      component.focus();
    };

    // Not needed in case of custom editor as value is set on server-side.
    // Overridden in order to avoid blinking of the cell content.
    column._setEditorValue = function (editor, value) {};
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
