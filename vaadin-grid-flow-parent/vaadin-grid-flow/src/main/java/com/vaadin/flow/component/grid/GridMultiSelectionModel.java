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

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Multiselection model interface for Grid.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of items in grid
 * @since 1.0
 */
public interface GridMultiSelectionModel<T>
        extends GridSelectionModel<T>, SelectionModel.Multi<Grid<T>, T> {

    /**
     * State for showing the select all checkbox in the grid's default header
     * row for the selection column.
     * <p>
     * Default value is {@link #DEFAULT}, which means that the select all
     * checkbox is only visible for in-memory data.
     */
    public enum SelectAllCheckboxVisibility {

        /**
         * Shows the select all checkbox for in-memory data, and for lazy data
         * with a known item count, for example when a count callback is
         * provided. For lazy data with unknown item count, the checkbox is
         * never shown.
         * <p>
         * <b>For lazy data, selecting all fetches all rows from the backend
         * into application memory!</b>
         */
        VISIBLE,

        /**
         * Never shows the select all checkbox, regardless of whether the data
         * is in-memory or lazy.
         */
        HIDDEN,

        /**
         * By default, the visibility of the select all checkbox depends on how
         * the Grid's items are fetched:
         * <ul>
         * <li>Visible, if the data is in-memory</li>
         * <li>Hidden, if the data is NOT in-memory (lazy)</li>
         * </ul>
         */
        DEFAULT;
    }

    /**
     * Gets a wrapper to use this multiselection model as a multiselect in
     * {@link Binder}.
     *
     * @return the multiselect wrapper
     */
    MultiSelect<Grid<T>, T> asMultiSelect();

    /**
     * Adds a selection listener that will be called when the selection is
     * changed either by the user or programmatically.
     *
     * @param listener
     *            the multi selection listener, not {@code null}
     * @return a registration for the listener
     */
    Registration addMultiSelectionListener(
            MultiSelectionListener<Grid<T>, T> listener);

    /**
     * Adds a client item toggle listener that will be called when the user
     * toggles the selection state of an item on the client-side.
     * <p>
     * This event follows {@link MultiSelectionEvent} and provides details about
     * the item that was toggled, its new selection state, and whether the shift
     * key was pressed during the selection. This can be helpful for
     * implementing features like range selection.
     *
     * @param listener
     *            the client item toggle listener, not {@code null}
     * @return a registration for the listener
     * @since 24.7
     */
    Registration addClientItemToggleListener(
            ComponentEventListener<ClientItemToggleEvent<T>> listener);

    /**
     * Sets the select all checkbox visibility mode.
     * <p>
     * The default value is {@link SelectAllCheckboxVisibility#DEFAULT}, which
     * means that the checkbox is only visible if the grid's data provider is
     * in-memory.
     * <p>
     * The select all checkbox will never be shown if the Grid uses lazy loading
     * with unknown item count, i.e. no items count query provided to it, and
     * even setting {@link SelectAllCheckboxVisibility#VISIBLE} won't make it
     * visible.
     *
     * @param selectAllCheckBoxVisibility
     *            the visibility mode to use
     * @see SelectAllCheckboxVisibility
     */
    void setSelectAllCheckboxVisibility(
            SelectAllCheckboxVisibility selectAllCheckBoxVisibility);

    /**
     * Gets the current mode for the select all checkbox visibility.
     *
     * @return the select all checkbox visibility mode
     * @see SelectAllCheckboxVisibility
     * @see #isSelectAllCheckboxVisible()
     */
    SelectAllCheckboxVisibility getSelectAllCheckboxVisibility();

    /**
     * Returns whether the select all checkbox will be visible with the current
     * setting of
     * {@link #setSelectAllCheckboxVisibility(SelectAllCheckboxVisibility)} and
     * the type of data set to the Grid (in-memory or lazy).
     * <p>
     * The select all checkbox will never be shown if the Grid uses lazy loading
     * with unknown item count, meaning that no count callback has been
     * provided. It will also not be shown if the grid is configured to use
     * conditional selection via
     * {@link Grid#setItemSelectableProvider(SerializablePredicate)}
     *
     * @return {@code true} if the checkbox will be visible with the current
     *         settings
     * @see SelectAllCheckboxVisibility
     * @see #setSelectAllCheckboxVisibility(SelectAllCheckboxVisibility)
     */
    boolean isSelectAllCheckboxVisible();

    /**
     * Sets the selection column's frozen state.
     *
     * @param frozen
     *            whether to freeze or unfreeze the selection column
     */
    void setSelectionColumnFrozen(boolean frozen);

    /**
     * Gets the selection column's frozen state.
     *
     * @return whether the selection column is frozen
     */
    boolean isSelectionColumnFrozen();

    /**
     * If <code>true</code>, grid rows can be selected or deselected by dragging
     * the mouse cursor over grid's selection column.
     *
     * @param dragSelect
     *            <code>true</code> to enable drag select feature,
     *            <code>false</code> for disabling it
     * @since 24.2
     */
    void setDragSelect(boolean dragSelect);

    /**
     * Gets whether grid drag select is enabled or not.
     *
     * @return <code>true</code> if drag select feature is enabled,
     *         <code>false</code> otherwise
     * @since 24.2
     */
    boolean isDragSelect();

    /**
     * Gets whether a click on a row toggles the selection of the row's item.
     * <p>
     * The default implementation throws an
     * {@link UnsupportedOperationException}. The selection models that Grid
     * uses override it.
     *
     * @return {@code true} if a click on a row toggles the selection,
     *         {@code false} otherwise
     * @throws UnsupportedOperationException
     *             if the implementation does not support this feature
     * @see #setSelectOnItemClick(boolean)
     * @since 25.4
     */
    default boolean isSelectOnItemClick() {
        throw new UnsupportedOperationException(
                "Select on item click is not supported by this selection model");
    }

    /**
     * Sets whether a click on a row toggles the selection of the row's item.
     * When enabled, you can select and deselect items with a click anywhere in
     * the row, not only with the checkbox in the selection column.
     * <ul>
     * <li>A click on an item that is not selected selects it. A click on a
     * selected item deselects it.</li>
     * <li><kbd>Space</kbd> on a focused row or cell counts as a click.</li>
     * <li>A click on focusable content in a cell, such as a button or a text
     * field, does not change the selection.</li>
     * <li>The selection of items that
     * {@link Grid#setItemSelectableProvider(SerializablePredicate)} does not
     * allow does not change.</li>
     * </ul>
     * The default value is {@code false}.
     * <p>
     * The default implementation throws an
     * {@link UnsupportedOperationException}. The selection models that Grid
     * uses override it.
     *
     * @param selectOnItemClick
     *            {@code true} to toggle the selection on a click on a row,
     *            {@code false} to not change the selection on a click on a row
     * @throws UnsupportedOperationException
     *             if the implementation does not support this feature
     * @since 25.4
     */
    default void setSelectOnItemClick(boolean selectOnItemClick) {
        throw new UnsupportedOperationException(
                "Select on item click is not supported by this selection model");
    }
}
