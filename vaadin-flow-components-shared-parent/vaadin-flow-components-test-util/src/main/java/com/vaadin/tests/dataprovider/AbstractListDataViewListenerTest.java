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
package com.vaadin.tests.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.function.SerializablePredicate;

// Originally from com.vaadin.flow.data.provider.AbstractListDataViewListenerTest
// If this breaks, check there for updates

public abstract class AbstractListDataViewListenerTest {

    @Test
    public void addItemCountChangeListener_itemsCountChanged_listenersAreNotified() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component
                .setItems(new ArrayList<>(Arrays.asList(items)));

        AtomicInteger invocationCounter = new AtomicInteger(0);

        dataView.addItemCountChangeListener(
                event -> invocationCounter.incrementAndGet());

        UI ui = new MockUI();
        ui.add((Component) component);

        dataView.setFilter("one"::equals);
        dataView.setFilter(null);
        dataView.addItemAfter("item5", "item4");
        dataView.addItemBefore("item0", "item1");
        dataView.addItem("last");
        dataView.removeItem("item0");

        fakeClientCall(ui);

        Assert.assertEquals(
                "Unexpected number of item count change listener invocations "
                        + "occurred",
                1, invocationCounter.get());
    }

    @Test
    public void addItemCountChangeListener_itemsCountNotChanged_listenersAreNotNotified() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component.setItems(items);

        AtomicBoolean invocationChecker = new AtomicBoolean(false);

        UI ui = new MockUI();
        ui.add((Component) component);

        // Make initial item count change
        fakeClientCall(ui);

        dataView.addItemCountChangeListener(
                event -> invocationChecker.getAndSet(true));

        dataView.setSortComparator(String::compareTo);

        // Make item count change after sort. No event should be sent as item
        // count stays the same.
        fakeClientCall(ui);

        Assert.assertFalse("Unexpected item count listener invocation",
                invocationChecker.get());
    }

    @Test
    public void addItemCountChangeListener_itemsCountChanged_newItemCountSuppliedInEvent() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();
        AbstractListDataView<String> dataView = component.setItems(items);

        AtomicBoolean invocationChecker = new AtomicBoolean(false);

        UI ui = new MockUI();
        ui.add((Component) component);

        // Make initial item count event
        fakeClientCall(ui);

        dataView.addItemCountChangeListener(event -> {
            Assert.assertEquals("Unexpected item count", 1,
                    event.getItemCount());
            Assert.assertFalse(event.isItemCountEstimated());
            invocationChecker.set(true);
        });

        dataView.setFilter("item1"::equals);

        // Item count change should be sent as item count has changed after
        // filtering.
        fakeClientCall(ui);

        Assert.assertTrue("Item count change never called",
                invocationChecker.get());
    }

    @Test
    public void setItems_setNewItemsToComponent_filteringAndSortingRemoved() {
        HasListDataView<String, ? extends AbstractListDataView<String>> component = getVerifiedComponent();

        AbstractListDataView<String> listDataView = component.setItems("item1",
                "item2", "item3");

        SerializablePredicate<String> filter = "item2"::equals;

        listDataView.setFilter(filter);

        Assert.assertEquals("Unexpected filtered item count", 1,
                listDataView.getItemCount());

        listDataView = component.setItems("item1", "item2", "item3");

        Assert.assertEquals("Non-filtered item count expected", 3,
                listDataView.getItemCount());
    }

    protected abstract HasListDataView<String, ? extends AbstractListDataView<String>> getComponent();

    private void fakeClientCall(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

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
