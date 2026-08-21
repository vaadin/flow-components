/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.io.Serializable;

import com.vaadin.flow.component.Component;

/**
 * Creates the extra content a {@link FormAIController} shows in the popover of
 * the AI field marker, between the explanation message and the revert control.
 * Register it through
 * {@link FormAIController#setFieldMarkerContentProvider(FieldMarkerContentProvider)}.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
@FunctionalInterface
public interface FieldMarkerContentProvider extends Serializable {

    /**
     * Creates the content component for one field the controller is marking.
     * Invoked once per field whose value changed during a successful AI turn,
     * with the same event the
     * {@link FormAIController#addFieldValueChangeListener(FieldValueChangeListener)
     * field-value-change listeners} receive.
     *
     * @param change
     *            the change the mark annotates, never {@code null}
     * @return the component to show in the marker's popover, or {@code null}
     *         for no extra content
     */
    Component createContent(FieldValueChangeEvent change);
}
