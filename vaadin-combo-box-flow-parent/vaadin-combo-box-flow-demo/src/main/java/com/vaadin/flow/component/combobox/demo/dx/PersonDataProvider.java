package com.vaadin.flow.component.combobox.demo.dx;

import java.util.Collection;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.demo.data.PersonData;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

public class PersonDataProvider implements InMemoryDataProvider<Person> {

    private final Collection<Person> persons = new PersonData().getPersons();

    private SerializablePredicate<Person> filter = person -> true;

    private SerializableComparator<Person> comparator = (p1, p2) -> {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException();
        }
        return p1.toString().compareToIgnoreCase(p2.toString());
    };

    @Override
    public SerializablePredicate<Person> getFilter() {
        return filter;
    }

    @Override
    public void setFilter(SerializablePredicate<Person> filter) {
        this.filter = filter;
    }

    @Override
    public SerializableComparator<Person> getSortComparator() {
        return comparator;
    }

    @Override
    public void setSortComparator(SerializableComparator<Person> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int size(Query<Person, SerializablePredicate<Person>> query) {
        return (int) persons.stream()
                .filter(filter.and(query.getFilter().orElse(p -> true)))
                .count();
    }

    @Override
    public Stream<Person> fetch(
            Query<Person, SerializablePredicate<Person>> query) {
        return persons.stream()
                .filter(filter.and(query.getFilter().orElse(p -> true)))
                .sorted(comparator).skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public void refreshItem(Person item) {
    }

    @Override
    public void refreshAll() {
    }

    @Override
    public Registration addDataProviderListener(
            DataProviderListener<Person> listener) {
        return () -> {};
    }
}
