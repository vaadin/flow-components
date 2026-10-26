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
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * A {@link QueryableProvider} over a MongoDB database in which the LLM writes
 * aggregation pipelines. The schema lists the collections and the fields of a
 * sample document of each; a query is a JSON object naming the collection and
 * the pipeline. Paging, sorting and counting are pushed down by appending
 * {@code $sort}, {@code $skip}, {@code $limit} and {@code $count} stages to the
 * pipeline the LLM wrote.
 * <p>
 * Pipeline stages that write ({@code $out}, {@code $merge}) are rejected; the
 * database user should be read-only as well.
 * </p>
 */
public class MongoAggregationQueryableProvider implements QueryableProvider {

    private static final Set<String> WRITE_STAGES = Set.of("$out", "$merge");

    private final transient Supplier<MongoDatabase> database;
    private final String notes;

    /**
     * Creates a provider over the database the supplier returns.
     *
     * @param database
     *            supplies the database to query; called on every use, so the
     *            provider itself holds no connection
     * @param notes
     *            notes about the data appended to the schema, or {@code null}
     */
    public MongoAggregationQueryableProvider(Supplier<MongoDatabase> database,
            String notes) {
        this.database = database;
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public QueryLanguage getQueryLanguage() {
        return MongoAggregationQueryLanguage.INSTANCE;
    }

    @Override
    public String getSchema() {
        var db = database.get();
        var schema = new StringBuilder("MONGODB DATABASE \"")
                .append(db.getName())
                .append("\". Collections and their fields, from a sample document of each:\n");
        for (var collection : collectionNames(db)) {
            var sample = db.getCollection(collection).find().first();
            schema.append("- ").append(collection).append(": ")
                    .append(sample == null ? "(empty)"
                            : String.join(", ",
                                    MongoDocuments.describeFields(sample)))
                    .append('\n');
        }
        if (!notes.isBlank()) {
            schema.append("\nNOTES:\n").append(notes);
        }
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        var request = parse(query);
        return run(request.collection(), request.pipeline());
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query,
            QueryPage page) {
        var request = parse(query);
        var pipeline = new ArrayList<>(request.pipeline());
        if (!page.sortOrders().isEmpty()) {
            var sort = new Document();
            page.sortOrders().forEach(order -> sort.append(order.getSorted(),
                    order.getDirection() == SortDirection.DESCENDING ? -1 : 1));
            pipeline.add(new Document("$sort", sort));
        }
        pipeline.add(new Document("$skip", page.offset()));
        pipeline.add(new Document("$limit", page.limit()));
        return run(request.collection(), pipeline);
    }

    @Override
    public OptionalInt count(String query) {
        var request = parse(query);
        var pipeline = new ArrayList<>(request.pipeline());
        pipeline.add(new Document("$count", "n"));
        var rows = run(request.collection(), pipeline);
        return OptionalInt.of(rows.isEmpty() ? 0
                : ((Number) rows.getFirst().get("n")).intValue());
    }

    private record Request(String collection, List<Document> pipeline) {
    }

    private Request parse(String query) {
        Document request;
        try {
            request = Document.parse(query.trim());
        } catch (RuntimeException e) {
            throw new ToolException(
                    "The query must be a JSON object with \"collection\" and \"pipeline\": "
                            + e.getMessage(),
                    e);
        }
        var db = database.get();
        var collections = collectionNames(db);
        if (!(request.get("collection") instanceof String name)
                || !collections.contains(name)) {
            throw new ToolException(
                    "Unknown collection " + request.get("collection")
                            + ". Available: " + collections);
        }
        List<Document> pipeline;
        try {
            pipeline = request.getList("pipeline", Document.class);
        } catch (ClassCastException e) {
            throw new ToolException(
                    "\"pipeline\" must be an array of stage objects", e);
        }
        if (pipeline == null) {
            throw new ToolException("\"pipeline\" is missing");
        }
        for (var stage : pipeline) {
            for (var stageName : stage.keySet()) {
                if (WRITE_STAGES.contains(stageName)) {
                    throw new ToolException(
                            "Stage " + stageName + " is not allowed");
                }
            }
        }
        return new Request(name, pipeline);
    }

    private List<Map<String, Object>> run(String collection,
            List<Document> pipeline) {
        var rows = new ArrayList<Map<String, Object>>();
        try {
            for (var document : database.get().getCollection(collection)
                    .aggregate(pipeline)) {
                rows.add(MongoDocuments.flatten(document));
            }
        } catch (MongoException e) {
            // The server describes what is wrong with the pipeline the LLM
            // wrote, so the LLM can correct it
            throw new ToolException(e.getMessage(), e);
        }
        return rows;
    }

    private static Set<String> collectionNames(MongoDatabase db) {
        return db.listCollectionNames().into(new TreeSet<>());
    }
}
