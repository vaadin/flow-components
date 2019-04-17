window.Vaadin.Flow.gridProConnector = {
  setEditModeRenderer: function(column, component) {
      column.editModeRenderer = function(root) {
          root.appendChild(component);
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
  }
};
