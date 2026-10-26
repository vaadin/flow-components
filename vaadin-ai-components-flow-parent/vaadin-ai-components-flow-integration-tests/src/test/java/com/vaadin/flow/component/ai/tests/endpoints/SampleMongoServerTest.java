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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

class SampleMongoServerTest {

    private final SampleMongoServer server = SampleMongoServer.get();

    @Test
    void holdsEveryTableAsCollection() {
        var database = server.database();
        var names = database.listCollectionNames().into(new TreeSet<>());
        for (var table : SampleData.TABLES) {
            Assertions.assertTrue(names.contains(table.resourceName()),
                    table.resourceName());
            Assertions.assertEquals(table.rows().size(), database
                    .getCollection(table.resourceName()).countDocuments());
        }
    }

    @Test
    void documentsUseIdAndCamelCaseFields() {
        var sale = server.database().getCollection("sales")
                .find(Filters.eq("_id", 7)).first();
        Assertions.assertNotNull(sale);
        Assertions.assertEquals("January", sale.getString("month"));
        Assertions.assertEquals(1, sale.getInteger("monthOrder"));
        Assertions.assertEquals("South", sale.getString("region"));
        Assertions.assertFalse(sale.containsKey("id"));
    }

    @Test
    void stockPricesNestThePricesAndUseBsonDates() {
        var price = server.database().getCollection("stockPrices")
                .find(Filters.eq("_id", 1)).first();
        Assertions.assertNotNull(price);
        Assertions.assertInstanceOf(Date.class, price.get("tradeDate"));
        var prices = price.get("prices", Document.class);
        Assertions.assertEquals(142.5, prices.getDouble("open"));
        Assertions.assertEquals(147.8, prices.getDouble("close"));
        Assertions.assertFalse(price.containsKey("openPrice"));
    }

    @Test
    void orgChartEmbedsTheManager() {
        var collection = server.database().getCollection("orgChart");
        var ceo = collection.find(Filters.eq("_id", 1)).first();
        Assertions.assertNotNull(ceo);
        Assertions.assertFalse(ceo.containsKey("manager"));
        Assertions.assertFalse(ceo.containsKey("managerId"));
        var lead = collection.find(Filters.eq("_id", 4)).first();
        Assertions.assertNotNull(lead);
        var manager = lead.get("manager", Document.class);
        Assertions.assertEquals(2, manager.getInteger("id"));
        Assertions.assertEquals("Tom VP Eng", manager.getString("name"));
    }

    @Test
    void aggregationPipelinesRun() {
        var totals = server.database().getCollection("sales")
                .aggregate(List.of(
                        Aggregates.group("$region",
                                Accumulators.sum("total", "$revenue")),
                        Aggregates.sort(Sorts.ascending("_id"))))
                .into(new ArrayList<>());
        Assertions.assertEquals(2, totals.size());
        Assertions.assertEquals("North", totals.getFirst().get("_id"));
        Assertions.assertEquals(338000, totals.getFirst().getInteger("total"));
    }

    @Test
    void driverConnectsWithTheConnectionString() {
        try (var client = MongoClients.create(server.connectionString())) {
            Assertions.assertEquals(5,
                    client.getDatabase(SampleMongoServer.DATABASE_NAME)
                            .getCollection("employees").countDocuments());
        }
    }
}
