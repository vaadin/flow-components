/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.shared.Registration;

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
}
