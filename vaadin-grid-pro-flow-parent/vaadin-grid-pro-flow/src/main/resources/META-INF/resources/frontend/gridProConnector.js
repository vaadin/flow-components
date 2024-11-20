/**
 * @license
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
const LOADING_EDITOR_CELL_ATTRIBUTE = 'loading-editor';

(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Grid Pro');
  };

  function isEditedRow(grid, rowData) {
    return grid.__edited && grid.__edited.model.item.key === rowData.item.key;
  }

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
    setEditModeRenderer: (column, component) =>
      tryCatchWrapper(function (column, component) {
        column.editModeRenderer = tryCatchWrapper(function editModeRenderer(root, _, rowData) {
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
        });

        // Not needed in case of custom editor as value is set on server-side.
        // Overridden in order to avoid blinking of the cell content.
        column._setEditorValue = function (editor, value) {};

        const stopCellEdit = column._stopCellEdit;
        column._stopCellEdit = function () {
          stopCellEdit.apply(this, arguments);
          this._grid.toggleAttribute(LOADING_EDITOR_CELL_ATTRIBUTE, false);
        };
      })(column, component),

    patchEditModeRenderer: (column) =>
      tryCatchWrapper(function (column) {
        column.__editModeRenderer = tryCatchWrapper(function __editModeRenderer(root, column, rowData) {
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
        });
      })(column)
  };
})();
