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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.TableSource.Capability;
import com.vaadin.flow.component.ai.provider.TableSource.Column;
import com.vaadin.flow.component.ai.provider.TableSource.Pushdown;
import com.vaadin.flow.component.ai.provider.TableSource.SortKey;

class FederatedDatabaseProviderTest {

    /**
     * A source over fixed rows that records every pushdown it receives and
     * applies what it was given itself, the equality filters, the sort, the
     * offset and the limit, like a backend would.
     */
    static class RecordingSource implements TableSource {

        private final String name;
        private final List<Column> columns;
        private final Set<Capability> capabilities;
        private final List<Map<String, Object>> rows;
        final List<Pushdown> pushdowns = new ArrayList<>();
        final List<Pushdown> counts = new ArrayList<>();
        int sortKeysSupported = Integer.MAX_VALUE;
        Set<String> unfilterableColumns = Set.of();
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
        public boolean supportsEqualityFilter(String column) {
            return !unfilterableColumns.contains(column);
        }

        @Override
        public boolean supportsSort(List<SortKey> sort) {
            return sort.size() <= sortKeysSupported;
        }

        @Override
        public List<Map<String, Object>> fetch(Pushdown pushdown) {
            pushdowns.add(pushdown);
            if (failure != null) {
                throw failure;
            }
            var result = new ArrayList<>(filtered(pushdown));
            result.sort(comparator(pushdown.sort()));
            var from = (int) Math.min(result.size(), pushdown.offset());
            var to = pushdown.limit() == null ? result.size()
                    : (int) Math.min(result.size(), from + pushdown.limit());
            return result.subList(from, to);
        }

        @Override
        public long count(Pushdown pushdown) {
            counts.add(pushdown);
            return filtered(pushdown).size();
        }

        private List<Map<String, Object>> filtered(Pushdown pushdown) {
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Comparator<Map<String, Object>> comparator(
                List<SortKey> sort) {
            Comparator<Map<String, Object>> result = (a, b) -> 0;
            for (var key : sort) {
                Comparator<Map<String, Object>> byKey = Comparator.comparing(
                        row -> (Comparable) row.get(key.column()),
                        Comparator.nullsLast(Comparator.naturalOrder()));
                result = result.thenComparing(
                        key.descending() ? byKey.reversed() : byKey);
            }
            return result;
        }
    }

    /**
     * An adapter table with fixed rows, standing in for one of Calcite's
     * adapters: the engine reads it as a Calcite table, not through a source.
     */
    static class FixedAdapterTable implements AdapterTable {

        @Override
        public String getName() {
            return "cities";
        }

        @Override
        public String getDescription() {
            return "adapter rows";
        }

        @Override
        public Table createTable(SchemaPlus rootSchema) {
            return new ScannableFixedTable();
        }
    }

    static class ScannableFixedTable extends AbstractTable
            implements ScannableTable {

        @Override
        public RelDataType getRowType(RelDataTypeFactory typeFactory) {
            return typeFactory.builder().add("city", SqlTypeName.VARCHAR)
                    .add("population", SqlTypeName.INTEGER).build();
        }

        @Override
        public Enumerable<Object[]> scan(DataContext root) {
            return Linq4j
                    .asEnumerable(List.of(new Object[] { "Turku", 200_000 },
                            new Object[] { "Oulu", 210_000 }));
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

    private static final Set<Capability> ALL_CAPABILITIES = Set.of(
            Capability.EQUALITY_FILTER, Capability.SORT, Capability.LIMIT,
            Capability.COUNT);

    private static final String GRID_QUERY = "SELECT name AS \"Name\", salary AS \"Salary\" FROM people WHERE active = TRUE";

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
    void stringLiteralsOfAUnion_joinWithTableValues() {
        var rows = provider.executeQuery(
                "SELECT p.name, t.town FROM people p JOIN (SELECT 'Oulu' AS town UNION ALL SELECT 'Turku' AS town) t ON t.town = p.city ORDER BY p.id");

        Assertions.assertEquals(List.of("Ann", "Bob", "Cid"),
                rows.stream().map(row -> row.get("name")).toList());
        Assertions.assertEquals("Oulu", rows.get(1).get("town"));
    }

    @Test
    void stringLiteralsOfACase_joinWithTableValues() {
        var rows = provider.executeQuery(
                "SELECT p.name FROM people p JOIN (SELECT DISTINCT CASE WHEN amount > 50 THEN 'Oulu' ELSE 'Turku' END AS town FROM orders) t ON t.town = p.city ORDER BY p.id");

        Assertions.assertEquals(List.of("Ann", "Bob", "Cid"),
                rows.stream().map(row -> row.get("name")).toList());
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

    private FederatedDatabaseProvider providerOf(RecordingSource... sources) {
        var provider = new FederatedDatabaseProvider(List.of(sources));
        provider.setCacheDuration(Duration.ZERO);
        return provider;
    }

    private static RecordingSource pagingPeople() {
        return new RecordingSource("people", PEOPLE_COLUMNS, ALL_CAPABILITIES,
                PEOPLE_ROWS);
    }

    private static List<Object> column(List<Map<String, Object>> rows,
            String column) {
        return rows.stream().map(row -> row.get(column)).toList();
    }

    @Test
    void gridPage_isReadAsOnePageOfTheSource() {
        var people = pagingPeople();

        var rows = providerOf(people)
                .executeQuery("SELECT * FROM (SELECT * FROM (" + GRID_QUERY
                        + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 1 OFFSET 1");

        Assertions.assertEquals(List.of("Ann"), column(rows, "Name"));
        Assertions.assertEquals(List.of("Name", "Salary"),
                new ArrayList<>(rows.get(0).keySet()));
        Assertions.assertEquals(
                List.of(new Pushdown(Map.of("active", true),
                        List.of(new SortKey("salary", true)), 1, 1L)),
                people.pushdowns);
    }

    @Test
    void gridCount_isCountedByTheSource() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT COUNT(*) FROM (" + GRID_QUERY + ") AS _counted");

        Assertions.assertEquals(2L, rows.get(0).values().iterator().next());
        Assertions.assertEquals(List.of(new Pushdown(Map.of("active", true))),
                people.counts);
        Assertions.assertTrue(people.pushdowns.isEmpty());
    }

    @Test
    void gridValidation_readsOneRow() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT * FROM (" + GRID_QUERY + ") AS _v LIMIT 1");

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals(
                List.of(new Pushdown(Map.of("active", true), List.of(), 0, 1L)),
                people.pushdowns);
    }

    @Test
    void gridPageWithoutSort_followsTheOrderOfTheQuery() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT * FROM (SELECT name FROM people ORDER BY born DESC) AS _limited LIMIT 2 OFFSET 0");

        Assertions.assertEquals(List.of("Cid", "Ann"), column(rows, "name"));
        Assertions.assertEquals(List.of(new SortKey("born", true)),
                people.pushdowns.get(0).sort());
        Assertions.assertEquals(2L, people.pushdowns.get(0).limit());
    }

    @Test
    void topRows_ofOneTable_areReadFromTheSource() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT name, city FROM people WHERE city = 'Turku' ORDER BY born, name DESC LIMIT 1");

        Assertions.assertEquals(List.of("Ann"), column(rows, "name"));
        Assertions.assertEquals(
                new Pushdown(Map.of("city", "Turku"),
                        List.of(new SortKey("born", false),
                                new SortKey("name", true)),
                        0, 1L),
                people.pushdowns.get(0));
    }

    @Test
    void equalityFilter_onAColumnTheSourceCannotFilterBy_staysInTheEngine() {
        var people = pagingPeople();
        people.unfilterableColumns = Set.of("city");

        var rows = providerOf(people).executeQuery(
                "SELECT name FROM people WHERE city = 'Turku' AND active = TRUE ORDER BY name");

        Assertions.assertEquals(List.of("Ann", "Cid"), column(rows, "name"));
        Assertions.assertEquals(Map.of("active", true),
                people.pushdowns.get(0).equalityFilters());
    }

    @Test
    void limit_staysInTheEngineWhenTheSourceCannotFilterByAColumn() {
        var people = pagingPeople();
        people.unfilterableColumns = Set.of("city");

        var rows = providerOf(people).executeQuery(
                "SELECT name FROM people WHERE city = 'Turku' ORDER BY name DESC LIMIT 1");

        Assertions.assertEquals(List.of("Cid"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void limitZero_readsNothing() {
        var people = pagingPeople();

        var rows = providerOf(people)
                .executeQuery("SELECT name FROM people LIMIT 0");

        Assertions.assertTrue(rows.isEmpty());
        Assertions.assertTrue(people.pushdowns.isEmpty());
    }

    @Test
    void limit_staysInTheEngineWhenAFilterDoes() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT name FROM people WHERE salary > 4500 ORDER BY salary LIMIT 1");

        Assertions.assertEquals(List.of("Ann"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void limit_staysInTheEngineWithoutTheCapability() {
        var rows = provider.executeQuery("SELECT * FROM (SELECT * FROM ("
                + GRID_QUERY
                + ") AS _t ORDER BY \"Salary\" DESC) AS _limited LIMIT 1 OFFSET 1");

        Assertions.assertEquals(List.of("Ann"), column(rows, "Name"));
        Assertions.assertEquals(List.of(new Pushdown(Map.of("active", true))),
                people.pushdowns);
    }

    @Test
    void sort_staysInTheEngineWhenTheSourceCannotSortThatWay() {
        var people = pagingPeople();
        people.sortKeysSupported = 1;

        var rows = providerOf(people).executeQuery(
                "SELECT name FROM people ORDER BY city, name DESC LIMIT 2");

        Assertions.assertEquals(List.of("Bob", "Cid"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void sort_staysInTheEngineWhenNullsArePlacedOtherwise() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT name FROM people ORDER BY born NULLS FIRST LIMIT 1");

        Assertions.assertEquals(List.of("Cid"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void sort_staysInTheEngineForAComputedValue() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT name, salary * 2 AS doubled FROM people ORDER BY doubled DESC LIMIT 1");

        Assertions.assertEquals(List.of("Cid"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
    }

    @Test
    void computedColumns_areComputedOverThePushedPage() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT UPPER(name) AS loud FROM people ORDER BY id DESC LIMIT 1");

        Assertions.assertEquals(List.of("CID"), column(rows, "loud"));
        Assertions.assertEquals(List.of(new SortKey("id", true)),
                people.pushdowns.get(0).sort());
    }

    @Test
    void limitOverAJoin_staysInTheEngine() {
        var people = pagingPeople();
        var orders = new RecordingSource("orders", ORDER_COLUMNS,
                ALL_CAPABILITIES, ORDER_ROWS);

        var rows = providerOf(people, orders).executeQuery(
                "SELECT p.name, o.amount FROM people p JOIN orders o ON o.personId = p.id ORDER BY o.amount DESC LIMIT 1");

        Assertions.assertEquals(List.of("Bob"), column(rows, "name"));
        Assertions.assertEquals(List.of(Pushdown.NONE), people.pushdowns);
        Assertions.assertEquals(List.of(Pushdown.NONE), orders.pushdowns);
    }

    @Test
    void count_staysInTheEngineForGroupsAndDistinctValues() {
        var people = pagingPeople();
        var provider = providerOf(people);

        var distinct = provider
                .executeQuery("SELECT COUNT(DISTINCT city) AS n FROM people");
        var grouped = provider.executeQuery(
                "SELECT city, COUNT(*) AS n FROM people GROUP BY city ORDER BY city");

        Assertions.assertEquals(2L, distinct.get(0).get("n"));
        Assertions.assertEquals(List.of(1L, 2L), column(grouped, "n"));
        Assertions.assertTrue(people.counts.isEmpty());
    }

    @Test
    void count_staysInTheEngineWhenAFilterDoes() {
        var people = pagingPeople();

        var rows = providerOf(people).executeQuery(
                "SELECT COUNT(*) AS n FROM people WHERE salary > 4500");

        Assertions.assertEquals(2L, rows.get(0).get("n"));
        Assertions.assertTrue(people.counts.isEmpty());
    }

    @Test
    void cache_keepsCountsAndPagesApart() {
        var people = pagingPeople();
        var provider = providerOf(people);
        provider.setCacheDuration(Duration.ofMinutes(1));
        var count = "SELECT COUNT(*) FROM (" + GRID_QUERY + ") AS _counted";
        var page = "SELECT * FROM (" + GRID_QUERY + ") AS _limited LIMIT 1";

        provider.executeQuery(count);
        provider.executeQuery(count);
        provider.executeQuery(page);
        provider.executeQuery(page);

        Assertions.assertEquals(1, people.counts.size());
        Assertions.assertEquals(1, people.pushdowns.size());
    }

    @Test
    void explain_namesThePageAndTheCountASourceReads() {
        var provider = providerOf(pagingPeople());

        var page = provider.explain(
                "SELECT name FROM people WHERE city = 'Turku' ORDER BY name LIMIT 5");
        var count = provider
                .explain("SELECT COUNT(*) FROM people WHERE city = 'Turku'");

        Assertions.assertTrue(page.contains(
                "fetch Pushdown[equalityFilters={city=Turku}, sort=[SortKey[column=name, descending=false]], offset=0, limit=5]"),
                page);
        Assertions.assertTrue(
                count.contains("count Pushdown[equalityFilters={city=Turku}"),
                count);
    }

    @Test
    void pushdown_rejectsNegativeOffsetAndLimit() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Pushdown(Map.of(), List.of(), -1, null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Pushdown(Map.of(), List.of(), 0, -1L));
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
    void adapterTable_joinsTheSchemaAndTheQueries() {
        var provider = new FederatedDatabaseProvider(List.of(people),
                List.of(new FixedAdapterTable()), null);

        var schema = provider.getSchema();
        var rows = provider.executeQuery("""
                SELECT p.name AS "Name", c.population AS "Population"
                FROM people p JOIN cities c ON c.city = p.city
                ORDER BY p.id""");

        Assertions.assertTrue(schema.contains("TABLE cities  (adapter rows)\n"),
                schema);
        Assertions.assertTrue(schema.contains("  population INTEGER\n"),
                schema);
        Assertions.assertEquals(List.of("Ann", "Bob", "Cid"),
                rows.stream().map(row -> row.get("Name")).toList());
        Assertions.assertEquals(210_000, rows.get(1).get("Population"));
    }

    @Test
    void explain_showsThePhysicalPlan() {
        var plan = provider.explain(
                "SELECT name FROM people WHERE city = 'Turku' ORDER BY name");

        Assertions.assertTrue(plan.contains("BindableTableScan"), plan);
        Assertions.assertTrue(plan.contains("federated, people"), plan);
        Assertions.assertThrows(ToolException.class,
                () -> provider.explain("SELECT nothing FROM nowhere"));
    }

    @Test
    void constructor_rejectsBadSources() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FederatedDatabaseProvider(List.of()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new FederatedDatabaseProvider(List.of(people),
                        List.of(new FixedAdapterTable() {
                            @Override
                            public String getName() {
                                return "People";
                            }
                        }), null));
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
