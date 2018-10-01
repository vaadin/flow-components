package com.vaadin.flow.component.crud.vaadincom;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.crud.vaadincom.CrudView.Person;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * A dummy data provider. DO NOT DO THIS IN A PRODUCTION APP!
 */
public class PersonDataProvider extends AbstractBackEndDataProvider<Person, CrudFilter> {

    private static final String[] FIRSTS = {"James", "Mary", "John", "Patricia", "Robert", "Jennifer"};
    private static final String[] LASTS = {"Smith", "Johnson", "Williams", "Brown"};

    // A real app should hook up something like JPA
    private final List<Person> DATABASE = IntStream
            .rangeClosed(1, 200)
            .mapToObj(i -> new Person(i, FIRSTS[i % FIRSTS.length], LASTS[i % LASTS.length]))
            .collect(toList());

    private Consumer<Long> sizeChangeListener;

    @Override
    protected Stream<Person> fetchFromBackEnd(Query<Person, CrudFilter> query) {
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
    protected int sizeInBackEnd(Query<Person, CrudFilter> query) {
        // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
        long count = fetchFromBackEnd(query).count();

        if (sizeChangeListener != null) {
            sizeChangeListener.accept(count);
        }

        return (int) count;
    }

    public void setSizeChangeListener(Consumer<Long> listener) {
        sizeChangeListener = listener;
    }

    private Predicate<Person> predicate(CrudFilter filter) {
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

    private Comparator<Person> comparator(CrudFilter filter) {
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
            field.setAccessible(true);
            return field.get(person);
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

    void delete(Person item) {
        DATABASE.removeIf(entity -> entity.getId().equals(item.getId()));
    }
}
