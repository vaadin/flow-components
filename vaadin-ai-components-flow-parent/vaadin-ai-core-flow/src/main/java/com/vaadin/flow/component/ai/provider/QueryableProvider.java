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
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;

/**
 * A source of data an LLM queries on behalf of an AI controller: a database, a
 * document store, a GraphQL or REST API, or anything else that answers a query
 * written in some language with rows. The provider tells the LLM which
 * {@link QueryLanguage language} to write and describes the {@link #getSchema()
 * schema} it can refer to; the controllers hand the queries the LLM writes to
 * {@link #executeQuery(String)} exactly as written and render the rows in a
 * grid or chart. The rows themselves never go back to the LLM.
 * <p>
 * The query is passed through unmodified, so a provider only needs to
 * understand its own language. A grid asks for pages of a result with
 * {@link #executeQuery(String, QueryPage)} and for the size of the result with
 * {@link #count(String)}; the defaults serve any language by running the whole
 * query, which is fine for small results, and a provider whose backend can
 * page, sort and count on its own overrides them.
 * </p>
 * <p>
 * For a SQL database, implement {@link DatabaseProvider} instead: it is this
 * interface with the language, paging and counting already done for SQL.
 * </p>
 * <p>
 * <b>Security:</b> the LLM writes the queries. Connect with an account that can
 * only read, and only the data the LLM should see.
 * </p>
 * <p>
 * The example below queries a GraphQL API. Its language describes GraphQL to
 * the LLM, and the rows are the items of the single root field of the response,
 * flattened to column maps:
 * </p>
 *
 * <pre>
 * public class ProductsApiProvider implements QueryableProvider {
 *
 *     private static final QueryLanguage GRAPHQL = new GraphQLQueryLanguage();
 *
 *     &#064;Override
 *     public QueryLanguage getQueryLanguage() {
 *         return GRAPHQL;
 *     }
 *
 *     &#064;Override
 *     public String getSchema() {
 *         return schemaSdl; // the API's schema in SDL, plus notes
 *     }
 *
 *     &#064;Override
 *     public List&lt;Map&lt;String, Object&gt;&gt; executeQuery(String query) {
 *         var response = post(query); // {"data": {"products": [...]}}
 *         if (response.hasErrors()) {
 *             throw new ToolException(response.errorMessages());
 *         }
 *         return flatten(response.rootFieldItems());
 *     }
 * }
 * </pre>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public interface QueryableProvider extends Serializable {

    /**
     * Returns the language the LLM writes its queries in. The controllers put
     * its name, rules and examples into their tool descriptions, which the LLM
     * providers cache, so the language must be the same on every call for the
     * lifetime of the provider.
     *
     * @return the query language, never {@code null}
     */
    QueryLanguage getQueryLanguage();

    /**
     * Returns a text description of what the queries can refer to: the tables,
     * collections or types with their columns or fields and types, and any
     * notes that help writing correct queries. The LLM reads it as is, so keep
     * row values and anything sensitive out of it.
     *
     * @return the schema description, never {@code null}
     */
    String getSchema();

    /**
     * Executes a query the LLM wrote, exactly as written, and returns the rows.
     * Each row is a map from result column name to value, in column order.
     * <p>
     * Only allow reading. When a query fails, for example because it refers to
     * an unknown field, throw a {@link ToolException} whose message describes
     * the problem in terms safe to expose; the message is passed to the LLM so
     * it can correct the query instead of retrying it unchanged. Any other
     * exception is replaced with a generic error message before reaching the
     * LLM.
     * </p>
     *
     * @param query
     *            the query, in the language of {@link #getQueryLanguage()}, not
     *            {@code null}
     * @return the rows, never {@code null} but may be empty
     * @throws ToolException
     *             if the query fails and the LLM should learn why
     */
    List<Map<String, Object>> executeQuery(String query);

    /**
     * Returns one page of a query's rows: the rows from the page's offset, at
     * most its limit of them, ordered by its sort orders, which name result
     * columns of the query. A grid loads its rows through this method.
     * <p>
     * The default runs the whole query with {@link #executeQuery(String)} and
     * selects the page in memory with {@link QueryPage#apply(List)}. Override
     * it to page and sort in the backend when the results can be large.
     * </p>
     *
     * @param query
     *            the query, not {@code null}
     * @param page
     *            the page to return, not {@code null}
     * @return the rows of the page, never {@code null} but may be empty
     * @throws ToolException
     *             if the query fails and the LLM should learn why
     */
    default List<Map<String, Object>> executeQuery(String query,
            QueryPage page) {
        return page.apply(executeQuery(query));
    }

    /**
     * Returns the number of rows a query has, if the provider can tell without
     * fetching them. A grid with a known count shows a scrollbar for the whole
     * result; with an unknown count it loads pages until one comes back short.
     * <p>
     * The default returns an empty value, an unknown count.
     * </p>
     *
     * @param query
     *            the query, not {@code null}
     * @return the number of rows, or empty if unknown
     * @throws ToolException
     *             if the query fails and the LLM should learn why
     */
    default OptionalInt count(String query) {
        return OptionalInt.empty();
    }
}
