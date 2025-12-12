/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.tests.dataprovider.MockUI;

public class GridLazyFilterDataViewTest {

    private Grid<Person> grid;
    private MockUI ui;
    private List<Person> allItems;

    /**
     * Simple filter class for testing typed filters
     */
    static class PersonFilter {
        private final String name;
        private final Integer minAge;

        public PersonFilter(String name, Integer minAge) {
            this.name = name;
            this.minAge = minAge;
        }

        public String getName() {
            return name;
        }

        public Integer getMinAge() {
            return minAge;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PersonFilter that = (PersonFilter) o;
            return Objects.equals(name, that.name)
                    && Objects.equals(minAge, that.minAge);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, minAge);
        }
    }

    /**
     * Simple person class for testing
     */
    static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return name + " (" + age + ")";
        }
    }

    @Before
    public void setup() {
        grid = new Grid<>();
        ui = new MockUI();
        ui.add(grid);

        // Create test data
        allItems = Arrays.asList(new Person("Alice", 25), new Person("Bob", 30),
                new Person("Charlie", 35), new Person("David", 40),
                new Person("Eve", 45));
    }

    @Test
    public void setItemsWithFilter_withFetchCallback_canSetFilter() {
        AtomicReference<PersonFilter> receivedFilter = new AtomicReference<>();

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    receivedFilter.set(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                });

        // Initially, no filter
        fakeClientCommunication();
        Assert.assertNull("Filter should initially be null",
                receivedFilter.get());

        // Set a filter
        PersonFilter filter = new PersonFilter("Alice", 20);
        dataView.setFilter(filter);

        fakeClientCommunication();
        Assert.assertEquals("Filter should be passed to callback", filter,
                receivedFilter.get());
    }

    @Test
    public void setItemsWithFilter_withFetchAndCountCallbacks_filterPassedToBoth() {
        AtomicReference<PersonFilter> fetchFilter = new AtomicReference<>();
        AtomicReference<PersonFilter> countFilter = new AtomicReference<>();

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    fetchFilter.set(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                }, query -> {
                    countFilter.set(query.getFilter().orElse(null));
                    return filterAndCount(query);
                });

        PersonFilter filter = new PersonFilter("Bob", 25);
        dataView.setFilter(filter);

        fakeClientCommunication();

        Assert.assertEquals("Filter should be passed to fetch callback", filter,
                fetchFilter.get());
        Assert.assertEquals("Filter should be passed to count callback", filter,
                countFilter.get());
    }

    @Test
    public void setItemsWithFilter_withBackEndDataProvider_canSetFilter() {
        AtomicReference<PersonFilter> receivedFilter = new AtomicReference<>();

        BackEndDataProvider<Person, PersonFilter> dataProvider = DataProvider
                .fromFilteringCallbacks(query -> {
                    receivedFilter.set(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                }, query -> filterAndCount(query));

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(dataProvider);

        PersonFilter filter = new PersonFilter("Charlie", 30);
        dataView.setFilter(filter);

        fakeClientCommunication();

        Assert.assertEquals("Filter should be passed to data provider", filter,
                receivedFilter.get());
    }

    @Test
    public void setFilter_null_clearsFilter() {
        AtomicReference<PersonFilter> receivedFilter = new AtomicReference<>();

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    receivedFilter.set(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                });

        // Set a filter
        PersonFilter filter = new PersonFilter("Alice", 20);
        dataView.setFilter(filter);
        fakeClientCommunication();
        Assert.assertEquals(filter, receivedFilter.get());

        // Clear the filter
        dataView.setFilter(null);
        fakeClientCommunication();
        Assert.assertNull("Filter should be null after clearing",
                receivedFilter.get());
    }

    @Test
    public void setFilter_multipleUpdates_lastFilterApplied() {
        List<PersonFilter> receivedFilters = new ArrayList<>();

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    receivedFilters.add(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                });

        // Set first filter
        PersonFilter filter1 = new PersonFilter("Alice", 20);
        dataView.setFilter(filter1);
        fakeClientCommunication();

        // Set second filter
        PersonFilter filter2 = new PersonFilter("Bob", 30);
        dataView.setFilter(filter2);
        fakeClientCommunication();

        // The last filter should be the one applied
        Assert.assertTrue("Should have received multiple filters",
                receivedFilters.size() >= 2);
        Assert.assertEquals("Last filter should be filter2", filter2,
                receivedFilters.get(receivedFilters.size() - 1));
    }

    @Test
    public void setItemCountCallbackWithFilter_withTypedFilter_filterPassedToCallback() {
        AtomicReference<PersonFilter> countFilter = new AtomicReference<>();
        AtomicInteger itemCount = new AtomicInteger(-1);

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> filterAndFetch(query));

        // Set a typed count callback
        dataView.setItemCountCallbackWithFilter(query -> {
            countFilter.set(query.getFilter().orElse(null));
            return filterAndCount(query);
        });

        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));

        // Set a filter
        PersonFilter filter = new PersonFilter("Alice", 20);
        dataView.setFilter(filter);

        grid.getDataCommunicator().setViewportRange(0, 50);
        fakeClientCommunication();

        Assert.assertEquals("Filter should be passed to count callback", filter,
                countFilter.get());
        Assert.assertEquals("Item count should match filtered results", 1,
                itemCount.get());
    }

    @Test
    public void getFilter_returnsCurrentFilter() {
        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> filterAndFetch(query));

        Assert.assertNull("Initial filter should be null",
                dataView.getFilter());

        PersonFilter filter = new PersonFilter("Alice", 20);
        dataView.setFilter(filter);

        Assert.assertEquals("getFilter should return the set filter", filter,
                dataView.getFilter());

        dataView.setFilter(null);
        Assert.assertNull("getFilter should return null after clearing",
                dataView.getFilter());
    }

    @Test
    public void setItemsWithFilter_automaticRefreshOnFilterChange() {
        AtomicInteger fetchCount = new AtomicInteger(0);

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    fetchCount.incrementAndGet();
                    return filterAndFetch(query);
                });

        grid.getDataCommunicator().setViewportRange(0, 50);
        fakeClientCommunication();
        int initialFetchCount = fetchCount.get();

        // Setting a filter should trigger a refresh
        dataView.setFilter(new PersonFilter("Alice", 20));
        fakeClientCommunication();

        Assert.assertTrue("Setting filter should trigger data refresh",
                fetchCount.get() > initialFetchCount);
    }

    @Test
    public void backwardCompatibility_untypedSetItemsStillWorks() {
        // Test that existing untyped setItems still works
        CallbackDataProvider<String, Void> untypedProvider = DataProvider
                .fromCallbacks(query -> Stream.of("foo", "bar", "baz"),
                        query -> 3);

        Grid<String> stringGrid = new Grid<>();
        ui.add(stringGrid);

        GridLazyDataView<String> untypedDataView = stringGrid
                .setItems(untypedProvider);

        Assert.assertNotNull("Untyped data view should be created",
                untypedDataView);
        Assert.assertTrue("DataCommunicator should be defined size",
                stringGrid.getDataCommunicator().isDefinedSize());
    }

    @Test
    public void filterWithStringType_compileTimeTypeSafety() {
        // This test verifies compile-time type safety
        GridLazyFilterDataView<Person, String> dataView = grid
                .setItemsWithFilter(query -> {
                    String filter = query.getFilter().orElse(null);
                    return allItems.stream().filter(p -> filter == null
                            || p.getName().contains(filter));
                });

        dataView.setFilter("Alice");
        // This should compile - verifying type safety
        String currentFilter = dataView.getFilter();
        Assert.assertEquals("Alice", currentFilter);
    }

    @Test
    public void filterWithComplexObject_worksCorrectly() {
        AtomicReference<PersonFilter> receivedFilter = new AtomicReference<>();

        GridLazyFilterDataView<Person, PersonFilter> dataView = grid
                .setItemsWithFilter(query -> {
                    receivedFilter.set(query.getFilter().orElse(null));
                    return filterAndFetch(query);
                });

        PersonFilter complexFilter = new PersonFilter("Alice", 25);
        dataView.setFilter(complexFilter);
        fakeClientCommunication();

        PersonFilter received = receivedFilter.get();
        Assert.assertNotNull("Should receive filter", received);
        Assert.assertEquals("Name should match", "Alice", received.getName());
        Assert.assertEquals("Age should match", Integer.valueOf(25),
                received.getMinAge());
    }

    // Helper methods

    private Stream<Person> filterAndFetch(Query<Person, PersonFilter> query) {
        PersonFilter filter = query.getFilter().orElse(null);
        Stream<Person> stream = allItems.stream();

        if (filter != null) {
            if (filter.getName() != null) {
                stream = stream
                        .filter(p -> p.getName().equals(filter.getName()));
            }
            if (filter.getMinAge() != null) {
                stream = stream.filter(p -> p.getAge() >= filter.getMinAge());
            }
        }

        return stream.skip(query.getOffset()).limit(query.getLimit());
    }

    private int filterAndCount(Query<Person, PersonFilter> query) {
        PersonFilter filter = query.getFilter().orElse(null);
        Stream<Person> stream = allItems.stream();

        if (filter != null) {
            if (filter.getName() != null) {
                stream = stream
                        .filter(p -> p.getName().equals(filter.getName()));
            }
            if (filter.getMinAge() != null) {
                stream = stream.filter(p -> p.getAge() >= filter.getMinAge());
            }
        }

        return (int) stream.count();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
