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

// Create a placeholder element to focus on when a custom editor is being loaded.
const editorPlaceholder = document.createElement('div');
editorPlaceholder.style.opacity = '0';
editorPlaceholder.tabIndex = -1;
editorPlaceholder.addEventListener('keydown', (e) => {
  if (!['Tab', 'Escape', 'Enter'].includes(e.key)) {
    // Power users might try yo hit Space, arrow keys, etc. before the actual editor is shown.
    // Cancel the events to avoid side effects like scrolling the page.
    e.preventDefault();
  }
});

window.Vaadin.Flow.gridProConnector = {
  selectAll: (editor, itemKey) => {
    if (editorPlaceholder.__itemKey !== itemKey) {
      // This is an outdated call from an earlier edit, don't unhide the editor yet.
      return;
    }

    // Remove the placeholder element
    editorPlaceholder.remove();
    // Unhide the updated editor component
    editor.style.removeProperty('visibility');
    editor.focus();

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
      component.style.visibility = 'hidden';
      component.after(editorPlaceholder);
      editorPlaceholder.focus();
      editorPlaceholder.__itemKey = rowData.item.key;
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
    column.isCellEditable = function (model) {
      // If there is no cell editable data, assume the cell is editable
      const isEditable = model.item.cellEditable && model.item.cellEditable[column._flowId];
      return isEditable === undefined || isEditable;
    };
  }
};
