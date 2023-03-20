/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.shared.Registration;

/**
 * Selection model implementation for disabling selection in Grid.
 *
 * @param <T>
 *            the grid bean type
 */
public class GridNoneSelectionModel<T> implements GridSelectionModel<T> {

    @Override
    public Set<T> getSelectedItems() {
        return Collections.emptySet();
    }

    @Override
    public Optional<T> getFirstSelectedItem() {
        return Optional.empty();
    }

    @Override
    public void select(T item) {
        // NO-OP
    }

    @Override
    public void deselect(T item) {
        // NO-OP
    }

    @Override
    public void deselectAll() {
        // NO-OP
    }

    @Override
    public void selectFromClient(T item) {
        throw new IllegalStateException("Client tried to update selection"
                + " even though selection mode is currently set to NONE.");
    }

    @Override
    public void deselectFromClient(T item) {
        throw new IllegalStateException("Client tried to update selection"
                + " even though selection mode is currently set to NONE.");
    }

    @Override
    public Registration addSelectionListener(
            SelectionListener<Grid<T>, T> listener) {
        throw new UnsupportedOperationException(
                "This selection model doesn't allow selection, cannot add selection listeners to it. "
                        + "Please set suitable selection mode with grid.setSelectionMode");
    }
}
