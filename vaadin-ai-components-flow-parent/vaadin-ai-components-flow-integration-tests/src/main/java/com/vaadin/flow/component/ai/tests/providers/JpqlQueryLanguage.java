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
 * JPQL, the Jakarta Persistence Query Language, as a {@link QueryLanguage}:
 * queries over entities and their attributes, with result columns named by
 * {@code AS} aliases that must be identifiers.
 */
public final class JpqlQueryLanguage implements QueryLanguage {

    public static final JpqlQueryLanguage INSTANCE = new JpqlQueryLanguage();

    private static final String RULES = """
            - Write one JPQL SELECT over the entities and attributes listed by get_database_schema(); there are no table or column names
            - Alias every selected expression with AS; the alias becomes the result column name and must be an identifier (use underscores, never spaces, dots or quotes)
            - Navigate associations with dots (t.dependsOn.taskName) and aggregate with COUNT, SUM, AVG, MIN, MAX and GROUP BY
            - Selecting a whole entity (SELECT e FROM Employee e) returns its basic attributes as columns""";

    private JpqlQueryLanguage() {
    }

    @Override
    public String getName() {
        return "JPQL";
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
        var selectList = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            selectList.add("e." + columns.get(i) + " AS "
                    + toIdentifier(aliases.get(i)));
        }
        return "SELECT " + String.join(", ", selectList) + " FROM " + source
                + " e";
    }

    /**
     * Turns a result column name into a JPQL alias: anything but letters,
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
