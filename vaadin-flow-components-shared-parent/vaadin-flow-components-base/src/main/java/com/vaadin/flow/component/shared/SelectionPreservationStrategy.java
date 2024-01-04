package com.vaadin.flow.component.shared;

/**
 * Represents selection preservation strategy on data change.
 * <p>
 * These enums should be used in
 * {@link DataChangeHandler#setSelectionPreservationStrategy(SelectionPreservationStrategy)}
 * to switch between the implemented selection preservation strategies.
 *
 * @see DataChangeHandler
 * @author Vaadin Ltd.
 */
public enum SelectionPreservationStrategy {

    /**
     * Selection preservation strategy that preserves all selected items on data
     * change.
     */
    PRESERVE_ALL,

    /**
     * Selection preservation strategy that only preserves the selected items
     * that still exist after data change.
     */
    PRESERVE_EXISTENT,

    /**
     * Selection preservation strategy that clears selection on data change.
     */
    DISCARD
}
