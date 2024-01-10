/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.provider.DataChangeEvent;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract class that handles selection on data change.
 * <p>
 * Uses {@link SelectionPreservationMode} to switch between the selection
 * preservation modes.
 *
 * @see SelectionPreservationMode
 * @author Vaadin Ltd.
 */
public abstract class SelectionPreservationHandler<T> implements Serializable {

    private SelectionPreservationMode selectionPreservationMode;

    /**
     * Constructor taking in the initial selection preservation mode.
     *
     * @param selectionPreservationMode
     *            the selection preservation mode, not {@code null}
     */
    public SelectionPreservationHandler(
            SelectionPreservationMode selectionPreservationMode) {
        setSelectionPreservationMode(selectionPreservationMode);
    }

    /**
     * Sets the selection preservation mode on data change.
     *
     * @param selectionPreservationMode
     *            the selection preservation mode to switch to, not {@code null}
     *
     * @see SelectionPreservationMode
     */
    public final void setSelectionPreservationMode(
            SelectionPreservationMode selectionPreservationMode) {
        Objects.requireNonNull(selectionPreservationMode,
                "Selection preservation mode cannot be null.");
        this.selectionPreservationMode = selectionPreservationMode;
    }

    /**
     * Gets the selection preservation mode on data change.
     *
     * @return the selection preservation mode
     *
     * @see #setSelectionPreservationMode(SelectionPreservationMode)
     */
    public final SelectionPreservationMode getSelectionPreservationMode() {
        return selectionPreservationMode;
    }

    /**
     * Handles data change based on the current selection preservation mode.
     *
     * @param dataChangeEvent
     *            the data change event
     * @see #setSelectionPreservationMode(SelectionPreservationMode)
     */
    public final void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        switch (selectionPreservationMode) {
        case PRESERVE_ALL -> onPreserveAll(dataChangeEvent);
        case PRESERVE_EXISTING -> onPreserveExisting(dataChangeEvent);
        case DISCARD -> onDiscard(dataChangeEvent);
        }
    }

    /**
     * Preserves all selected items on data change.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onPreserveAll(DataChangeEvent<T> dataChangeEvent);

    /**
     * Preserves the selected items that still exist after data change.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onPreserveExisting(DataChangeEvent<T> dataChangeEvent);

    /**
     * Clears selection on data change.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onDiscard(DataChangeEvent<T> dataChangeEvent);
}
