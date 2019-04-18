window.Vaadin.Flow.gridProConnector = {
  setEditModeRenderer: function(column, component) {
      column.editModeRenderer = function(root) {
          root.appendChild(component);
          this._grid._cancelStopEdit();
          component.focus();
      };

      column._setEditorValue = function(editor, value) {
          // Not needed in case of custom editor as value is set on server-side.
          // Overridden in order to avoid blinking of the cell content.
      }

      column._getEditorValue = function(editor) {
          // Not needed in case of custom editor as value is set on server-side.
          // Overridden in order to avoid blinking of the cell content.
          return;
      }
  },

  patchEditModeRenderer: function(column) {
      column.__editModeRenderer = function(root, column, rowData) {
          const cell = root.assignedSlot.parentNode;
          const grid = column._grid;

          if (grid.__edited && grid.__edited.model.item.key !== rowData.item.key) {
              grid._stopEdit();
              return;
          }

          const tagName = column._getEditorTagName(cell);
          if (!root.firstElementChild || root.firstElementChild.localName.toLowerCase() !== tagName) {
              root.innerHTML = `
              <${tagName}></${tagName}>
            `;
          }
      };
  }
};
