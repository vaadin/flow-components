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

import java.io.Serializable;
import java.util.List;

/**
 * The language an LLM writes its queries in for a
 * {@link com.vaadin.flow.component.ai.provider.QueryableProvider}. The AI
 * controllers put the language into the descriptions of their tools: they name
 * it, show its {@link #getRules() rules}, and render their examples with
 * {@link #exampleQuery(String, List, List)}, so the LLM learns the syntax of
 * the provider's language instead of SQL.
 * <p>
 * Tool descriptions are cached by the LLM providers, so a language must return
 * the same name, rules and examples on every call and must not carry anything
 * that changes from request to request.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public interface QueryLanguage extends Serializable {

    /**
     * SQL, the language of
     * {@link com.vaadin.flow.component.ai.provider.DatabaseProvider}. Its
     * examples are {@code SELECT} statements with {@code AS} aliases.
     */
    QueryLanguage SQL = new SqlQueryLanguage();

    /**
     * Returns the name of the language as the LLM should see it, for example
     * {@code "SQL"} or {@code "GraphQL"}.
     *
     * @return the language name, never {@code null}
     */
    String getName();

    /**
     * Returns rules for writing a query in this language, as a block of lines
     * for the LLM: how result columns are named, what the query must not do,
     * and other syntax the LLM needs to know. The rules must be the same on
     * every call.
     *
     * @return the rules, never {@code null}
     */
    String getRules();

    /**
     * Renders an example query that returns the given columns of a source under
     * the given result column names, in the syntax of this language. The tools
     * use it to show the LLM how to shape a query, so the example should be the
     * simplest query of this language that produces those columns.
     * <p>
     * For SQL, {@code exampleQuery("data", List.of("temperature", "sales"),
     * List.of("_x", "_y"))} renders
     * {@code SELECT temperature AS _x, sales AS _y FROM data}.
     * </p>
     *
     * @param source
     *            the table, collection or type the columns come from, not
     *            {@code null}
     * @param columns
     *            the columns or fields to return, at least one
     * @param aliases
     *            the result column names, one per column, in the same order
     * @return the example query, never {@code null}
     * @throws IllegalArgumentException
     *             if there are no columns or the lists differ in size
     */
    String exampleQuery(String source, List<String> columns,
            List<String> aliases);
}
