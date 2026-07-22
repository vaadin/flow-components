/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.grid;

import java.io.Serializable;

/**
 * Serializable grid state for persistence across sessions. Captured via
 * {@link GridAIController#getState()} and restored via
 * {@link GridAIController#restoreState(GridState)}.
 *
 * @param query
 *            the SQL query that populates the grid
 * @author Vaadin Ltd
 * @since 25.2
 */
public record GridState(String query) implements Serializable {
}
