/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.provider.DataProvider;

/**
 * Represents selection preservation mode. Determines what happens with the
 * selection when {@link DataProvider#refreshAll} is called.
 * <p>
 * These enums should be used in
 * {@link SelectionPreservationHandler#setSelectionPreservationMode(SelectionPreservationMode)}
 * to switch between the implemented selection preservation modes.
 *
 * @see SelectionPreservationHandler
 * @author Vaadin Ltd.
 */
public enum SelectionPreservationMode {

    /**
     * Selection preservation mode that preserves all selected items when
     * {@link DataProvider#refreshAll} is called.
     */
    PRESERVE_ALL,

    /**
     * Selection preservation mode that only preserves the selected items that
     * still exist after {@link DataProvider#refreshAll} is called.
     */
    PRESERVE_EXISTING,

    /**
     * Selection preservation mode that clears selection when
     * {@link DataProvider#refreshAll} is called.
     */
    DISCARD
}
