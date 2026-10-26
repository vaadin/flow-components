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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.common.QueryLanguage;

/**
 * GraphQL as a {@link QueryLanguage}: a query is a selection set with one root
 * field, and result columns are named with field aliases. Aliases must be
 * identifiers, so a readable name becomes {@code Readable_Name}, which the grid
 * shows as "Readable Name". Dotted names cannot be expressed, so the grid's
 * column grouping is not available.
 */
public final class GraphQLQueryLanguage implements QueryLanguage {

    public static final GraphQLQueryLanguage INSTANCE = new GraphQLQueryLanguage();

    private static final String RULES = """
            - Write one query selecting exactly one field of the Query type; each item it returns becomes a row
            - Alias every selected field to name its result column, as in alias: field
            - An alias is an identifier (letters, digits, underscores): write Readable_Name for a readable header, never spaces, dots or quotes
            - Fields of nested objects become dotted column names (dependsOn.taskName); nested lists are joined into text
            - Only the arguments listed in the schema filter the data. There is no sorting or aggregation: select the fields and let the component arrange them""";

    private GraphQLQueryLanguage() {
    }

    @Override
    public String getName() {
        return "GraphQL";
    }

    @Override
    public String getRules() {
        return RULES;
    }

    @Override
    public String exampleQuery(String source, List<String> columns,
            List<String> aliases) {
        Objects.requireNonNull(source, "source must not be null");
        if (columns.isEmpty() || columns.size() != aliases.size()) {
            throw new IllegalArgumentException(
                    "An example needs at least one column and one alias per column");
        }
        var fields = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            fields.add(toIdentifier(aliases.get(i)) + ": " + columns.get(i));
        }
        return "{ " + source + " { " + String.join(" ", fields) + " } }";
    }

    /**
     * Turns a result column name into a GraphQL alias: anything but letters,
     * digits and underscores becomes an underscore.
     *
     * @param alias
     *            the wanted column name
     * @return the alias
     */
    public static String toIdentifier(String alias) {
        var identifier = alias.replaceAll("[^A-Za-z0-9_]", "_");
        return Character.isDigit(identifier.charAt(0)) ? "_" + identifier
                : identifier;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
