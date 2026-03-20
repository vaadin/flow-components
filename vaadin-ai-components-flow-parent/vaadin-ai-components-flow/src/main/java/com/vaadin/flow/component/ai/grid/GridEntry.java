/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.grid;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid;

/**
 * Holds the data source query and pending LLM state for a grid. Grid entries
 * are stored directly on the {@link Grid} instance via {@link ComponentUtil},
 * so their lifecycle is tied to the grid component.
 *
 * @author Vaadin Ltd
 */
public class GridEntry implements Serializable {

    /**
     * Gets the {@link GridEntry} for the given grid, or {@code null} if none
     * has been set.
     *
     * @param grid
     *            the grid component, not {@code null}
     * @return the grid entry, or {@code null}
     */
    public static GridEntry get(Grid<?> grid) {
        return ComponentUtil.getData(grid, GridEntry.class);
    }

    /**
     * Gets the {@link GridEntry} for the given grid, creating one if it does
     * not exist.
     *
     * @param grid
     *            the grid component, not {@code null}
     * @param gridId
     *            the grid ID to assign if a new entry is created
     * @return the grid entry, never {@code null}
     */
    public static GridEntry getOrCreate(Grid<?> grid, String gridId) {
        GridEntry entry = ComponentUtil.getData(grid, GridEntry.class);
        if (entry == null) {
            entry = new GridEntry(gridId);
            ComponentUtil.setData(grid, GridEntry.class, entry);
        }
        return entry;
    }

    private final String id;
    private String query;
    private boolean pendingDataUpdate;

    /**
     * Creates a new grid entry with the given ID.
     *
     * @param id
     *            the grid ID, not {@code null}
     */
    public GridEntry(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Returns the grid ID.
     *
     * @return the grid ID, never {@code null}
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current SQL query for this grid's data.
     *
     * @return the SQL query, or {@code null} if no query has been set
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the SQL query for this grid's data.
     *
     * @param query
     *            the SQL query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Returns whether a data update is pending.
     *
     * @return {@code true} if the query was changed and the grid needs
     *         re-rendering
     */
    public boolean isPendingDataUpdate() {
        return pendingDataUpdate;
    }

    /**
     * Marks or clears the pending data update flag.
     *
     * @param pendingDataUpdate
     *            {@code true} if the grid data needs re-rendering
     */
    public void setPendingDataUpdate(boolean pendingDataUpdate) {
        this.pendingDataUpdate = pendingDataUpdate;
    }

    /**
     * Returns whether this entry has pending state waiting to be applied.
     *
     * @return {@code true} if there is a pending data update
     */
    public boolean hasPendingState() {
        return pendingDataUpdate;
    }

    /**
     * Clears all pending state.
     */
    public void clearPendingState() {
        pendingDataUpdate = false;
    }
}
