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
package com.vaadin.flow.component.ai.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * SQL as a {@link QueryLanguage}: {@code SELECT} statements whose result
 * columns are named with {@code AS} aliases. The single instance is
 * {@link QueryLanguage#SQL}.
 */
final class SqlQueryLanguage implements QueryLanguage {

    private static final Pattern PLAIN_IDENTIFIER = Pattern
            .compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final String RULES = """
            - Write one SELECT statement; never modify data
            - ALWAYS list specific columns — NEVER use SELECT *
            - Alias every column with AS; double-quote an alias with spaces or dots
            - Aggregate with GROUP BY, e.g. SELECT month AS category, SUM(revenue) AS value FROM sales GROUP BY month""";

    SqlQueryLanguage() {
    }

    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public String getRules() {
        return RULES;
    }

    @Override
    public String exampleQuery(String source, List<String> columns,
            List<String> aliases) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(columns, "columns must not be null");
        Objects.requireNonNull(aliases, "aliases must not be null");
        if (columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "An example query needs at least one column");
        }
        if (columns.size() != aliases.size()) {
            throw new IllegalArgumentException("Expected " + columns.size()
                    + " aliases for " + columns.size() + " columns, got "
                    + aliases.size());
        }
        var selectList = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            selectList.add(columns.get(i) + " AS " + quote(aliases.get(i)));
        }
        return "SELECT " + String.join(", ", selectList) + " FROM " + source;
    }

    private static String quote(String alias) {
        return PLAIN_IDENTIFIER.matcher(alias).matches() ? alias
                : "\"" + alias.replace("\"", "\"\"") + "\"";
    }

    /**
     * Keeps the language a single instance across serialization, so a
     * deserialized provider's language is still {@link QueryLanguage#SQL}.
     *
     * @return the shared instance
     */
    private Object readResolve() {
        return QueryLanguage.SQL;
    }
}
