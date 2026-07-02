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
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Event fired when a {@link Grid.Column column's} visibility changes, either
 * programmatically through {@link Grid.Column#setVisible(boolean)} or when the
 * user shows or hides the column through the grid's column toggle menu.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid.Column#addVisibilityChangedListener(com.vaadin.flow.component.ComponentEventListener)
 */
public class ColumnVisibilityChangedEvent<T>
        extends ComponentEvent<Grid.Column<T>> {

    private final boolean visible;

    /**
     * Creates a new column visibility change event.
     *
     * @param source
     *            the column whose visibility changed
     * @param fromClient
     *            <code>true</code> if the change originated from the client
     *            (the user toggled the column in a column selector),
     *            <code>false</code> if it originates from server-side logic
     * @param visible
     *            the new visibility of the column
     */
    public ColumnVisibilityChangedEvent(Grid.Column<T> source,
            boolean fromClient, boolean visible) {
        super(source, fromClient);
        this.visible = visible;
    }

    /**
     * Gets the column whose visibility changed.
     *
     * @return the column
     */
    public Grid.Column<T> getColumn() {
        return getSource();
    }

    /**
     * Gets the new visibility of the column.
     *
     * @return <code>true</code> if the column is now visible,
     *         <code>false</code> otherwise
     */
    public boolean isVisible() {
        return visible;
    }
}
