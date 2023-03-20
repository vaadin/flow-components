/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

/**
 * A dummy data provider. DO NOT DO THIS IN A PRODUCTION APP!
 */
class PersonCrudDataProvider
        extends AbstractBackEndDataProvider<Person, CrudFilter> {

    // A real app should hook up something like JPA
    private List<Person> database = generatePersonsList();

    public static List<Person> generatePersonsList() {
        return Stream
                .of(new Person(1, "Sayo", "Sayo"),
                        new Person(2, "Manolo", "Otto"),
                        new Person(3, "Guille", "Guille"))
                .collect(Collectors.toList());
    }

    private Consumer<Long> sizeChangeListener;

    private List<Person> getDatabaseCopy() {
        List<Person> dbCopy = new ArrayList<>(database.size());
        database.forEach(item -> dbCopy.add(item.clone()));
        return dbCopy;
    }

    void setDatabase(List<Person> database) {
        this.database = database;
    }

    @Override
    protected Stream<Person> fetchFromBackEnd(Query<Person, CrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();

        Stream<Person> stream = getDatabaseCopy().stream();

        if (query.getFilter().isPresent()) {
            stream = stream.filter(predicate(query.getFilter().get()))
                    .sorted(comparator(query.getFilter().get()));
        }

        return stream.skip(offset).limit(limit);
    }

    @Override
    protected int sizeInBackEnd(Query<Person, CrudFilter> query) {
        // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
        long count = fetchFromBackEnd(query).count();

        if (sizeChangeListener != null) {
            sizeChangeListener.accept(count);
        }

        return (int) count;
    }

    void setSizeChangeListener(Consumer<Long> listener) {
        sizeChangeListener = listener;
    }

    private Predicate<Person> predicate(CrudFilter filter) {
        // For RDBMS just generate a WHERE clause
        return filter.getConstraints().entrySet().stream()
                .map(constraint -> (Predicate<Person>) person -> {
                    try {
                        Object value = valueOf(constraint.getKey(), person);
                        return value != null && value.toString().toLowerCase()
                                .contains(constraint.getValue().toLowerCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }).reduce(Predicate::and).orElse(e -> true);
    }

    private Comparator<Person> comparator(CrudFilter filter) {
        // For RDBMS just generate an ORDER BY clause
        return filter.getSortOrders().entrySet().stream().map(sortClause -> {
            try {
                Comparator<Person> comparator = Comparator.comparing(
                        person -> (Comparable) valueOf(sortClause.getKey(),
                                person));

                if (sortClause.getValue() == SortDirection.DESCENDING) {
                    comparator = comparator.reversed();
                }

                return comparator;
            } catch (Exception ex) {
                return (Comparator<Person>) (o1, o2) -> 0;
            }
        }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
    }

    private Object valueOf(String fieldName, Person person) {
        try {
            Field field = Person.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(person);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void persist(Person item) {
        if (item.getId() == null) {
            item.setId(database.stream().map(Person::getId).max(naturalOrder())
                    .orElse(0) + 1);
        }

        final Optional<Person> existingItem = find(item.getId());
        if (existingItem.isPresent()) {
            int position = database.indexOf(existingItem.get());
            database.remove(existingItem.get());
            database.add(position, item);
        } else {
            database.add(item);
        }
    }

    Optional<Person> find(Integer id) {
        return database.stream().filter(entity -> entity.getId().equals(id))
                .findFirst();
    }

    void delete(Person item) {
        database.removeIf(entity -> entity.getId().equals(item.getId()));
    }
}
