/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ai.common.QueryLanguage;

/**
 * A GraphQL-shaped query language for tests of the controllers' language
 * neutrality: its examples are selection sets with field aliases and contain no
 * SQL.
 */
public final class FakeGraphQLLanguage implements QueryLanguage {

    public static final String NAME = "GraphQL";

    public static final String RULES = """
            - Select exactly one root field
            - Alias every field as alias: field""";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRules() {
        return RULES;
    }

    @Override
    public String exampleQuery(String source, List<String> columns,
            List<String> aliases) {
        var fields = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            fields.add(aliases.get(i) + ": " + columns.get(i));
        }
        return "{ " + source + " { " + String.join(" ", fields) + " } }";
    }
}
