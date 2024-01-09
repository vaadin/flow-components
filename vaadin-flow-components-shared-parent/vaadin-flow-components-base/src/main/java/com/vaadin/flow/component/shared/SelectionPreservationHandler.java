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
 * Uses {@link SelectionPreservationStrategy} to switch between the selection
 * preservation strategies.
 *
 * @see SelectionPreservationStrategy
 * @author Vaadin Ltd.
 */
public abstract class SelectionPreservationHandler<T> implements Serializable {

    private SelectionPreservationStrategy selectionPreservationStrategy;

    /**
     * Constructor taking in the initial selection preservation strategy.
     *
     * @param selectionPreservationStrategy
     *            the selection preservation strategy, not {@code null}
     */
    public SelectionPreservationHandler(
            SelectionPreservationStrategy selectionPreservationStrategy) {
        setSelectionPreservationStrategy(selectionPreservationStrategy);
    }

    /**
     * Sets the selection preservation strategy on data change.
     *
     * @param selectionPreservationStrategy
     *            the selection preservation strategy to switch to, not
     *            {@code null}
     *
     * @see SelectionPreservationStrategy
     */
    public final void setSelectionPreservationStrategy(
            SelectionPreservationStrategy selectionPreservationStrategy) {
        Objects.requireNonNull(selectionPreservationStrategy,
                "Selection preservation strategy cannot be null.");
        this.selectionPreservationStrategy = selectionPreservationStrategy;
    }

    /**
     * Gets the selection preservation strategy on data change.
     *
     * @return the selection preservation strategy
     *
     * @see #setSelectionPreservationStrategy(SelectionPreservationStrategy)
     */
    public final SelectionPreservationStrategy getSelectionPreservationStrategy() {
        return selectionPreservationStrategy;
    }

    /**
     * Handles data change based on the current selection preservation strategy.
     *
     * @param dataChangeEvent
     *            the data change event
     * @see #setSelectionPreservationStrategy(SelectionPreservationStrategy)
     */
    public final void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        switch (selectionPreservationStrategy) {
        case PRESERVE_ALL -> onPreserveAll(dataChangeEvent);
        case PRESERVE_EXISTENT -> onPreserveExistent(dataChangeEvent);
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
    public abstract void onPreserveExistent(DataChangeEvent<T> dataChangeEvent);

    /**
     * Clears selection on data change.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onDiscard(DataChangeEvent<T> dataChangeEvent);
}
