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
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.data.selection.MultiSelectionEvent;

/**
 * Event fired when the user toggles the selection state of an item on the
 * client-side.
 * <p>
 * This event follows {@link MultiSelectionEvent} and provides details about the
 * item that was toggled, its new selection state, and whether the shift key was
 * pressed during the selection. This can be helpful for implementing features
 * like range selection.
 *
 * @param <T>
 *            the grid bean type
 * @author Vaadin Ltd
 * @see GridMultiSelectionModel#addClientItemToggleListener(com.vaadin.flow.component.ComponentEventListener)
 */
public class ClientItemToggleEvent<T> extends ComponentEvent<Grid<T>> {
    private final T item;
    private final boolean isSelected;
    private final boolean isShiftKey;

    /**
     * Creates a new item toggle event.
     *
     * @param source
     *            the source component
     * @param item
     *            the item that was toggled
     * @param isSelected
     *            {@code true} if the item was selected, {@code false} otherwise
     * @param isShiftKey
     *            {@code true} if the shift key was pressed when the item was
     *            toggled
     */
    public ClientItemToggleEvent(Grid<T> source, T item, boolean isSelected,
            boolean isShiftKey) {
        super(source, true);
        this.item = item;
        this.isSelected = isSelected;
        this.isShiftKey = isShiftKey;
    }

    /**
     * Gets the item that was toggled.
     *
     * @return the item that was toggled
     */
    public T getItem() {
        return item;
    }

    /**
     * Gets whether the item was selected.
     *
     * @return {@code true} if the item was selected, {@code false} if the item
     *         was deselected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Gets whether the shift key was pressed when the item was toggled.
     *
     * @return {@code true} if the shift key was pressed, {@code false}
     *         otherwise
     */
    public boolean isShiftKey() {
        return isShiftKey;
    }
}
