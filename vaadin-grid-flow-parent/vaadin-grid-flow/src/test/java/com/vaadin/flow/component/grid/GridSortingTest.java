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
package com.vaadin.flow.component.grid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class GridSortingTest {

    public static class Person {
        private int id;
        private String name;
        private int age;
        private Address address;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Person)) {
                return false;
            }
            Person other = (Person) obj;
            return id == other.id;
        }

        @Override
        public String toString() {
            return String.format("Person [name=%s, age=%s]", name, age);
        }
    }

    public static class Address {
        private String street;
        private int number;
        private String postalCode;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }

    private static class TestSortListener<T> implements
            ComponentEventListener<SortEvent<Grid<T>, GridSortOrder<T>>> {

        public List<SortEvent<Grid<T>, GridSortOrder<T>>> events = new ArrayList<>();

        @Override
        public void onComponentEvent(
                SortEvent<Grid<T>, GridSortOrder<T>> event) {
            events.add(event);
        }
    }

    private Grid<Person> grid;
    private Column<Person> nameColumn;
    private Column<Person> ageColumn;
    private Column<Person> templateColumn;

    private TestSortListener<Person> testSortListener;

    @BeforeEach
    void setup() {
        testSortListener = new TestSortListener<>();

        grid = new Grid<>();
        grid.setItems(new ArrayList<>());
        grid.addSortListener(testSortListener);

        nameColumn = grid.addColumn(Person::getName, "name").setHeader("Name");
        ageColumn = grid.addColumn(Person::getAge, "age").setHeader("Age");

        templateColumn = grid.addColumn(LitRenderer.<Person> of(
                "<div>${item.street}, number ${item.number}<br><small>${item.postalCode}</small></div>")
                .withProperty("street",
                        person -> person.getAddress().getStreet())
                .withProperty("number",
                        person -> person.getAddress().getNumber()))
                .setHeader("Address").setSortProperty("street", "number");
    }

    @Test
    void columnComparator_comaratorHandlesNullValues() {
        nameColumn.setComparator(Person::getName);

        Person person = new Person();
        person.setName("foo");

        SerializableComparator<Person> comparator = nameColumn
                .getComparator(SortDirection.ASCENDING);
        Assertions.assertEquals(1, comparator.compare(new Person(), person));
    }

    @Test
    void in_memory_sorting_applied_correctly() {
        setTestSorting();
        assertInMemorySorting(Comparator.comparing(Person::getName)
                .thenComparing(Comparator.comparing(Person::getAge).reversed())
                .thenComparing(person -> person.getAddress().getStreet())
                .thenComparing(person -> person.getAddress().getNumber()));
    }

    @Test
    void backend_sorting_applied_correctly() {
        setTestSorting();
        assertSortOrdersEquals(
                QuerySortOrder.asc("name").thenDesc("age").thenAsc("street")
                        .thenAsc("number").build(),
                grid.getDataCommunicator().getBackEndSorting());
    }

    @Test
    void sort_event_not_sent_for_same_data() {
        Assertions.assertEquals(0, testSortListener.events.size(),
                "Sort event list should have been empty at start.");
        callSortersChanged(JacksonUtils.createArrayNode());
        Assertions.assertEquals(0, testSortListener.events.size(),
                "No sort event should have been fired for empty initial sort.");
        setTestSorting();
        Assertions.assertEquals(1, testSortListener.events.size(),
                "Setting sort order should have fired event");
        setTestSorting();
        Assertions.assertEquals(1, testSortListener.events.size(),
                "No sort should have  fired for same sort order");

    }

    @Test
    void sort_event_correct() {
        Assertions.assertEquals(0, testSortListener.events.size());
        setTestSorting();
        Assertions.assertEquals(1, testSortListener.events.size());

        Assertions.assertEquals(1, testSortListener.events.size());
        assertSortOrdersEquals(
                GridSortOrder.asc(nameColumn).thenDesc(ageColumn)
                        .thenAsc(templateColumn).build(),
                testSortListener.events.get(0).getSortOrder());
        Assertions.assertTrue(testSortListener.events.get(0).isFromClient());
    }

    @Test
    void changing_sorters() {
        setTestSorting();

        ArrayNode secondSortersArray = JacksonUtils.createArrayNode();
        secondSortersArray
                .add(createSortObject(getColumnId(nameColumn), "desc"));
        callSortersChanged(secondSortersArray);

        Assertions.assertEquals(2, testSortListener.events.size());

        assertSortOrdersEquals(GridSortOrder.desc(nameColumn).build(),
                testSortListener.events.get(1).getSortOrder());
    }

    @Test
    void template_renderer_non_comparable_property() {
        Column<Person> column = grid.addColumn(LitRenderer.<Person> of("")
                .withProperty("address", Person::getAddress))
                .setSortProperty("address");
        ArrayNode sortersArray = JacksonUtils.createArrayNode();
        sortersArray.add(createSortObject(getColumnId(column), "asc"));
        callSortersChanged(sortersArray);

        // No in-memory sorting applied
        assertInMemorySorting((a, b) -> 0);
        // Backend sorting set correctly
        assertSortOrdersEquals(QuerySortOrder.asc("address").build(),
                grid.getDataCommunicator().getBackEndSorting());
    }

    @Test
    void checkMultiSortDefaultValue() {
        Assertions.assertFalse(grid.isMultiSort());
    }

    @Test
    void checkMultiSortFalse() {
        grid.setMultiSort(false);
        Assertions.assertFalse(grid.isMultiSort());
    }

    @Test
    void checkMultiSortTrue() {
        grid.setMultiSort(true);
        Assertions.assertTrue(grid.isMultiSort());
    }

    @Test
    void setMultiSortWithPriority() {
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);

        Assertions.assertEquals("append",
                grid.getElement().getAttribute("multi-sort-priority"));
    }

    @Test
    void setMultiSortWithPriorityNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.setMultiSort(true, null));
    }

    @Test
    void setMultiSortShiftClickOnly() {
        grid.setMultiSort(true, true);

        Assertions.assertTrue(
                grid.getElement().getProperty("multiSortOnShiftClick", false));
    }

    @Test
    void setMultiSortShiftClickOnly_onlyWhenMultiSortTrue() {
        grid.setMultiSort(false, true);

        Assertions.assertFalse(
                grid.getElement().getProperty("multiSortOnShiftClick", false));
    }

    @Test
    void setMultiSortShiftClickOnlyFalse_whenUsingMethodWithoutOnShiftClickOnly() {
        grid.setMultiSort(true, true);
        grid.setMultiSort(false);

        Assertions.assertFalse(
                grid.getElement().getProperty("multiSortOnShiftClick", false));
    }

    @Test
    void setMultiSortShiftClickOnlyWithPriority() {
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND, true);

        Assertions.assertEquals("append",
                grid.getElement().getAttribute("multi-sort-priority"));
        Assertions.assertTrue(
                grid.getElement().getProperty("multiSortOnShiftClick", false));
    }

    @Test
    void setMultiSortShiftClickOnlyWithPriorityNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.setMultiSort(true, null, true));
    }

    private void setTestSorting() {
        ArrayNode sortersArray = JacksonUtils.createArrayNode();
        sortersArray.add(createSortObject(getColumnId(nameColumn), "asc"));
        sortersArray.add(createSortObject(getColumnId(ageColumn), "desc"));
        sortersArray.add(createSortObject(getColumnId(templateColumn), "asc"));
        callSortersChanged(sortersArray);
    }

    private void callSortersChanged(ArrayNode json) {
        try {
            Method method = Grid.class.getDeclaredMethod("sortersChanged",
                    ArrayNode.class);
            method.setAccessible(true);
            method.invoke(grid, json);
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            Assertions.fail("Could not call Grid.sortersChanged");
        }
    }

    private String getColumnId(Column<?> column) {
        return column.getInternalId();
    }

    private ObjectNode createSortObject(String columnId, String direction) {
        ObjectNode json = JacksonUtils.createObjectNode();
        json.put("path", columnId);
        json.put("direction", direction);
        return json;
    }

    private <T, V extends SortOrder<T>> void assertSortOrdersEquals(List<V> o1,
            List<V> o2) {
        Assertions.assertEquals(o1.size(), o2.size());
        for (int i = 0; i < o1.size(); ++i) {
            Assertions.assertEquals(o1.get(i).getDirection(),
                    o2.get(i).getDirection());
            Assertions.assertEquals(o1.get(i).getSorted(),
                    o2.get(i).getSorted());
        }
    }

    private static List<Person> createItems() {
        Random random = new Random(0);
        return IntStream.range(1, 500)
                .mapToObj(index -> createPerson(index, random))
                .collect(Collectors.toList());
    }

    private static Person createPerson(int index, Random random) {
        Person person = new Person();
        person.setId(index);
        person.setName("Person " + index);
        person.setAge(13 + random.nextInt(50));

        Address address = new Address();
        address.setStreet("Street " + ((char) ('A' + random.nextInt(26))));
        address.setNumber(1 + random.nextInt(50));
        address.setPostalCode(String.valueOf(10000 + random.nextInt(8999)));
        person.setAddress(address);

        return person;
    }

    private void assertInMemorySorting(Comparator<Person> comparator) {
        List<Person> expectedOrder = createItems();
        List<Person> actualOrder = new ArrayList<>(expectedOrder);

        expectedOrder.sort(comparator);
        actualOrder.sort(grid.getDataCommunicator().getInMemorySorting());

        Assertions.assertEquals(expectedOrder, actualOrder);
    }
}
