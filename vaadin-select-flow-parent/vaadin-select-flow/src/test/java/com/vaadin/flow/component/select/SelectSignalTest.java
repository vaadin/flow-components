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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
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
    public void bindItems_updateItemSignalValue_updatesItem() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var select = new Select<String>();
        select.bindItems(listSignal);
        ui.add(select);

        List<String> labels = getItemLabels(select);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("original", labels.get(0));

        // Update the item signal value (identity change)
        listSignal.peek().getFirst().set("updated");

        // Verify the item element was updated with the new label
        labels = getItemLabels(select);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("updated", labels.get(0));
    }

    private List<String> getItemLabels(Select<String> select) {
        // Items are inside the listBox element (first child of select)
        var listBoxElement = select.getElement().getChild(0);
        var labels = new ArrayList<String>();
        for (int i = 0; i < listBoxElement.getChildCount(); i++) {
            String text = listBoxElement.getChild(i).getText();
            if (text != null && !text.isEmpty()) {
                labels.add(text);
            }
        }
        return labels;
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
