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

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.shared.Registration;

/**
 * Multiselection model interface for Grid.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of items in grid
 */
public interface GridMultiSelectionModel<T>
        extends GridSelectionModel<T>, SelectionModel.Multi<Grid<T>, T> {

    /**
     * State for showing the select all checkbox in the grid's default header
     * row for the selection column.
     * <p>
     * Default value is {@link #DEFAULT}, which means that the select all is
     * only visible if an in-memory data is used.
     */
    public enum SelectAllCheckboxVisibility {

        /**
         * Shows the select all checkbox, if in-memory data is used.
         * <p>
         * For lazy data, the checkbox is only shown when a count callback has
         * been provided. For lazy data with unknown count, the checkbox will
         * never be shown.
         * <p>
         * <b>For lazy data, selecting all will result in to all rows being
         * fetched from backend to application memory!</b>
         */
        VISIBLE,

        /**
         * Never shows the select all checkbox, regardless of data is in-memory
         * or not (lazy).
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
     *            the visiblity mode to use
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
     * provided.
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
     * Gets the the selection column's frozen state.
     *
     * @return whether the selection column is frozen
     */
    boolean isSelectionColumnFrozen();
}
