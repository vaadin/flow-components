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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;

class FederatedDatabaseProviderTest {

    /**
     * A source over fixed rows that records every pushdown it receives and
     * applies the equality filters itself when it has the capability, like a
     * backend would.
     */
    static class RecordingSource implements TableSource {

        private final String name;
        private final List<Column> columns;
        private final Set<Capability> capabilities;
        private final List<Map<String, Object>> rows;
        final List<Pushdown> pushdowns = new ArrayList<>();
        RuntimeException failure;

        RecordingSource(String name, List<Column> columns,
                Set<Capability> capabilities, List<Map<String, Object>> rows) {
            this.name = name;
            this.columns = columns;
            this.capabilities = capabilities;
            this.rows = rows;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<Column> getColumns() {
            return columns;
        }

        @Override
        public Set<Capability> getCapabilities() {
            return capabilities;
        }

        @Override
        public String getDescription() {
            return "fixture rows of " + name;
        }

        @Override
        public List<Map<String, Object>> fetch(Pushdown pushdown) {
            pushdowns.add(pushdown);
            if (failure != null) {
                throw failure;
            }
            if (!capabilities.contains(Capability.EQUALITY_FILTER)) {
                return rows;
            }
            return rows.stream()
                    .filter(row -> pushdown.equalityFilters().entrySet()
                            .stream()
                            .allMatch(filter -> filter.getValue()
                                    .equals(row.get(filter.getKey()))))
                    .toList();
        }
    }

    private static Map<String, Object> row(Object... namesAndValues) {
        var row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            row.put((String) namesAndValues[i], namesAndValues[i + 1]);
        }
        return row;
    }

    private static final List<Column> PEOPLE_COLUMNS = List.of(
            new Column("id", Integer.class), new Column("name", String.class),
            new Column("city", String.class),
            new Column("born", LocalDate.class),
            new Column("salary", BigDecimal.class),
            new Column("active", Boolean.class));

    private static final List<Map<String, Object>> PEOPLE_ROWS = List.of(
            row("id", 1, "name", "Ann", "city", "Turku", "born",
                    LocalDate.of(1990, 5, 1), "salary",
                    new BigDecimal("5000.50"), "active", true),
            row("id", 2, "name", "Bob", "city", "Oulu", "born",
                    LocalDate.of(1985, 11, 20), "salary",
                    new BigDecimal("4200.00"), "active", false),
            row("id", 3, "name", "Cid", "city", "Turku", "born", null, "salary",
                    new BigDecimal("6100.25"), "active", true));

    private static final List<Column> ORDER_COLUMNS = List.of(
            new Column("id", Integer.class),
            new Column("personId", Integer.class),
            new Column("amount", Double.class),
            new Column("placed", LocalDateTime.class));

    private static final List<Map<String, Object>> ORDER_ROWS = List.of(
            row("id", 10, "personId", 1, "amount", 12.5, "placed",
                    LocalDateTime.of(2024, 1, 2, 10, 0)),
            row("id", 11, "personId", 1, "amount", 7.25, "placed",
                    LocalDateTime.of(2024, 2, 3, 11, 30)),
            row("id", 12, "personId", 2, "amount", 100.0, "placed",
                    LocalDateTime.of(2024, 3, 4, 12, 45)));

    private RecordingSource people;
    private RecordingSource orders;
    private FederatedDatabaseProvider provider;

    @BeforeEach
    void setUp() {
        people = new RecordingSource("people", PEOPLE_COLUMNS,
                Set.of(Capability.EQUALITY_FILTER), PEOPLE_ROWS);
        orders = new RecordingSource("orders", ORDER_COLUMNS, Set.of(),
                ORDER_ROWS);
        provider = new FederatedDatabaseProvider(List.of(people, orders),
                "- custom note\n");
        provider.setCacheDuration(Duration.ZERO);
    }

    @Test
    void schema_listsTablesColumnsTypesDescriptionsAndNotes() {
        var schema = provider.getSchema();

        Assertions.assertTrue(
                schema.contains("TABLE people  (fixture rows of people)\n"),
                schema);
        Assertions.assertTrue(schema.contains("  name VARCHAR\n"), schema);
        Assertions.assertTrue(schema.contains("  born DATE\n"), schema);
        Assertions.assertTrue(schema.contains("  salary DECIMAL\n"), schema);
        Assertions.assertTrue(schema.contains("  active BOOLEAN\n"), schema);
        Assertions.assertTrue(schema.contains("TABLE orders"), schema);
        Assertions.assertTrue(schema.contains("  placed TIMESTAMP\n"), schema);
        Assertions.assertTrue(schema.contains("  amount DOUBLE\n"), schema);
        Assertions.assertTrue(schema.contains("NOTES:\n"), schema);
        Assertions.assertTrue(schema.endsWith("- custom note\n"), schema);
    }

    @Test
    void schema_quotesReservedColumnNames() {
        var sales = new RecordingSource("sales",
                List.of(new Column("month", String.class),
                        new Column("value", Integer.class),
                        new Column("region", String.class)),
                Set.of(), List.of());
        var schema = new FederatedDatabaseProvider(List.of(sales)).getSchema();

        Assertions.assertTrue(schema.contains("  \"month\" VARCHAR\n"), schema);
        Assertions.assertTrue(schema.contains("  \"value\" INTEGER\n"), schema);
        Assertions.assertTrue(schema.contains("  region VARCHAR\n"), schema);
    }

    @Test
    void select_returnsRowsWithLabelsAsWritten() {
        var rows = provider.executeQuery(
                "SELECT name AS \"Full name\", city FROM people ORDER BY id");

        Assertions.assertEquals(3, rows.size());
        Assertions.assertEquals(List.of("Full name", "city"),
                new ArrayList<>(rows.get(0).keySet()));
        Assertions.assertEquals("Ann", rows.get(0).get("Full name"));
        Assertions.assertEquals("Turku", rows.get(0).get("city"));
    }

    @Test
    void identifiers_matchCaseInsensitivelyAndKeepTheirCase() {
        var rows = provider.executeQuery(
                "SELECT PERSONID AS personId FROM ORDERS WHERE Id = 10");

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals(List.of("personId"),
                new ArrayList<>(rows.get(0).keySet()));
        Assertions.assertEquals(1, rows.get(0).get("personId"));
    }

    @Test
    void equalityFilter_isPushedDownWhenTheSourceCanFilter() {
        var rows = provider.executeQuery(
                "SELECT name FROM people WHERE city = 'Turku' ORDER BY name");

        Assertions.assertEquals(List.of("Ann", "Cid"),
                rows.stream().map(row -> row.get("name")).toList());
        Assertions.assertEquals(1, people.pushdowns.size());
        Assertions.assertEquals(Map.of("city", "Turku"),
                people.pushdowns.get(0).equalityFilters());
    }

    @Test
    void equalityFilter_isNotPushedDownWithoutTheCapability() {
        var source = new RecordingSource("people", PEOPLE_COLUMNS, Set.of(),
                PEOPLE_ROWS);
        var provider = new FederatedDatabaseProvider(List.of(source));

        var rows = provider.executeQuery(
                "SELECT name FROM people WHERE city = 'Turku' ORDER BY name");

        Assertions.assertEquals(List.of("Ann", "Cid"),
                rows.stream().map(row -> row.get("name")).toList());
        Assertions.assertEquals(Map.of(),
                source.pushdowns.get(0).equalityFilters());
    }

    @Test
    void equalityFilter_carriesValuesOfTheColumnType() {
        provider.executeQuery("SELECT name FROM people WHERE id = 2");
        provider.executeQuery(
                "SELECT name FROM people WHERE born = DATE '1990-05-01'");
        provider.executeQuery("SELECT name FROM people WHERE active = TRUE");
        provider.executeQuery("SELECT name FROM people WHERE salary = 4200.00");

        Assertions.assertEquals(Map.of("id", 2),
                people.pushdowns.get(0).equalityFilters());
        Assertions.assertEquals(Map.of("born", LocalDate.of(1990, 5, 1)),
                people.pushdowns.get(1).equalityFilters());
        Assertions.assertEquals(Map.of("active", true),
                people.pushdowns.get(2).equalityFilters());
        var salary = people.pushdowns.get(3).equalityFilters().get("salary");
        Assertions.assertEquals(0,
                new BigDecimal("4200.00").compareTo((BigDecimal) salary));
    }

    @Test
    void nonEqualityFilters_stayInTheEngine() {
        var rows = provider.executeQuery(
                "SELECT name FROM people WHERE salary > 4500 AND city = 'Turku' ORDER BY name");

        Assertions.assertEquals(List.of("Ann", "Cid"),
                rows.stream().map(row -> row.get("name")).toList());
        Assertions.assertEquals(Map.of("city", "Turku"),
                people.pushdowns.get(0).equalityFilters());
    }

    @Test
    void count_fetchesEveryRowOnce() {
        var rows = provider.executeQuery("SELECT COUNT(*) AS n FROM people");

        Assertions.assertEquals(3L, rows.get(0).get("n"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void join_andAggregation_runAcrossSources() {
        var rows = provider.executeQuery("""
                SELECT p.name AS "Person", SUM(o.amount) AS "Total"
                FROM people p JOIN orders o ON o.personId = p.id
                GROUP BY p.name ORDER BY "Total" DESC""");

        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("Bob", rows.get(0).get("Person"));
        Assertions.assertEquals(100.0, rows.get(0).get("Total"));
        Assertions.assertEquals("Ann", rows.get(1).get("Person"));
        Assertions.assertEquals(19.75, rows.get(1).get("Total"));
    }

    @Test
    void aggregation_overDecimals() {
        var rows = provider.executeQuery(
                "SELECT city, SUM(salary) AS total, COUNT(*) AS n FROM people GROUP BY city ORDER BY city");

        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("Oulu", rows.get(0).get("city"));
        Assertions.assertEquals(0, new BigDecimal("4200.00")
                .compareTo((BigDecimal) rows.get(0).get("total")));
        Assertions.assertEquals(0, new BigDecimal("11100.75")
                .compareTo((BigDecimal) rows.get(1).get("total")));
        Assertions.assertEquals(2L, rows.get(1).get("n"));
    }

    @Test
    void gridWrappers_pageSortAndCount() {
        var query = "SELECT name AS \"Name\", salary AS \"Salary\" FROM people";

        var sample = provider
                .executeQuery("SELECT * FROM (" + query + ") AS _v LIMIT 1");
        var page = provider.executeQuery("SELECT * FROM (SELECT * FROM ("
                + query
                + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 2 OFFSET 1");
        var count = provider.executeQuery(
                "SELECT COUNT(*) FROM (" + query + ") AS _counted");

        Assertions.assertEquals(1, sample.size());
        Assertions.assertEquals(List.of("Name", "Salary"),
                new ArrayList<>(sample.get(0).keySet()));
        Assertions.assertEquals(List.of("Ann", "Bob"),
                page.stream().map(row -> row.get("Name")).toList());
        Assertions.assertEquals(3L, count.get(0).values().iterator().next());
    }

    @Test
    void dateAndTimestampValues_roundTripAsJavaTime() {
        var born = provider.executeQuery(
                "SELECT born FROM people WHERE born < DATE '1990-01-01'");
        var placed = provider
                .executeQuery("SELECT placed FROM orders WHERE id = 11");

        Assertions.assertEquals(LocalDate.of(1985, 11, 20),
                born.get(0).get("born"));
        Assertions.assertEquals(LocalDateTime.of(2024, 2, 3, 11, 30),
                placed.get(0).get("placed"));
    }

    @Test
    void nullValues_passThrough() {
        var rows = provider.executeQuery(
                "SELECT name, born FROM people WHERE born IS NULL");

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("Cid", rows.get(0).get("name"));
        Assertions.assertTrue(rows.get(0).containsKey("born"));
        Assertions.assertNull(rows.get(0).get("born"));
    }

    @Test
    void unknownColumn_throwsToolExceptionNamingIt() {
        var exception = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("SELECT foo FROM people"));

        Assertions.assertTrue(exception.getMessage().contains("foo"),
                exception.getMessage());
        Assertions.assertFalse(
                exception.getMessage().contains("Error while executing"),
                exception.getMessage());
    }

    @Test
    void unknownTable_throwsToolExceptionNamingIt() {
        var exception = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("SELECT id FROM nowhere"));

        Assertions.assertTrue(exception.getMessage().contains("nowhere"),
                exception.getMessage());
    }

    @Test
    void syntaxError_throwsToolExceptionWithoutTheExpectedTokenList() {
        var exception = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("SELEC name FROM people"));

        Assertions.assertFalse(exception.getMessage().contains("Was expecting"),
                exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("line 1"),
                exception.getMessage());
    }

    @Test
    void writes_areRejectedBeforeReachingTheSources() {
        for (var sql : List.of("INSERT INTO people (id) VALUES (9)",
                "DELETE FROM people", "UPDATE people SET name = 'x'")) {
            var exception = Assertions.assertThrows(ToolException.class,
                    () -> provider.executeQuery(sql));
            Assertions.assertEquals("Only SELECT queries are allowed.",
                    exception.getMessage());
        }
        Assertions.assertTrue(people.pushdowns.isEmpty());
    }

    @Test
    void sourceToolException_isRelayed() {
        people.failure = new ToolException("The API rejected the filter");

        var exception = Assertions.assertThrows(ToolException.class,
                () -> provider.executeQuery("SELECT name FROM people"));

        Assertions.assertEquals("The API rejected the filter",
                exception.getMessage());
    }

    @Test
    void sourceFailure_isNotRelayed() {
        people.failure = new IllegalStateException("secret backend detail");

        var exception = Assertions.assertThrows(RuntimeException.class,
                () -> provider.executeQuery("SELECT name FROM people"));

        Assertions.assertFalse(exception instanceof ToolException);
        Assertions.assertFalse(exception.getMessage().contains("secret"));
    }

    @Test
    void cache_reusesRowsWithinTheDurationAndIsKeyedByPushdown() {
        provider.setCacheDuration(Duration.ofMinutes(1));

        provider.executeQuery("SELECT name FROM people WHERE city = 'Turku'");
        provider.executeQuery("SELECT name FROM people WHERE city = 'Turku'");
        provider.executeQuery("SELECT name FROM people WHERE city = 'Oulu'");

        Assertions.assertEquals(2, people.pushdowns.size());
    }

    @Test
    void cache_zeroDurationFetchesEveryTime() {
        provider.executeQuery("SELECT name FROM people");
        provider.executeQuery("SELECT name FROM people");

        Assertions.assertEquals(2, people.pushdowns.size());
    }

    @Test
    void provider_worksAfterSerialization()
            throws IOException, ClassNotFoundException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(provider);
        }
        FederatedDatabaseProvider copy;
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (FederatedDatabaseProvider) in.readObject();
        }

        var rows = copy.executeQuery("SELECT COUNT(*) AS n FROM orders");

        Assertions.assertEquals(3L, rows.get(0).get("n"));
    }

    @Test
    void constructor_rejectsBadSources() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FederatedDatabaseProvider(List.of()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FederatedDatabaseProvider(
                        List.of(people, new RecordingSource("PEOPLE",
                                ORDER_COLUMNS, Set.of(), List.of()))));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FederatedDatabaseProvider(List.of(new RecordingSource(
                        "bad",
                        List.of(new Column("when", java.util.Date.class)),
                        Set.of(), List.of()))));
    }
}
