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

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Single selection model interface for Grid.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of items in grid
 */
public interface GridSingleSelectionModel<T>
        extends GridSelectionModel<T>, SelectionModel.Single<Grid<T>, T> {

    /**
     * Gets a wrapper to use this single selection model as a single select in
     * {@link Binder}.
     *
     * @return the single select wrapper
     */
    SingleSelect<Grid<T>, T> asSingleSelect();

    /**
     * Adds a selection listener that will be called when the selection is
     * changed either by the user or programmatically.
     *
     * @param listener
     *            the single selection listener, not {@code null}
     * @return a registration for the listener
     */
    Registration addSingleSelectionListener(
            SingleSelectionListener<Grid<T>, T> listener);

    /**
     * Returns a {@link ValueSignal} representing the currently selected item.
     * <p>
     * The returned signal is kept synchronized with the grid's selection: reads
     * reflect the current selection, and writes update it. The signal is
     * created lazily and cached — subsequent calls return the same instance.
     * <p>
     * This method cannot be used if a signal has already been bound to the
     * selection value via
     * {@link SingleSelect#bindValue(com.vaadin.flow.signals.Signal, com.vaadin.flow.function.SerializableConsumer)
     * asSingleSelect().bindValue(...)}.
     *
     * @return a value signal representing the selected item
     * @throws com.vaadin.flow.signals.BindingActiveException
     *             if a signal has already been bound via {@code bindValue()}
     * @since 25.1
     */
    ValueSignal<T> getSelectedItemSignal();
}
