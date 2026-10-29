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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

class DataProviderTableSourceTest {

    /** A record item with a property a table cannot hold. */
    public record Order(int id, String customer, BigDecimal total,
            LocalDate placed, boolean paid, List<String> tags) {
    }

    /** A bean item. */
    public static class Person {
        private final Integer id;
        private final String name;

        Person(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static final ListDataProvider<Order> ORDERS = DataProvider.ofItems(
            new Order(1, "Ann", new BigDecimal("12.50"),
                    LocalDate.of(2024, 1, 2), true, List.of("x")),
            new Order(2, "Bob", null, LocalDate.of(2024, 2, 3), false,
                    List.of()));

    @Test
    void columns_areTheSupportedPropertiesOfTheItemType() {
        var source = new DataProviderTableSource<>("orders", Order.class,
                ORDERS);

        Assertions.assertEquals(
                List.of(new Column("customer", String.class),
                        new Column("id", Integer.class),
                        new Column("paid", Boolean.class),
                        new Column("placed", LocalDate.class),
                        new Column("total", BigDecimal.class)),
                source.getColumns());
        Assertions.assertEquals("Flow data provider of Order",
                source.getDescription());
        Assertions.assertEquals("orders", source.getName());
    }

    @Test
    void fetch_returnsEveryItemAsARow() {
        var rows = new DataProviderTableSource<>("orders", Order.class, ORDERS)
                .fetch(Pushdown.NONE);

        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("Ann", rows.get(0).get("customer"));
        Assertions.assertEquals(1, rows.get(0).get("id"));
        Assertions.assertEquals(true, rows.get(0).get("paid"));
        Assertions.assertEquals(LocalDate.of(2024, 1, 2),
                rows.get(0).get("placed"));
        Assertions.assertNull(rows.get(1).get("total"));
        Assertions.assertFalse(rows.get(0).containsKey("tags"));
    }

    @Test
    void beans_workLikeRecords() {
        var source = new DataProviderTableSource<>("people", Person.class,
                DataProvider.ofItems(new Person(7, "Cid")));

        Assertions.assertEquals(List.of(new Column("id", Integer.class),
                new Column("name", String.class)), source.getColumns());
        Assertions.assertEquals(Map.of("id", 7, "name", "Cid"),
                source.fetch(Pushdown.NONE).get(0));
    }

    @Test
    void federatedProvider_queriesTheDataProvider() {
        var provider = new FederatedDatabaseProvider(List.of(
                new DataProviderTableSource<>("orders", Order.class, ORDERS)));

        var rows = provider.executeQuery(
                "SELECT customer, total FROM orders WHERE paid ORDER BY id");

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("Ann", rows.get(0).get("customer"));
        Assertions.assertEquals(0, new BigDecimal("12.50")
                .compareTo((BigDecimal) rows.get(0).get("total")));
    }

    @Test
    void source_sortsPagesAndCounts() {
        var source = new DataProviderTableSource<>("orders", Order.class,
                ORDERS);

        Assertions.assertEquals(
                Set.of(Capability.SORT, Capability.LIMIT, Capability.COUNT),
                source.getCapabilities());
        Assertions.assertEquals(2, source.count(Pushdown.NONE));
    }

    @Test
    void inMemoryDataProvider_sortsWithNullsAfterOtherValues() {
        var source = new DataProviderTableSource<>("orders", Order.class,
                ORDERS);

        var ascending = source.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("total", false)), 0, null));
        var descending = source.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("total", true)), 0, 1L));

        Assertions.assertEquals(List.of("Ann", "Bob"),
                ascending.stream().map(row -> row.get("customer")).toList());
        Assertions.assertEquals(List.of("Bob"),
                descending.stream().map(row -> row.get("customer")).toList());
    }

    @Test
    void backendDataProvider_receivesTheSortOrdersAndThePage() {
        var queries = new ArrayList<Query<Order, Void>>();
        DataProvider<Order, Void> backend = DataProvider
                .fromCallbacks(query -> {
                    queries.add(query);
                    return ORDERS.getItems().stream().skip(query.getOffset())
                            .limit(query.getLimit());
                }, query -> ORDERS.getItems().size());
        var source = new DataProviderTableSource<>("orders", Order.class,
                backend);

        var rows = source.fetch(new Pushdown(Map.of(),
                List.of(new SortKey("placed", true)), 1, 5L));

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals(1, queries.get(0).getOffset());
        Assertions.assertEquals(5, queries.get(0).getLimit());
        var sortOrder = queries.get(0).getSortOrders().get(0);
        Assertions.assertEquals("placed", sortOrder.getSorted());
        Assertions.assertEquals(SortDirection.DESCENDING,
                sortOrder.getDirection());
    }

    @Test
    void federatedProvider_readsAGridPageAndCountFromTheDataProvider() {
        var queries = new ArrayList<Query<Order, Void>>();
        var counts = new ArrayList<Query<Order, Void>>();
        DataProvider<Order, Void> backend = DataProvider
                .fromCallbacks(query -> {
                    queries.add(query);
                    return ORDERS.getItems().stream().sorted(
                            (a, b) -> -a.customer().compareTo(b.customer()))
                            .skip(query.getOffset()).limit(query.getLimit());
                }, query -> {
                    counts.add(query);
                    return ORDERS.getItems().size();
                });
        var provider = new FederatedDatabaseProvider(List.of(
                new DataProviderTableSource<>("orders", Order.class, backend)));
        var query = "SELECT customer AS \"Customer\" FROM orders";

        var page = provider.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Customer\" DESC) AS _limited LIMIT 1 OFFSET 0");
        var count = provider.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        Assertions.assertEquals(List.of(Map.of("Customer", "Bob")), page);
        var sortOrder = queries.get(0).getSortOrders().get(0);
        Assertions.assertEquals("customer", sortOrder.getSorted());
        Assertions.assertEquals(SortDirection.DESCENDING,
                sortOrder.getDirection());
        Assertions.assertEquals(1, queries.get(0).getLimit());
        Assertions.assertEquals(2L, count.get(0).values().iterator().next());
        Assertions.assertEquals(1, counts.size());
    }

    @Test
    void itemTypeWithoutUsableProperties_isRejected() {
        record Opaque(List<String> values) {
        }
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DataProviderTableSource<>("opaque", Opaque.class,
                        DataProvider.ofItems(new Opaque(List.of()))));
    }
}
