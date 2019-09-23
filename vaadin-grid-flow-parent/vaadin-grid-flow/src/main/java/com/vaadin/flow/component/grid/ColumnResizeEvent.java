/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.grid.Grid.Column;

import elemental.json.JsonObject;

/**
 * Event fired when a Grid column is resized by the user.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addColumnResizeListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("column-drag-resize")
public class ColumnResizeEvent<T> extends ComponentEvent<Grid<T>> {

    private final JsonObject columnWidths;
    private final JsonObject columnFlexGrows;
    private final String resizedColumnKey;

    /**
     * Creates a new column resize event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param columnWidths
     *            CSS width properties of all visible columns when resizing
     *            finished, mapped by their internal id
     * @param columnFlexGrows
     *            CSS flex-grow properties of all visible columns when resizing
     *            finished, mapped by their internal id
     * @param resizedColumnKey
     *            internal id of the column that was the target of user's resize
     *            action
     *
     */
    public ColumnResizeEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.columnWidths") JsonObject columnWidths,
            @EventData("event.detail.columnFlexGrows") JsonObject columnFlexGrows,
            @EventData("event.detail.resizedColumnKey") String resizedColumnKey) {
        super(source, fromClient);
        this.columnWidths = columnWidths;
        this.columnFlexGrows = columnFlexGrows;
        this.resizedColumnKey = resizedColumnKey;
    }

    /**
     * Returns the column that was the target of user's resize action
     * 
     * @return resize action target column
     */
    public Column<T> getResizedColumn() {
        return getSource().getColumns().stream()
                .filter(col -> col.getInternalId().equals(resizedColumnKey))
                .findFirst().orElse(null);
    }

    /**
     * Returns a map containing the CSS width properties of all visible columns
     * after the resize finished.
     * 
     * Note that resizing a single column can have side effects to the widths of
     * other columns.
     * 
     * @return CSS width properties of all visible columns
     */
    public Map<Column<T>, String> getColumnWidths() {
        return getSource().getColumns().stream().filter(Column::isVisible)
                .collect(Collectors.toMap(col -> col,
                        col -> columnWidths.getString(col.getInternalId())));
    }

    /**
     * Returns a map containing the CSS flex-grow properties of all visible
     * columns after the resize finished.
     * 
     * Note that resizing a single column can have side effects to the flex-grow
     * properties of other columns.
     * 
     * @return CSS flex-grow properties of all visible columns
     */
    public Map<Column<T>, Double> getColumnFlexGrows() {
        return getSource().getColumns().stream().filter(Column::isVisible)
                .collect(Collectors.toMap(col -> col,
                        col -> columnFlexGrows.getNumber(col.getInternalId())));
    }
}
