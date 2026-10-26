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

import org.bson.Document;

import com.vaadin.flow.component.ai.common.QueryLanguage;

/**
 * MongoDB aggregation pipelines as a {@link QueryLanguage}: a query is a JSON
 * object naming a collection and the pipeline to run on it, and result columns
 * are named by the output fields of a {@code $project} stage.
 */
public final class MongoAggregationQueryLanguage implements QueryLanguage {

    public static final MongoAggregationQueryLanguage INSTANCE = new MongoAggregationQueryLanguage();

    private static final String RULES = """
            - A query is a JSON object {"collection": "<name>", "pipeline": [<stages>]}
            - Each result document becomes a row; subdocuments are flattened into dotted column names (prices.open), so address nested fields with dots
            - Name the result columns with a $project stage: {"$project": {"_id": 0, "alias": "$field"}}; the _id of a $group stays in _id unless renamed there
            - Aggregate with $group and $sum, $avg, $min, $max, $count; filter with $match; sort with $sort
            - $out and $merge are not allowed""";

    private MongoAggregationQueryLanguage() {
    }

    @Override
    public String getName() {
        return "MongoDB aggregation pipeline";
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
        var projection = new Document("_id", 0);
        for (int i = 0; i < columns.size(); i++) {
            projection.append(aliases.get(i), "$" + columns.get(i));
        }
        var pipeline = new ArrayList<Document>();
        pipeline.add(new Document("$project", projection));
        return new Document("collection", source).append("pipeline", pipeline)
                .toJson();
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
