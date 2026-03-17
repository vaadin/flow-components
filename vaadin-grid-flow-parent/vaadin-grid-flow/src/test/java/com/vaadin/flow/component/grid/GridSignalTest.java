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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class GridSignalTest extends AbstractSignalsUnitTest {

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setDataProvider(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var grid = createGridWithBoundItems();
        grid.setItems("New Item 1", "New Item 2");
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        Grid<String> grid = new Grid<>(listSignal);
        ui.add(grid);

        List<String> items = grid.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("One", items.get(0));
        Assert.assertEquals("Two", items.get(1));

        listSignal.insertLast("Three");

        items = grid.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Three", items.get(2));
    }

    @Test
    public void bindItems_updateItemSignalValue_keyMapperRemapsToSameKey() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var grid = new Grid<String>();
        grid.bindItems(listSignal);
        ui.add(grid);

        // Force a flush so the key mapper has assigned a key
        ui.fakeClientCommunication();

        var keyMapper = grid.getDataCommunicator().getKeyMapper();
        String keyBefore = keyMapper.key("original");
        Assert.assertTrue(keyMapper.has("original"));

        // Update the item signal value
        listSignal.peek().getFirst().set("updated");

        // The old identity should be gone, replaced by the new one
        // mapped to the same key
        Assert.assertFalse(keyMapper.has("original"));
        Assert.assertTrue(keyMapper.has("updated"));
        Assert.assertEquals(keyBefore, keyMapper.key("updated"));
    }

    private Grid<String> createGridWithBoundItems() {
        var grid = new Grid<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        grid.bindItems(itemsSignal);
        ui.add(grid);
        return grid;
    }
}
