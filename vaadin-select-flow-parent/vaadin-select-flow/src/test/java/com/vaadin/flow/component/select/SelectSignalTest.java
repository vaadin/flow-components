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
package com.vaadin.flow.component.select;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class SelectSignalTest extends AbstractSignalsUnitTest {

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var select = createSelectWithBoundItems();
        select.setDataProvider(
                DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var select = createSelectWithBoundItems();
        select.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var select = createSelectWithBoundItems();
        select.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var select = createSelectWithBoundItems();
        select.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var select = createSelectWithBoundItems();
        select.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var select = createSelectWithBoundItems();
        select.setItems("New Item 1", "New Item 2");
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        Select<String> select = new Select<>("Options", listSignal);
        ui.add(select);

        List<String> items = select.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("One", items.get(0));
        Assert.assertEquals("Two", items.get(1));
        Assert.assertEquals("Options", select.getLabel());

        listSignal.insertLast("Three");

        items = select.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Three", items.get(2));
    }

    @Test
    public void bindItems_updateItemSignalValue_keyMapperRemapsToSameKey()
            throws Exception {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var select = new Select<String>();
        select.bindItems(listSignal);
        ui.add(select);

        KeyMapper<String> keyMapper = getKeyMapper(select);
        String keyBefore = keyMapper.key("original");
        Assert.assertTrue(keyMapper.has("original"));

        // Update the item signal value
        listSignal.peek().getFirst().set("updated");

        // The old identity should be gone, replaced by the new one
        // mapped to the same key
        Assert.assertFalse(keyMapper.has("original"));
        Assert.assertTrue(keyMapper.has("updated"));
        Assert.assertEquals(keyBefore, keyMapper.key("updated"));

        // Verify the component items reflect the update
        List<String> items = select.getGenericDataView().getItems().toList();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals("updated", items.get(0));
    }

    @SuppressWarnings("unchecked")
    private KeyMapper<String> getKeyMapper(Select<String> select)
            throws Exception {
        Field field = Select.class.getDeclaredField("keyMapper");
        field.setAccessible(true);
        return (KeyMapper<String>) field.get(select);
    }

    private Select<String> createSelectWithBoundItems() {
        var select = new Select<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        select.bindItems(itemsSignal);
        ui.add(select);
        return select;
    }
}
