/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.flow.component.listbox.test.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxDataView;
import com.vaadin.flow.data.provider.AbstractComponentDataViewTest;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

public class ListBoxDataViewTest extends AbstractComponentDataViewTest {

    private static final String OUTER_HTML = "<vaadin-item>\n <span>%s</span>\n</vaadin-item>";

    @Test
    public void getItem_dataViewWithItems_returnsCorrectItem() {
        Assert.assertEquals(items.get(0), dataView.getItem(0));
        Assert.assertEquals(items.get(1), dataView.getItem(1));
        Assert.assertEquals(items.get(2), dataView.getItem(2));
    }

    @Test
    public void setIdentifierProvider_customIdentity_itemRefreshed() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        GenericDataProvider dataProvider = new GenericDataProvider(items);
        ListBox<Item> component = new ListBox<>();

        ListBoxDataView<Item> dataView = component.setItems(dataProvider);

        dataView.setIdentifierProvider(Item::getId);

        first.setValue("changed-1");
        second.setValue("changed-2");

        dataProvider.refreshItem(new Item(1L));

        Assert.assertTrue(containsLabel(component, "changed-1"));
        Assert.assertFalse(containsLabel(component, "changed-2"));
    }

    @Test
    public void setIdentifierProvider_customDataProviderIdentity_itemRefreshed() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        CustomIdentityItemDataProvider customIdentityItemDataProvider = new CustomIdentityItemDataProvider(
                items);

        ListBox<Item> component = new ListBox<>();
        component.setItems(customIdentityItemDataProvider);

        first.setValue("changed-1");
        second.setValue("changed-2");

        customIdentityItemDataProvider.refreshItem(new Item(1L));

        Assert.assertTrue(containsLabel(component, "changed-1"));
        Assert.assertFalse(containsLabel(component, "changed-2"));
    }

    @Test
    public void setIdentifierProvider_defaultIdentity_itemRefreshed() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        GenericDataProvider dataProvider = new GenericDataProvider(items);
        ListBox<Item> component = new ListBox<>();
        component.setItems(dataProvider);

        first.setValue("changed-1");
        second.setValue("changed-2");

        dataProvider.refreshItem(new Item(1L, "changed-1"));

        Assert.assertTrue(containsLabel(component, "changed-1"));
        Assert.assertFalse(containsLabel(component, "changed-2"));
    }

    @Test
    public void setInMemoryDataProvider_convertsToGenericDataProvider() {
        ListBox<String> checkboxGroup = Mockito.spy(new ListBox<>());

        InMemoryDataProvider<String> inMemoryDataProvider = new InMemoryDataProvider<String>() {

            private SerializablePredicate<String> filter;

            @Override
            public int size(
                    Query<String, SerializablePredicate<String>> query) {
                Assert.assertTrue(query.getFilter().isPresent());
                return (int) Stream.of("foo").filter(query.getFilter().get())
                        .count();
            }

            @Override
            public Stream<String> fetch(
                    Query<String, SerializablePredicate<String>> query) {
                Assert.assertTrue(query.getFilter().isPresent());
                return Stream.of("foo").filter(query.getFilter().get());
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
                return null;
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
                return null;
            }

            @Override
            public void setSortComparator(
                    SerializableComparator<String> comparator) {

            }
        };

        ListBoxDataView<String> dataView = checkboxGroup
                .setItems(inMemoryDataProvider);

        // We expect that the current implementation of 'setItems' with IMDP
        // will delegate to 'setItems(DataProvider)'
        Mockito.verify(checkboxGroup).setItems(Mockito.any(DataProvider.class));

        // Verify the predicate filter always returns true and passes the item
        Assert.assertEquals("foo", dataView.getItem(0));

        // Now set the predicate and verify it goes to query parameter
        inMemoryDataProvider.setFilter(item -> item.equals("bar"));
        Assert.assertEquals(0, dataView.getItems().count());
    }

    @Override
    protected HasDataView<String, Void, ? extends DataView<String>> getComponent() {
        return new ListBox<>();
    }

    private boolean containsLabel(ListBox<Item> component, String label) {
        String elementLabel = String.format(OUTER_HTML, label);
        return component.getChildren().map(Component::getElement)
                .anyMatch(elem -> elem.getOuterHTML().equals(elementLabel));
    }

    private static class GenericDataProvider
            extends AbstractDataProvider<Item, Void> {
        private List<Item> items;

        public GenericDataProvider(List<Item> items) {
            this.items = items;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Item, Void> query) {
            return 2;
        }

        @Override
        public Stream<Item> fetch(Query<Item, Void> query) {
            return Stream.of(items.toArray(new Item[0]));
        }
    }

    private static class CustomIdentityItemDataProvider
            extends ListDataProvider<Item> {

        public CustomIdentityItemDataProvider(Collection<Item> items) {
            super(items);
        }

        @Override
        public Object getId(Item item) {
            return item.getId();
        }
    }
}