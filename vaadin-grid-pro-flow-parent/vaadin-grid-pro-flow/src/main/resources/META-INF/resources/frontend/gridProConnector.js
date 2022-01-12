(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Grid Pro', 'vaadin-grid-pro-flow');
    };

    function isEditedRow(grid, rowData) {
        return grid.__edited && grid.__edited.model.item.key === rowData.item.key;
    }

    window.Vaadin.Flow.gridProConnector = {
        initLazy: grid => tryCatchWrapper(function(grid) {
            grid.$connector.editCell = tryCatchWrapper(function(row, col, userOriginated) {
                if (!row || !col || (userOriginated && grid.hasAttribute('disabled'))) {
                    throw new Error('Invalid row/col or grid is disabled.')
                }

                const columns = grid._getColumns().filter(col => !col.hidden)

                let colIdx = -1
                if(isNaN(col)) { //If col is not a number, maybe it's because the id was passed instead
                    const matchingColumn = columns.filter(c => c.id == col)
                    if(!matchingColumn || matchingColumn.length == 0) {
                        throw new Error(`col with id ${col} was not found`)
                    }
                    colIdx = columns.indexOf(matchingColumn[0])
                } else {
                    colIdx = col
                }

                let rowIdx = -1
                if(isNaN(row)) { //If row is not a number, maybe it's because the item was passed instead
                    if(!row.hasOwnProperty('key')) {
                        throw new Error('Invalid object passed as row item.')
                    }
                    rowIdx = row.key - 1
                } else {
                    rowIdx = row
                }

                // Make sure col[colIdx] exists and is editable:
                if(colIdx > columns.length) {
                    throw new Error('Invalid colIdx (out of bounds)')
                }

                const column = columns[colIdx]
                if(!grid._isEditColumn(column)) {
                    throw new Error('Column is not editable')
                }

                // Get rows (excluding header)
                const tRows = grid.$.table.getElementsByTagName('tbody').items.rows


                // Make sure row[rowIdx] exists
                if(rowIdx > tRows.length) {
                    throw new Error('Invalid rowIdx (out of bounds)')
                }

                grid._startEdit(tRows[rowIdx].cells[colIdx], column)
            });

        }),

        setEditModeRenderer: (column, component) => tryCatchWrapper(function (column, component) {
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
            });

            // Not needed in case of custom editor as value is set on server-side.
            // Overridden in order to avoid blinking of the cell content.
            column._setEditorValue = function (editor, value) {};
            column._getEditorValue = function (editor) {
                return;
            };
        })(column, component),

        patchEditModeRenderer: column => tryCatchWrapper(function (column) {
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
