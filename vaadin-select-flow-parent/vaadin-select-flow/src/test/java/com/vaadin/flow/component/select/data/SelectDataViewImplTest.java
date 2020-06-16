/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.select.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SizeChangeEvent;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;


public class SelectDataViewImplTest {
        private List<String> items;

        private InMemoryDataProvider<String> dataProvider;

        private SelectDataView<String> dataView;

        private Select<String> component;

        @Before
        public void init() {

            items = new ArrayList<>(Arrays.asList("first", "middle", "last"));

            dataProvider = new InMemoryProvider(items);
            component = new Select<>();
            dataView = component.setDataSource(dataProvider);
        }

        @Test
        public void getAllItems_noFiltersSet_allItemsObtained() {
            Stream<String> allItems = dataView.getItems();
            Assert.assertArrayEquals("Unexpected data set", items.toArray(),
                    allItems.toArray());
        }

        @Test
        public void getDataSize_noFiltersSet_dataSizeObtained() {
            Assert.assertEquals("Unexpected size for data", items.size(),
                    dataView.getSize());
        }

        @Test
        public void addListener_fireEvent_listenerIsCalled() {
            AtomicInteger fired = new AtomicInteger(0);
            dataView.addSizeChangeListener(
                    event -> fired.compareAndSet(0, event.getSize()));

            ComponentUtil
                    .fireEvent(component, new SizeChangeEvent<>(component, 10));

            Assert.assertEquals(10, fired.get());
        }

        @Test
        public void dataViewWithItems_contains_returnsCorrectItems() {
            Assert.assertTrue("Returned false for item that should exist",
                    dataView.contains("middle"));
            Assert.assertFalse("Returned false for item that should exist",
                    dataView.contains("non existing"));
        }

        @Test
        public void dataViewWithItems_getItemOnRow_returnsCorrectItem() {
            Assert.assertEquals(items.get(0), dataView.getItemOnIndex(0));
            Assert.assertEquals(items.get(1), dataView.getItemOnIndex(1));
            Assert.assertEquals(items.get(2), dataView.getItemOnIndex(2));
        }

        private static class InMemoryProvider
                implements InMemoryDataProvider<String> {

            private List<String> items;
            private SerializablePredicate<String> filter = in -> true;
            private SerializableComparator<String> comparator;

            public InMemoryProvider(List<String> items) {
                this.items = items;
            }

            @Override
            public SerializablePredicate<String> getFilter() {
                return filter;
            }

            @Override
            public void setFilter(SerializablePredicate<String> filter) {
                this.filter = filter;
            }

            @Override
            public SerializableComparator<String> getSortComparator() {
                return comparator;
            }

            @Override
            public void setSortComparator(
                    SerializableComparator<String> comparator) {
                this.comparator = comparator;
            }

            @Override
            public int size(Query<String, SerializablePredicate<String>> query) {
                return (int) items.stream().filter(filter::test).count();
            }

            @Override
            public Stream<String> fetch(
                    Query<String, SerializablePredicate<String>> query) {
                return items.stream().filter(filter::test);
            }

            @Override
            public void refreshItem(String item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(
                    DataProviderListener<String> listener) {
                return () -> {
                };
            }
        }


}