/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Event fired when the columns in the Grid are reordered.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addColumnReorderListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("column-reorder-all-columns")
public class ColumnReorderEvent<T> extends ComponentEvent<Grid<T>> {

    /**
     * The new order of the columns. Unmodifiable.
     */
    private final List<Grid.Column<T>> columns;

    /**
     * Creates a new column reorder event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param columnIDs
     *            the internal column IDs; automatically translated to proper
     *            Grid Column instances.
     *
     */
    public ColumnReorderEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.columns") JsonArray columnIDs) {
        this(source, fromClient,
                getSortedByIds(source.getColumns(), columnIDs));
    }

    /**
     * Creates a new column reorder event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param columns
     *            the newly ordered Grid columns. Not null, may be empty.
     *
     */
    public ColumnReorderEvent(Grid<T> source, boolean fromClient,
            List<Grid.Column<T>> columns) {
        super(source, fromClient);
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns)); // defensive
                                                                               // copy
    }

    /**
     * Gets the new order of the columns.
     *
     * @return the list of columns, not null, unmodifiable.
     */
    public List<Grid.Column<T>> getColumns() {
        return columns;
    }

    private static <T> List<Grid.Column<T>> getSortedByIds(
            List<Grid.Column<T>> currentColumns, JsonArray columnIDs) {
        final List<Grid.Column<T>> columns = new ArrayList<>(
                currentColumns.size());
        for (int i = 0; i < columnIDs.length(); i++) {
            final String columnID = columnIDs.getString(i);
            columns.add(findByColumnId(currentColumns, columnID));
        }
        return columns;
    }

    private static <T> Grid.Column<T> findByColumnId(
            List<Grid.Column<T>> columns, String id) {
        return columns.stream().filter(it -> id.equals(it.getInternalId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "No column with ID " + id));
    }
}
