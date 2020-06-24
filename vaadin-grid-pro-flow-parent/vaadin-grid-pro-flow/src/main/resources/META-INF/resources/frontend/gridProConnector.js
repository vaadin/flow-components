(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Grid Pro', 'vaadin-grid-pro-flow');
    };

    window.Vaadin.Flow.gridProConnector = {
        setEditModeRenderer: (column, component) => tryCatchWrapper(function (column, component) {
            column.editModeRenderer = tryCatchWrapper(function (root) {
                root.appendChild(component);
                this._grid._cancelStopEdit();
                component.focus();
            });

            // Not needed in case of custom editor as value is set on server-side.
            // Overridden in order to avoid blinking of the cell content.
            column._setEditorValue = function (editor, value) {};
            column._getEditorValue = function (editor) {
                return;
            };
        })(column, component),

        patchEditModeRenderer: column => tryCatchWrapper(function (column) {
            column.__editModeRenderer = tryCatchWrapper(function (root, column, rowData) {
                const cell = root.assignedSlot.parentNode;
                const grid = column._grid;

                if (grid.__edited && grid.__edited.model.item.key !== rowData.item.key) {
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
