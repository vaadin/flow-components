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

/**
 * Listener for {@link FieldValueChangeEvent}s fired by a
 * {@link FormAIController} after a successful AI turn. One event is delivered
 * per changed field, in document order.
 *
 * @since 25.3
 */
@FunctionalInterface
public interface FieldValueChangeListener extends Serializable {

    /**
     * Invoked once per changed field after the AI turn has settled.
     *
     * @param event
     *            the event describing the change, never {@code null}
     */
    void onFieldValueChange(FieldValueChangeEvent event);
}
