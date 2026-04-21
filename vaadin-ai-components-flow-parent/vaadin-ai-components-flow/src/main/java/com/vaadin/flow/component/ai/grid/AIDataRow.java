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
