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
package com.vaadin.flow.component.ai.tests.providers;

import java.util.List;

import com.vaadin.flow.component.ai.common.QueryLanguage;

/**
 * SQL in a named dialect as a {@link QueryLanguage}: the examples are plain
 * SQL, the name tells the LLM which product's SQL to write, and the rules add
 * what differs in that dialect.
 */
public final class SqlDialectQueryLanguage implements QueryLanguage {

    private final String name;
    private final String dialectRules;

    /**
     * Creates a dialect.
     *
     * @param name
     *            the name the LLM should see, for example {@code "PostgreSQL"}
     * @param dialectRules
     *            rules specific to the dialect, as lines starting with
     *            {@code "- "}, or {@code null}
     */
    public SqlDialectQueryLanguage(String name, String dialectRules) {
        this.name = name;
        this.dialectRules = dialectRules == null ? "" : dialectRules;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRules() {
        return dialectRules.isBlank() ? QueryLanguage.SQL.getRules()
                : QueryLanguage.SQL.getRules() + "\n" + dialectRules;
    }

    @Override
    public String exampleQuery(String source, List<String> columns,
            List<String> aliases) {
        return QueryLanguage.SQL.exampleQuery(source, columns, aliases);
    }
}
