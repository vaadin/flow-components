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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single row of data in an AI-managed grid. Row instances are
 * created internally by the framework when rendering query results and are not
 * intended to be constructed or inspected by application code. This type exists
 * to make the {@link GridAIController}'s grid type parameter explicit
 * ({@code Grid<AIDataRow>}) and to signal that row data is framework-owned.
 *
 * @author Vaadin Ltd
 * @see GridAIController
 * @see GridRenderer
 * @since 25.2
 */
public final class AIDataRow implements Serializable {

    private final Map<String, Object> values;

    /**
     * Creates a new row from the given column-value map. The map is copied
     * defensively; later mutations to the source map do not affect the row.
     *
     * @param values
     *            the column values keyed by column name, not {@code null}
     */
    AIDataRow(Map<String, Object> values) {
        Objects.requireNonNull(values, "values must not be null");
        this.values = new LinkedHashMap<>(values);
    }

    /**
     * Returns the value for the given column.
     *
     * @param column
     *            the column name
     * @return the value, or {@code null} if the column is not present or its
     *         value is {@code null}
     */
    Object get(String column) {
        return values.get(column);
    }

    /**
     * Returns the column-value entries in this row, in insertion order.
     *
     * @return the entries, never {@code null}
     */
    Set<Map.Entry<String, Object>> entries() {
        return values.entrySet();
    }
}
