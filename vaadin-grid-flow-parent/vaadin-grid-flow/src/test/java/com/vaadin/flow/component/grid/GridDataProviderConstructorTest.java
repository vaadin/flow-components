package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test for Grid constructors with DataProvider and Collection as parameter
 *
 * @author Vaadin Ltd.
 */
public class GridDataProviderConstructorTest {

    transient List<Person> people = Arrays.asList(
            new Person(1, "Mik", "Gir", "mik@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(10000),
                    "Dev"),
            new Person(2, "Mak", "Gar", "mak@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(20000),
                    "Dev"),
            new Person(3, "Muk", "Gur", "muk@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(30000),
                    "Dev"),
            new Person(4, "Mok", "Gor", "mok@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(40000),
                    "Dev"));

    @Test
    public void constructorBackEndDataProvider() {
        Grid<Person> gridBackendDataProvider = createGrid(
                new PersonBackendDataProvider());
        Assert.assertEquals(people.get(0),
                gridBackendDataProvider.getLazyDataView().getItem(0));
        Assert.assertEquals(people.get(1),
                gridBackendDataProvider.getLazyDataView().getItem(1));
        Assert.assertEquals(people.get(2),
                gridBackendDataProvider.getLazyDataView().getItem(2));
        Assert.assertEquals(people.get(3),
                gridBackendDataProvider.getLazyDataView().getItem(3));
    }

    @Test
    public void constructorGenericDataProvider() {
        Grid<Person> gridGenericDataProvider = createGrid(
                new PersonGenericDataProvider());
        Assert.assertEquals(people.get(0),
                gridGenericDataProvider.getGenericDataView().getItem(0));
        Assert.assertEquals(people.get(1),
                gridGenericDataProvider.getGenericDataView().getItem(1));
        Assert.assertEquals(people.get(2),
                gridGenericDataProvider.getGenericDataView().getItem(2));
        Assert.assertEquals(people.get(3),
                gridGenericDataProvider.getGenericDataView().getItem(3));
    }

    @Test
    public void constructorInMemoryDataProvider() {
        Grid<Person> gridInMemoryDataProvider = createGrid(
                new PersonInMemoryDataProvider());
        Assert.assertEquals(people.get(0),
                gridInMemoryDataProvider.getGenericDataView().getItem(0));
        Assert.assertEquals(people.get(1),
                gridInMemoryDataProvider.getGenericDataView().getItem(1));
        Assert.assertEquals(people.get(2),
                gridInMemoryDataProvider.getGenericDataView().getItem(2));
        Assert.assertEquals(people.get(3),
                gridInMemoryDataProvider.getGenericDataView().getItem(3));
    }

    @Test
    public void constructorListDataProvider() {
        Grid<Person> gridListDataProvider = createGrid(
                new PersonListDataProvider());
        Assert.assertEquals(people.get(0),
                gridListDataProvider.getListDataView().getItem(0));
        Assert.assertEquals(people.get(1),
                gridListDataProvider.getListDataView().getItem(1));
        Assert.assertEquals(people.get(2),
                gridListDataProvider.getListDataView().getItem(2));
        Assert.assertEquals(people.get(3),
                gridListDataProvider.getListDataView().getItem(3));
    }

    @Test
    public void constructorCollection() {
        Grid<Person> gridCollection = createGrid(people);
        Assert.assertEquals(people.get(0),
                gridCollection.getGenericDataView().getItem(0));
        Assert.assertEquals(people.get(1),
                gridCollection.getGenericDataView().getItem(1));
        Assert.assertEquals(people.get(2),
                gridCollection.getGenericDataView().getItem(2));
        Assert.assertEquals(people.get(3),
                gridCollection.getGenericDataView().getItem(3));
    }

    private final Grid<Person> createGrid(DataProvider dataProvider) {
        Grid<Person> grid = new Grid<>(dataProvider);
        return createColumns(grid);
    }

    private final Grid<Person> createGrid(Collection<Person> items) {
        Grid<Person> grid = new Grid<>(items);
        return createColumns(grid);
    }

    private final Grid<Person> createColumns(Grid<Person> grid) {
        grid.addColumn(Person::getFirstName).setHeader("First name")
                .setKey("first");
        grid.addColumn(Person::getLastName).setHeader("Last name")
                .setKey("last");
        grid.addColumn(Person::getEmail).setHeader("Email").setKey("email");
        grid.addColumn(Person::getProfession).setHeader("Profession")
                .setKey("profession");
        grid.addColumn(new LocalDateRenderer<>(Person::getBirthday,
                () -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Date Renderer").setKey("date");
        grid.addColumn(
                new LocalDateRenderer<>(Person::getBirthday, "dd/MM/yyyy"))
                .setHeader("String Renderer").setKey("renderer");
        return grid;
    }

    public final static class Person {

        private String firstName;

        private String lastName;

        private String email;

        private LocalDate birthday;

        private Integer id;

        private String profession;

        public Person(Integer id, String firstName, String lastName,
                String email, LocalDate birthday, String profession) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.birthday = birthday;
            this.id = id;
            this.profession = profession;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getProfession() {
            return profession;
        }

        public void setProfession(String profession) {
            this.profession = profession;
        }
    }

    private final class PersonBackendDataProvider
            implements BackEndDataProvider<Person, Void> {

        @Override
        public void setSortOrders(List<QuerySortOrder> list) {
            return;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Person, Void> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(Query<Person, Void> query) {
            query.getPage();
            query.getPageSize();
            return people.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

        @Override
        public void refreshItem(Person person) {
            return;
        }

        @Override
        public void refreshItems(boolean b) {
            return;
        }

        @Override
        public void refreshAll() {
            return;
        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }

    }

    private final class PersonListDataProvider
            extends ListDataProvider<Person> {

        public PersonListDataProvider() {
            super(people);
        }

    }

    private final class PersonInMemoryDataProvider
            implements InMemoryDataProvider<Person> {

        @Override
        public SerializablePredicate<Person> getFilter() {
            return null;
        }

        @Override
        public void setFilter(
                SerializablePredicate<Person> serializablePredicate) {

        }

        @Override
        public SerializableComparator<Person> getSortComparator() {
            return null;
        }

        @Override
        public void setSortComparator(
                SerializableComparator<Person> serializableComparator) {
            return;
        }

        @Override
        public int size(Query<Person, SerializablePredicate<Person>> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(
                Query<Person, SerializablePredicate<Person>> query) {
            query.getPage();
            query.getPageSize();
            return people.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

        @Override
        public void refreshItem(Person person) {
            return;
        }

        @Override
        public void refreshAll() {
            return;
        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }

        @Override
        public void refreshItems(boolean b) {
            return;
        }
    }

    private final class PersonGenericDataProvider
            implements DataProvider<Person, Void> {

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Person, Void> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(Query<Person, Void> query) {
            query.getPage();
            query.getPageSize();
            return people.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

        @Override
        public void refreshItem(Person person) {
            return;
        }

        @Override
        public void refreshAll() {
            return;
        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }

        @Override
        public void refreshItems(boolean b) {
            return;
        }
    }
}
