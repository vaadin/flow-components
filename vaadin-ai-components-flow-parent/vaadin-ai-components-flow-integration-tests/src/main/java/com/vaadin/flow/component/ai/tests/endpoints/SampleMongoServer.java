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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;

/**
 * An in-memory MongoDB server holding {@link SampleData} as documents, with a
 * client connected to it. The server speaks the MongoDB wire protocol, so the
 * official driver talks to it as to a real deployment. Once loaded, the server
 * logs every command clients send at {@code INFO}.
 * <p>
 * The documents are shaped the way a document store would hold the data rather
 * than as copies of the SQL rows: field names are camelCase, the {@code _id} is
 * the row id, dates are BSON dates, decimals are doubles, and two collections
 * nest related values: {@code stockPrices} keeps the four prices in a
 * {@code prices} subdocument and {@code orgChart} embeds the manager's id and
 * name in a {@code manager} subdocument, which the CEO's document does not
 * have.
 * </p>
 */
public final class SampleMongoServer {

    /** Name of the database holding the sample collections. */
    public static final String DATABASE_NAME = "sample";

    private static SampleMongoServer instance;

    private final MongoServer server;
    private final MongoClient client;
    private final String connectionString;

    private SampleMongoServer() {
        var backend = new LoggingMongoBackend();
        server = new MongoServer(backend);
        var address = server.bind();
        connectionString = "mongodb://" + address.getHostString() + ":"
                + address.getPort();
        client = MongoClients.create(connectionString);
        load();
        backend.startLogging();
    }

    /**
     * Returns the running server, starting and loading it on the first call.
     *
     * @return the server
     */
    public static synchronized SampleMongoServer get() {
        if (instance == null) {
            instance = new SampleMongoServer();
        }
        return instance;
    }

    /**
     * Returns the sample database.
     *
     * @return the database
     */
    public MongoDatabase database() {
        return client.getDatabase(DATABASE_NAME);
    }

    /**
     * Returns the connection string of the server.
     *
     * @return the connection string
     */
    public String connectionString() {
        return connectionString;
    }

    private void load() {
        var database = database();
        for (var table : SampleData.TABLES) {
            var documents = new ArrayList<Document>();
            for (var row : table.rowMaps()) {
                documents.add(toDocument(table.resourceName(), row));
            }
            database.getCollection(table.resourceName()).insertMany(documents);
        }
    }

    private static Document toDocument(String collection,
            Map<String, Object> row) {
        var document = new Document();
        for (var entry : row.entrySet()) {
            var key = entry.getKey().equals("id") ? "_id" : entry.getKey();
            var value = entry.getValue();
            if (value instanceof LocalDate date) {
                value = Date
                        .from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            } else if (value instanceof BigDecimal decimal) {
                value = decimal.doubleValue();
            }
            document.put(key, value);
        }
        if (collection.equals("stockPrices")) {
            nestPrices(document);
        } else if (collection.equals("orgChart")) {
            nestManager(document);
        }
        return document;
    }

    private static void nestPrices(Document document) {
        var prices = new Document();
        for (var price : List.of("open", "high", "low", "close")) {
            prices.put(price, document.remove(price + "Price"));
        }
        document.put("prices", prices);
    }

    private static void nestManager(Document document) {
        var managerId = (Integer) document.remove("managerId");
        if (managerId == null) {
            return;
        }
        var manager = SampleData.ORG_CHART.stream()
                .filter(member -> member.id() == managerId).findFirst()
                .orElseThrow();
        document.put("manager", new Document("id", manager.id()).append("name",
                manager.empName()));
    }
}
