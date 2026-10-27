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

import com.vaadin.flow.component.ai.provider.TableSource;
import com.vaadin.flow.function.SerializableSupplier;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * One JPA entity as a table, read with JPQL. The columns are the entity's basic
 * attributes and, for each to-one association, the id of the associated entity
 * as {@code <attribute>Id}, in name order. Equality filters on any column are
 * pushed down into the {@code WHERE} clause of the JPQL query, so the JPA
 * endpoint returns only the matching rows.
 */
public class JpqlTableSource implements TableSource {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(String.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            Boolean.class, LocalDate.class, LocalDateTime.class,
            LocalTime.class);

    private final SerializableSupplier<EntityManagerFactory> entityManagerFactory;
    private final String name;
    private final List<Column> columns;
    // The JPQL path of each column, from the entity alias e or from the alias
    // of the join of an association
    private final Map<String, String> paths = new LinkedHashMap<>();
    private final List<String> joinedAssociations = new ArrayList<>();

    /**
     * Creates a source over one entity of a persistence unit.
     *
     * @param entityManagerFactory
     *            supplies the entity manager factory of the persistence unit;
     *            called on every fetch
     * @param entityClass
     *            the entity class
     * @throws IllegalArgumentException
     *             if the class is not an entity of the persistence unit, or has
     *             no attribute a table can hold
     */
    public JpqlTableSource(
            SerializableSupplier<EntityManagerFactory> entityManagerFactory,
            Class<?> entityClass) {
        this.entityManagerFactory = Objects
                .requireNonNull(entityManagerFactory);
        EntityType<?> entityType = entityManagerFactory.get().getMetamodel()
                .entity(entityClass);
        name = entityType.getName();
        var found = new ArrayList<Column>();
        for (var attribute : entityType.getSingularAttributes()) {
            var column = column(attribute);
            if (column != null) {
                found.add(column);
            }
        }
        if (found.isEmpty()) {
            throw new IllegalArgumentException(entityClass.getName()
                    + " has no attribute a table can hold");
        }
        found.sort(Comparator.comparing(Column::name));
        columns = List.copyOf(found);
    }

    /**
     * Returns the column of an attribute and records its JPQL path, or returns
     * {@code null} for an attribute a table cannot hold.
     */
    private Column column(SingularAttribute<?, ?> attribute) {
        var type = attribute.getPersistentAttributeType();
        if (type == Attribute.PersistentAttributeType.BASIC) {
            var javaType = boxed(attribute.getJavaType());
            if (!SUPPORTED_TYPES.contains(javaType)) {
                return null;
            }
            paths.put(attribute.getName(), "e." + attribute.getName());
            return new Column(attribute.getName(), javaType);
        }
        if ((type == Attribute.PersistentAttributeType.MANY_TO_ONE
                || type == Attribute.PersistentAttributeType.ONE_TO_ONE)
                && attribute.getType() instanceof EntityType<?> target) {
            var id = target.getSingularAttributes().stream()
                    .filter(SingularAttribute::isId).findFirst().orElse(null);
            if (id == null
                    || !SUPPORTED_TYPES.contains(boxed(id.getJavaType()))) {
                return null;
            }
            var alias = "j" + joinedAssociations.size();
            joinedAssociations.add(attribute.getName());
            var columnName = attribute.getName() + "Id";
            paths.put(columnName, alias + "." + id.getName());
            return new Column(columnName, boxed(id.getJavaType()));
        }
        // Embedded values and element collections are left out
        return null;
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
        return Set.of(Capability.EQUALITY_FILTER);
    }

    @Override
    public String getDescription() {
        return "JPA entity " + name + ", read with JPQL";
    }

    @Override
    public List<Map<String, Object>> fetch(Pushdown pushdown) {
        var jpql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            jpql.append(i == 0 ? "" : ", ")
                    .append(paths.get(columns.get(i).name())).append(" AS c")
                    .append(i);
        }
        jpql.append(" FROM ").append(name).append(" e");
        for (int i = 0; i < joinedAssociations.size(); i++) {
            jpql.append(" LEFT JOIN e.").append(joinedAssociations.get(i))
                    .append(" j").append(i);
        }
        // A filter on a column this source does not know is left to the engine
        var filters = pushdown.equalityFilters().entrySet().stream()
                .filter(filter -> paths.containsKey(filter.getKey())).toList();
        for (int i = 0; i < filters.size(); i++) {
            jpql.append(i == 0 ? " WHERE " : " AND ")
                    .append(paths.get(filters.get(i).getKey())).append(" = :p")
                    .append(i);
        }
        try (var entityManager = entityManagerFactory.get()
                .createEntityManager()) {
            var query = entityManager.createQuery(jpql.toString(), Tuple.class);
            for (int i = 0; i < filters.size(); i++) {
                query.setParameter("p" + i, filters.get(i).getValue());
            }
            var rows = new ArrayList<Map<String, Object>>();
            for (var tuple : query.getResultList()) {
                var row = new LinkedHashMap<String, Object>();
                for (int i = 0; i < columns.size(); i++) {
                    row.put(columns.get(i).name(), tuple.get(i));
                }
                rows.add(row);
            }
            return rows;
        }
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
