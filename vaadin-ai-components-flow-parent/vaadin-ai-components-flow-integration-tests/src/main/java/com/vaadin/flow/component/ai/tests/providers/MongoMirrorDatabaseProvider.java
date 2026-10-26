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
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.mongodb.client.MongoDatabase;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

/**
 * A provider that lets the LLM query a MongoDB database with SQL by copying
 * every collection into a {@link SqlMirror} on first use. Documents are
 * flattened: a subdocument field {@code prices.open} becomes the column
 * {@code PRICES_OPEN} and {@code _id} becomes {@code ID}. Compare with
 * {@link MongoAggregationQueryableProvider}, which lets the LLM query the
 * documents directly.
 */
public class MongoMirrorDatabaseProvider extends MirrorDatabaseProvider {

    private final transient Supplier<MongoDatabase> database;

    /**
     * Creates a provider mirroring the database the supplier returns.
     *
     * @param database
     *            supplies the database to mirror
     */
    public MongoMirrorDatabaseProvider(Supplier<MongoDatabase> database) {
        this.database = database;
    }

    @Override
    protected SqlMirror copy() {
        var db = database.get();
        var mirror = SqlMirror.create("mongo_mirror_" + db.getName(), false);
        for (var collection : db.listCollectionNames().into(new TreeSet<>())) {
            var rows = new ArrayList<Map<String, Object>>();
            for (var document : db.getCollection(collection).find()) {
                rows.add(MongoDocuments.flatten(document));
            }
            mirror.load(collection, rows);
        }
        return mirror;
    }

    @Override
    protected String notes() {
        return """
                - This database is a snapshot of a MongoDB database. Each collection is a table and each document field a column, renamed to upper snake_case; subdocument fields are joined with underscores (prices.open -> PRICES_OPEN) and _id is ID.
                """
                + SampleData.NOTES;
    }
}
