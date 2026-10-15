/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * Abstract class that handles selection when {@link DataProvider#refreshAll} is
 * called.
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
     * Sets the selection preservation mode. Determines what happens with the
     * selection when {@link DataProvider#refreshAll} is called.
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
     * Gets the selection preservation mode.
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
     * Preserves all selected items when {@link DataProvider#refreshAll} is
     * called.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onPreserveAll(DataChangeEvent<T> dataChangeEvent);

    /**
     * Preserves the selected items that still exist after
     * {@link DataProvider#refreshAll} is called.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onPreserveExisting(DataChangeEvent<T> dataChangeEvent);

    /**
     * Clears selection when {@link DataProvider#refreshAll} is called.
     *
     * @param dataChangeEvent
     *            the data change event
     */
    public abstract void onDiscard(DataChangeEvent<T> dataChangeEvent);
}
