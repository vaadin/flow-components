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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.provider.QueryableProvider;
import com.vaadin.flow.component.ai.provider.ToolException;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * A {@link QueryableProvider} over a JPA persistence unit in which the LLM
 * writes JPQL. The schema is derived from the JPA metamodel in entity terms:
 * entity names, attributes with their Java types, and associations. Results are
 * rows keyed by the {@code AS} aliases of the query; a selected entity is
 * expanded into its basic attributes.
 */
public class JpqlQueryableProvider implements QueryableProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(JpqlQueryableProvider.class);

    private final transient Supplier<EntityManagerFactory> entityManagerFactory;
    private final String notes;

    /**
     * Creates a provider over the persistence unit the supplier returns.
     *
     * @param entityManagerFactory
     *            supplies the entity manager factory; called on every use
     * @param notes
     *            notes about the data appended to the schema, or {@code null}
     */
    public JpqlQueryableProvider(
            Supplier<EntityManagerFactory> entityManagerFactory, String notes) {
        this.entityManagerFactory = entityManagerFactory;
        this.notes = notes == null ? "" : notes;
    }

    @Override
    public QueryLanguage getQueryLanguage() {
        return JpqlQueryLanguage.INSTANCE;
    }

    @Override
    public String getSchema() {
        var schema = new StringBuilder(
                "JPA ENTITIES (query them with JPQL by these names):\n\n");
        var entities = new ArrayList<>(
                entityManagerFactory.get().getMetamodel().getEntities());
        entities.sort(Comparator.comparing(EntityType::getName));
        for (var entity : entities) {
            schema.append(entity.getName()).append('\n');
            for (var attribute : attributesInDeclarationOrder(entity)) {
                schema.append("  - ").append(attribute.getName()).append(": ")
                        .append(describeType(attribute)).append('\n');
            }
            schema.append('\n');
        }
        if (!notes.isBlank()) {
            schema.append("NOTES:\n").append(notes);
        }
        ProviderLog.schema(LOGGER, schema.toString());
        return schema.toString();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        ProviderLog.query(LOGGER, getQueryLanguage(), query);
        if (!query.trim().toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            throw relayed(
                    new ToolException("Only SELECT queries are allowed."));
        }
        try (var entityManager = entityManagerFactory.get()
                .createEntityManager()) {
            List<Tuple> tuples;
            try {
                tuples = entityManager.createQuery(query, Tuple.class)
                        .getResultList();
            } catch (IllegalArgumentException e) {
                // JPQL syntax and semantic errors quote the query itself;
                // Hibernate prefixes some with its exception class name
                throw relayed(new ToolException(
                        e.getMessage().replaceFirst("^[\\w.]+Exception: ", ""),
                        e));
            }
            var rows = new ArrayList<Map<String, Object>>(tuples.size());
            for (var tuple : tuples) {
                rows.add(toRow(tuple));
            }
            ProviderLog.rows(LOGGER, rows.size());
            return rows;
        }
    }

    private static ToolException relayed(ToolException e) {
        ProviderLog.relayed(LOGGER, e);
        return e;
    }

    private Map<String, Object> toRow(Tuple tuple) {
        var row = new LinkedHashMap<String, Object>();
        var elements = tuple.getElements();
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            var value = tuple.get(i);
            var alias = element.getAlias() != null ? element.getAlias()
                    : "column" + (i + 1);
            if (value != null && isEntity(value.getClass())) {
                var prefix = elements.size() > 1 ? alias + "." : "";
                basicAttributes(value).forEach((name, attributeValue) -> row
                        .put(prefix + name, attributeValue));
            } else {
                row.put(alias, value);
            }
        }
        return row;
    }

    private boolean isEntity(Class<?> type) {
        return entityManagerFactory.get().getMetamodel().getEntities().stream()
                .anyMatch(
                        entity -> entity.getJavaType().isAssignableFrom(type));
    }

    private Map<String, Object> basicAttributes(Object entity) {
        var type = entityManagerFactory.get().getMetamodel()
                .entity(entity.getClass());
        var values = new LinkedHashMap<String, Object>();
        for (var attribute : attributesInDeclarationOrder(type)) {
            if (attribute.isAssociation() || attribute.isCollection()) {
                continue;
            }
            if (attribute.getJavaMember() instanceof Field field) {
                try {
                    field.setAccessible(true);
                    values.put(attribute.getName(), field.get(entity));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Cannot read " + attribute.getName(), e);
                }
            }
        }
        return values;
    }

    private static List<Attribute<?, ?>> attributesInDeclarationOrder(
            EntityType<?> entity) {
        var byName = new LinkedHashMap<String, Attribute<?, ?>>();
        for (var attribute : entity.getAttributes()) {
            byName.put(attribute.getName(), attribute);
        }
        var ordered = new ArrayList<Attribute<?, ?>>();
        for (var field : entity.getJavaType().getDeclaredFields()) {
            var attribute = byName.remove(field.getName());
            if (attribute != null) {
                ordered.add(attribute);
            }
        }
        ordered.addAll(byName.values());
        return ordered;
    }

    private static String describeType(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute<?, ?, ?> plural) {
            return "collection of "
                    + plural.getElementType().getJavaType().getSimpleName()
                    + " (one-to-many association)";
        }
        if (attribute.isAssociation()
                && attribute instanceof SingularAttribute<?, ?> singular) {
            return singular.getType().getJavaType().getSimpleName()
                    + " (many-to-one association, navigate with a dot)";
        }
        var type = attribute.getJavaType().getSimpleName();
        return attribute instanceof SingularAttribute<?, ?> singular
                && singular.isId() ? type + ", id" : type;
    }
}
