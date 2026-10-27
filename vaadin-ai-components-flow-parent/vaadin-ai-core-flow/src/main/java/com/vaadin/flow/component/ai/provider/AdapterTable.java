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
package com.vaadin.flow.component.ai.provider;

import java.io.Serializable;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;

/**
 * A table of a {@link FederatedDatabaseProvider} that one of Apache Calcite's
 * own adapters reads, for example its MongoDB adapter. Where a
 * {@link TableSource} hands rows to the engine, an adapter table takes part in
 * planning: whatever the adapter can push into its backend, filters, sorts,
 * limits or aggregates, it pushes there.
 * <p>
 * The provider calls {@link #createTable(SchemaPlus)} once, when it connects,
 * with the root schema of its connection, so that the adapter's own schema can
 * be registered there; the returned table joins the provider's schema under
 * {@link #getName()}.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public interface AdapterTable extends Serializable {

    /**
     * Returns the table name the LLM uses in SQL. Names are matched
     * case-insensitively.
     *
     * @return the table name, never {@code null}
     */
    String getName();

    /**
     * Returns a short description of where the rows come from, shown to the LLM
     * next to the table name. The default is {@code null}, no description.
     *
     * @return the description, or {@code null}
     */
    default String getDescription() {
        return null;
    }

    /**
     * Creates the Calcite table, registering whatever schema the adapter needs
     * under the root schema.
     *
     * @param rootSchema
     *            the root schema of the provider's connection, never
     *            {@code null}
     * @return the table, never {@code null}
     */
    Table createTable(SchemaPlus rootSchema);
}
