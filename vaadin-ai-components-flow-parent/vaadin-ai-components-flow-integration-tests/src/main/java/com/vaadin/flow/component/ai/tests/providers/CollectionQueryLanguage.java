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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * The query language of the collection-like endpoints, see
 * {@link CollectionQuery}. It deliberately promises the LLM no more than the
 * endpoint delivers: fields of one source under chosen names, equality filters
 * and sorting; no joins, aggregation or expressions. The {@link #REST} and
 * {@link #CSV} instances differ only in what they call a source.
 */
public final class CollectionQueryLanguage implements QueryLanguage {

    /** The language of a REST API whose resources are collections. */
    public static final CollectionQueryLanguage REST = new CollectionQueryLanguage(
            "REST query", "collection", "the REST API");

    /** The language of a directory of CSV files. */
    public static final CollectionQueryLanguage CSV = new CollectionQueryLanguage(
            "CSV query", "file", "the CSV files");

    private static final String RULES = """
            - A query is a JSON object: {"source": "<NOUN>", "columns": {"<result column>": "<field>", ...}, "filter": {"<field>": <value>, ...}, "sort": [{"field": "<field>", "direction": "asc"}]}
            - columns is required and maps each result column name to one field of the NOUN, so a column can only show a field as it is; there are no expressions
            - filter keeps only the items whose fields equal the given values; that is the only filtering BACKEND offers
            - sort orders the items by fields of the NOUN; paging is done by the component
            - One NOUN per query and one row per item: there are no joins, no aggregation and no computed values. When the user asks for a total or an average, show the items it would be computed from and say that BACKEND cannot compute it""";

    private final String name;
    private final String noun;
    private final String backend;

    private CollectionQueryLanguage(String name, String noun, String backend) {
        this.name = name;
        this.noun = noun;
        this.backend = backend;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRules() {
        return RULES.replace("NOUN", noun).replace("BACKEND", backend);
    }

    @Override
    public String exampleQuery(String source, List<String> columns,
            List<String> aliases) {
        Objects.requireNonNull(source, "source must not be null");
        if (columns.isEmpty() || columns.size() != aliases.size()) {
            throw new IllegalArgumentException(
                    "An example needs at least one column and one alias per column");
        }
        var columnMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < columns.size(); i++) {
            columnMap.put(aliases.get(i), columns.get(i));
        }
        var query = new LinkedHashMap<String, Object>();
        query.put("source", source);
        query.put("columns", columnMap);
        return JacksonUtils.getMapper().writeValueAsString(query);
    }

    /**
     * Returns the word this language uses for a source.
     *
     * @return the noun, for example {@code "collection"}
     */
    public String getNoun() {
        return noun;
    }

    private Object readResolve() {
        return "file".equals(noun) ? CSV : REST;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CollectionQueryLanguage that
                && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
