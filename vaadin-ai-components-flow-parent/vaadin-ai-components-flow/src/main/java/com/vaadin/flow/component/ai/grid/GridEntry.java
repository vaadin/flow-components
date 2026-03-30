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
import com.vaadin.flow.internal.JacksonUtils;

/**
 * Holds the pending and current query state for an AI-managed grid. Entries are
 * stored directly on the {@link Grid} instance via {@link ComponentUtil}, so
 * their lifecycle is tied to the grid component and they survive serialization.
 *
 * @author Vaadin Ltd
 */
class GridEntry implements Serializable {

    private final String id;
    private String currentQuery;
    private String pendingQuery;

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
        var entry = get(grid);
        if (entry == null) {
            entry = new GridEntry(gridId);
            ComponentUtil.setData(grid, GridEntry.class, entry);
        }
        return entry;
    }

    /**
     * Returns the current state of the grid as a JSON string suitable for LLM
     * tool responses.
     *
     * @param grid
     *            the grid component, not {@code null}
     * @param gridId
     *            the grid ID
     * @return the state as a JSON string, never {@code null}
     */
    public static String getStateAsJson(Grid<?> grid, String gridId) {
        var node = JacksonUtils.createObjectNode();
        node.put("gridId", gridId);
        var entry = get(grid);
        if (entry == null || entry.currentQuery == null) {
            node.put("status", "empty");
            node.put("message", "No grid data has been loaded yet");
        } else {
            node.put("query", entry.currentQuery);
        }
        return node.toString();
    }

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
     * Returns the current query that was last successfully rendered, or
     * {@code null} if no query has been applied yet.
     *
     * @return the current SQL query, or {@code null}
     */
    public String getCurrentQuery() {
        return currentQuery;
    }

    /**
     * Sets the current query after successful rendering.
     *
     * @param query
     *            the SQL query
     */
    public void setCurrentQuery(String query) {
        this.currentQuery = query;
    }

    /**
     * Returns the pending query waiting to be rendered, or {@code null} if
     * none.
     *
     * @return the pending SQL query, or {@code null}
     */
    public String getPendingQuery() {
        return pendingQuery;
    }

    /**
     * Sets a pending query to be rendered.
     *
     * @param query
     *            the SQL query
     */
    public void setPendingQuery(String query) {
        this.pendingQuery = query;
    }

    /**
     * Returns whether this entry has a pending query waiting to be applied.
     *
     * @return {@code true} if there is a pending query
     */
    public boolean hasPendingState() {
        return pendingQuery != null;
    }

    /**
     * Clears the pending query.
     */
    public void clearPendingState() {
        pendingQuery = null;
    }
}
