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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.ai.tests.endpoints.SampleData;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;
import jakarta.persistence.Tuple;
import jakarta.persistence.metamodel.EntityType;

/**
 * A {@link DatabaseProvider} over a JPA persistence unit for applications that
 * have entities rather than a hand-written schema: the schema is derived from
 * the entity mappings ({@code @Table}, {@code @Column}, {@code @JoinColumn}),
 * and the SQL the LLM writes runs as a native query through the entity manager,
 * so no second connection is configured.
 */
public class JpaNativeDatabaseProvider implements DatabaseProvider {

    private final transient Supplier<EntityManagerFactory> entityManagerFactory;

    /**
     * Creates a provider over the persistence unit the supplier returns.
     *
     * @param entityManagerFactory
     *            supplies the entity manager factory; called on every use
     */
    public JpaNativeDatabaseProvider(
            Supplier<EntityManagerFactory> entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public String getSchema() {
        var schema = new StringBuilder(
                "DATABASE SCHEMA (from the JPA entity mappings; query it with SQL):\n\n");
        var entities = new ArrayList<>(
                entityManagerFactory.get().getMetamodel().getEntities());
        entities.sort(
                Comparator.comparing(JpaNativeDatabaseProvider::tableName));
        for (var entity : entities) {
            schema.append("Table: ").append(tableName(entity)).append('\n')
                    .append("Columns:\n");
            for (var field : entity.getJavaType().getDeclaredFields()) {
                describeColumn(schema, field);
            }
            schema.append('\n');
        }
        schema.append("NOTES:\n").append(SampleData.NOTES);
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        var upper = sql.trim().toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            throw new ToolException("Only SELECT queries are allowed.");
        }
        try (var entityManager = entityManagerFactory.get()
                .createEntityManager()) {
            @SuppressWarnings("unchecked")
            List<Tuple> tuples = entityManager
                    .createNativeQuery(sql, Tuple.class).getResultList();
            var rows = new ArrayList<Map<String, Object>>(tuples.size());
            for (var tuple : tuples) {
                var row = new LinkedHashMap<String, Object>();
                var elements = tuple.getElements();
                for (int i = 0; i < elements.size(); i++) {
                    row.put(elements.get(i).getAlias(), tuple.get(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (PersistenceException e) {
            var cause = sqlCause(e);
            if (cause != null && cause.getSQLState() != null
                    && cause.getSQLState().startsWith("42")) {
                // Syntax errors and unknown names quote only the query
                throw new ToolException(cause.getMessage(), e);
            }
            throw new IllegalStateException("Failed to execute query", e);
        }
    }

    private static SQLException sqlCause(Throwable e) {
        for (var cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                return sqlException;
            }
        }
        return null;
    }

    private static String tableName(EntityType<?> entity) {
        var table = entity.getJavaType().getAnnotation(Table.class);
        return table != null && !table.name().isEmpty() ? table.name()
                : entity.getName();
    }

    private static void describeColumn(StringBuilder schema, Field field) {
        var column = field.getAnnotation(Column.class);
        var joinColumn = field.getAnnotation(JoinColumn.class);
        if (Modifier.isStatic(field.getModifiers())
                || Collection.class.isAssignableFrom(field.getType())) {
            // Static fields and inverse collections have no column
            return;
        }
        String name;
        String type;
        if (joinColumn != null) {
            name = joinColumn.name().isEmpty() ? field.getName() + "_id"
                    : joinColumn.name();
            var target = field.getType().getAnnotation(Table.class);
            var referenced = joinColumn.referencedColumnName().isEmpty() ? "id"
                    : joinColumn.referencedColumnName();
            type = (referenced.equals("id") ? "INT" : "VARCHAR")
                    + ", references "
                    + (target == null ? field.getType().getSimpleName()
                            : target.name())
                    + "(" + referenced + ")";
        } else {
            name = column != null && !column.name().isEmpty() ? column.name()
                    : field.getName();
            type = sqlType(field.getType(), column);
        }
        schema.append("  - ").append(name).append(" (").append(type)
                .append(')');
        if (field.isAnnotationPresent(Id.class)) {
            schema.append(" PRIMARY KEY");
        } else if (field.getType().isPrimitive()
                || column != null && !column.nullable()) {
            schema.append(" NOT NULL");
        }
        if (column != null && column.unique()) {
            schema.append(" UNIQUE");
        }
        if (name.startsWith("\"")) {
            schema.append(" -- quoted identifier, write it exactly like this");
        }
        schema.append('\n');
    }

    private static String sqlType(Class<?> javaType, Column column) {
        if (javaType == int.class || javaType == Integer.class) {
            return "INT";
        }
        if (javaType == long.class || javaType == Long.class) {
            return "BIGINT";
        }
        if (javaType == String.class) {
            return "VARCHAR(" + (column == null ? 255 : column.length()) + ")";
        }
        if (javaType == BigDecimal.class) {
            return column == null ? "DECIMAL"
                    : "DECIMAL(" + column.precision() + ", " + column.scale()
                            + ")";
        }
        if (javaType == LocalDate.class) {
            return "DATE";
        }
        if (javaType == LocalDateTime.class) {
            return "TIMESTAMP";
        }
        if (javaType == boolean.class || javaType == Boolean.class) {
            return "BOOLEAN";
        }
        if (javaType == double.class || javaType == Double.class) {
            return "DOUBLE";
        }
        return javaType.getSimpleName().toUpperCase(Locale.ROOT);
    }
}
