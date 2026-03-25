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
package com.vaadin.tests.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.tests.MockUIExtension;

// Originally from com.vaadin.flow.data.provider.AbstractListDataViewListenerTest
// If this breaks, check there for updates

public abstract class AbstractListDataViewListenerTest {
    @RegisterExtension
    protected MockUIExtension ui = new MockUIExtension();

    @Test
    void addItemCountChangeListener_itemsCountChanged_listenersAreNotified() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component
                .setItems(new ArrayList<>(Arrays.asList(items)));

        AtomicInteger invocationCounter = new AtomicInteger(0);

        dataView.addItemCountChangeListener(
                event -> invocationCounter.incrementAndGet());

        ui.add((Component) component);

        dataView.setFilter("one"::equals);
        dataView.setFilter(null);
        dataView.addItemAfter("item5", "item4");
        dataView.addItemBefore("item0", "item1");
        dataView.addItem("last");
        dataView.removeItem("item0");

        ui.fakeClientCommunication();

        Assertions.assertEquals(1, invocationCounter.get(),
                "Unexpected number of item count change listener invocations "
                        + "occurred");
    }

    @Test
    void addItemCountChangeListener_itemsCountNotChanged_listenersAreNotNotified() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component.setItems(items);

        AtomicBoolean invocationChecker = new AtomicBoolean(false);

        ui.add((Component) component);

        // Make initial item count change
        ui.fakeClientCommunication();

        dataView.addItemCountChangeListener(
                event -> invocationChecker.getAndSet(true));

        dataView.setSortComparator(String::compareTo);

        // Make item count change after sort. No event should be sent as item
        // count stays the same.
        ui.fakeClientCommunication();

        Assertions.assertFalse(invocationChecker.get(),
                "Unexpected item count listener invocation");
    }

    @Test
    void addItemCountChangeListener_itemsCountChanged_newItemCountSuppliedInEvent() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component.setItems(items);

        AtomicBoolean invocationChecker = new AtomicBoolean(false);

        ui.add((Component) component);

        // Make initial item count event
        ui.fakeClientCommunication();

        dataView.addItemCountChangeListener(event -> {
            Assertions.assertEquals(1, event.getItemCount(),
                    "Unexpected item count");
            Assertions.assertFalse(event.isItemCountEstimated());
            invocationChecker.set(true);
        });

        dataView.setFilter("item1"::equals);

        // Item count change should be sent as item count has changed after
        // filtering.
        ui.fakeClientCommunication();

        Assertions.assertTrue(invocationChecker.get(),
                "Item count change never called");
    }

    @Test
    void setItems_setNewItemsToComponent_filteringAndSortingRemoved() {
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();

        AbstractListDataView<String> listDataView = component.setItems("item1",
                "item2", "item3");

        SerializablePredicate<String> filter = "item2"::equals;

        listDataView.setFilter(filter);

        Assertions.assertEquals(1, listDataView.getItemCount(),
                "Unexpected filtered item count");

        listDataView = component.setItems("item1", "item2", "item3");

        Assertions.assertEquals(3, listDataView.getItemCount(),
                "Non-filtered item count expected");
    }

    protected abstract HasListDataView<String, ? extends AbstractListDataView<String>> getComponent();

    private HasListDataView<String, ? extends AbstractListDataView<String>> getVerifiedComponent() {
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getComponent();
        if (component instanceof Component) {
            return component;
        }
        throw new IllegalArgumentException(String.format(
                "Component subclass is expected, but was given a '%s'",
                component.getClass().getSimpleName()));
    }
}
