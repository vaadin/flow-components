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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.data.binder.BeanPropertySet;
import com.vaadin.flow.data.binder.PropertyDefinition;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * A {@link TableSource} over a Flow {@link DataProvider}, so that data an
 * application already serves to its grids becomes a table of a
 * {@link FederatedDatabaseProvider}. The columns are the properties of the item
 * type, a bean or a record, whose types a table can hold: text, numbers,
 * booleans and the {@code java.time} dates and times; properties of other types
 * are left out. The item type's property names are the column names, in name
 * order.
 * <p>
 * The source sorts, pages and counts through the data provider, so a grid over
 * a query of one such table reads one page at a time, the way the data provider
 * serves its own grids. A sort reaches the data provider as a
 * {@link QuerySortOrder} on the property of the column's name and, for an
 * in-memory data provider, as a comparator that places null values after all
 * others when ascending; a backend data provider places null values its own
 * way. Filters are not pushed down, because a data provider's filter type is
 * its own: a query that filters reads every item and filters in the engine.
 * </p>
 *
 * @param <T>
 *            the item type of the data provider
 * @author Vaadin Ltd
 * @since 25.4
 */
public class DataProviderTableSource<T> implements TableSource {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(String.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            Boolean.class, LocalDate.class, LocalDateTime.class,
            LocalTime.class);

    private final String name;
    private final Class<T> itemType;
    private final DataProvider<T, ?> dataProvider;
    private final List<Column> columns;

    /**
     * Creates a source over a data provider.
     *
     * @param name
     *            the table name the LLM uses, not {@code null}
     * @param itemType
     *            the item type of the data provider, not {@code null}
     * @param dataProvider
     *            the data provider, not {@code null}
     * @throws IllegalArgumentException
     *             if the item type has no property a table can hold
     */
    public DataProviderTableSource(String name, Class<T> itemType,
            DataProvider<T, ?> dataProvider) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.itemType = Objects.requireNonNull(itemType,
                "itemType must not be null");
        this.dataProvider = Objects.requireNonNull(dataProvider,
                "dataProvider must not be null");
        var columns = new ArrayList<Column>();
        properties().sorted(Comparator.comparing(PropertyDefinition::getName))
                .forEach(property -> {
                    var type = boxed(property.getType());
                    if (SUPPORTED_TYPES.contains(type)) {
                        columns.add(new Column(property.getName(), type));
                    }
                });
        if (columns.isEmpty()) {
            throw new IllegalArgumentException(
                    itemType.getName() + " has no property a table can hold");
        }
        this.columns = List.copyOf(columns);
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
    public String getDescription() {
        return "Flow data provider of " + itemType.getSimpleName();
    }

    @Override
    public Set<Capability> getCapabilities() {
        return Set.of(Capability.SORT, Capability.LIMIT, Capability.COUNT);
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var getters = getters();
        var sortOrders = pushdown.sort().stream()
                .map(key -> new QuerySortOrder(key.column(),
                        key.descending() ? SortDirection.DESCENDING
                                : SortDirection.ASCENDING))
                .toList();
        var limit = pushdown.limit() == null ? Integer.MAX_VALUE
                : (int) Math.min(pushdown.limit(), Integer.MAX_VALUE);
        var query = new Query<T, Object>(
                (int) Math.min(pushdown.offset(), Integer.MAX_VALUE), limit,
                sortOrders, comparator(pushdown.sort(), getters), null);
        return fetch(dataProvider, query).map(item -> {
            var row = new LinkedHashMap<String, Object>();
            for (var column : columns) {
                row.put(column.name(),
                        getters.get(column.name()).getGetter().apply(item));
            }
            return (Map<String, Object>) row;
        }).toList();
    }

    @Override
    public long count(Pushdown pushdown) {
        return size(dataProvider);
    }

    @SuppressWarnings("unchecked")
    private static <T, F> Stream<T> fetch(DataProvider<T, F> dataProvider,
            Query<T, Object> query) {
        return dataProvider.fetch((Query<T, F>) (Query<T, ?>) query);
    }

    private static <T, F> int size(DataProvider<T, F> dataProvider) {
        return dataProvider.size(new Query<>());
    }

    private Map<String, PropertyDefinition<T, ?>> getters() {
        var getters = new LinkedHashMap<String, PropertyDefinition<T, ?>>();
        properties()
                .forEach(property -> getters.put(property.getName(), property));
        return getters;
    }

    /**
     * Returns the order of the sort keys as a comparator of items, for an
     * in-memory data provider, which sorts with the query's comparator rather
     * than its sort orders. Null values sort as the greatest values.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> Comparator<T> comparator(List<SortKey> sort,
            Map<String, PropertyDefinition<T, ?>> getters) {
        if (sort.isEmpty()) {
            return null;
        }
        Comparator<T> result = null;
        for (var key : sort) {
            var getter = getters.get(key.column()).getGetter();
            Comparator<T> byKey = Comparator.comparing(
                    item -> (Comparable) getter.apply(item),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            if (key.descending()) {
                byKey = byKey.reversed();
            }
            result = result == null ? byKey : result.thenComparing(byKey);
        }
        return result;
    }

    private Stream<PropertyDefinition<T, ?>> properties() {
        PropertySet<T> propertySet = BeanPropertySet.get(itemType);
        return propertySet.getProperties()
                .filter(property -> !property.isSubProperty());
    }

    private static Class<?> boxed(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        }
        return type;
    }
}
