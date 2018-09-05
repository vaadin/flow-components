package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.SimpleCrudFilter;
import com.vaadin.flow.component.crud.Util;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

class PersonCrudDataProvider extends AbstractBackEndDataProvider<Person, SimpleCrudFilter> {

    // A real app should hook up something like JPA
    private static final List<Person> DATABASE = IntStream
            .rangeClosed(1, 10)
            .mapToObj(i -> new Person(i, randomName() + " " + i))
            .collect(toList());

    @Override
    protected Stream<Person> fetchFromBackEnd(Query<Person, SimpleCrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();

        Stream<Person> stream = DATABASE.stream();

        if (query.getFilter().isPresent()) {
            stream = stream
                    .filter(predicate(query.getFilter().get()))
                    .sorted(comparator(query.getFilter().get()));
        }

        return stream.skip(offset).limit(limit);
    }

    @Override
    protected int sizeInBackEnd(Query<Person, SimpleCrudFilter> query) {
        // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
        return (int) fetchFromBackEnd(query).count();
    }

    private Predicate<Person> predicate(SimpleCrudFilter filter) {
        // For RDBMS just generate a WHERE clause
        return filter.getConstraints().entrySet().stream()
                .map(constraint -> (Predicate<Person>) person -> {
                    try {
                        Object value = valueOf(constraint.getKey(), person);
                        return value != null && value.toString().startsWith(constraint.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .reduce(Predicate::and)
                .orElse(e -> true);
    }

    private Comparator<Person> comparator(SimpleCrudFilter filter) {
        // For RDBMS just generate an ORDER BY clause
        return filter.getSortOrders().entrySet().stream()
                .map(sortClause -> {
                    try {
                        Comparator<Person> comparator
                                = Comparator.comparing(person ->
                                (Comparable) valueOf(sortClause.getKey(), person));

                        if (sortClause.getValue() == SortDirection.DESCENDING) {
                            comparator = comparator.reversed();
                        }

                        return comparator;
                    } catch (Exception ex) {
                        return (Comparator<Person>) (o1, o2) -> 0;
                    }
                })
                .reduce(Comparator::thenComparing)
                .orElse((o1, o2) -> 0);
    }

    private Object valueOf(String fieldName, Person person) {
        try {
            Field field = Person.class.getDeclaredField(fieldName);
            Object value;
            try {
                Method getter = Util.getterFor(field, Person.class);
                value = getter.invoke(person);
            } catch (Exception ex) {
                field.setAccessible(true);
                value = field.get(person);
            }

            return value;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void persist(Person item) {
        if (item.getId() == null) {
            item.setId(DATABASE
                    .stream()
                    .map(Person::getId)
                    .max(naturalOrder())
                    .orElse(0) + 1);
        }

        final Optional<Person> existingItem = find(item.getId());
        if (existingItem.isPresent()) {
            int position = DATABASE.indexOf(existingItem.get());
            DATABASE.remove(existingItem.get());
            DATABASE.add(position, item);
        } else {
            DATABASE.add(item);
        }
    }

    Optional<Person> find(Integer id) {
        return DATABASE
                .stream()
                .filter(entity -> entity.getId().equals(id))
                .findFirst();
    }

    void delete(Integer id) {
        DATABASE.removeIf(entity -> entity.getId().equals(id));
    }

    private static String randomName() {
        Random random = new Random();
        int length = 4 + random.nextInt(6);
        StringBuilder result = new StringBuilder();

        for (int a = 0; a < length; a++) {
            result.append((char) ('A' + random.nextInt(26)));
        }

        return result.toString();
    }
}
