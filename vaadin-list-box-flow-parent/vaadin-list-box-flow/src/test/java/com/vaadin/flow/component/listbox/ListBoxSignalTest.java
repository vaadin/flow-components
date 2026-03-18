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
package com.vaadin.flow.component.listbox;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ListBoxSignalTest extends AbstractSignalsUnitTest {
    @Test(expected = UnsupportedOperationException.class)
    public void listBox_bindRequiredIndicatorVisible_throwsException() {
        var listBox = new ListBox<>();
        listBox.bindRequiredIndicatorVisible(new ValueSignal<>(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void multiSelectListBox_bindRequiredIndicatorVisible_throwsException() {
        var listBox = new MultiSelectListBox<>();
        listBox.bindRequiredIndicatorVisible(new ValueSignal<>(false));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setDataProvider(
                DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var listBox = createListBoxWithBoundItems();
        listBox.setItems("New Item 1", "New Item 2");
    }

    @Test
    public void bindItems_updateItemSignalValue_updatesItem() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var listBox = new ListBox<String>();
        listBox.bindItems(listSignal);
        ui.add(listBox);

        List<String> labels = getItemLabels(listBox);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("original", labels.get(0));

        // Update the item signal value (identity change)
        listSignal.peek().getFirst().set("updated");

        // Verify the item component was replaced with the updated label
        labels = getItemLabels(listBox);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("updated", labels.get(0));
    }

    @Test
    public void multiSelectListBox_bindItems_updateItemSignalValue_updatesItem() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var listBox = new MultiSelectListBox<String>();
        listBox.bindItems(listSignal);
        ui.add(listBox);

        List<String> labels = getItemLabels(listBox);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("original", labels.get(0));

        // Update the item signal value (identity change)
        listSignal.peek().getFirst().set("updated");

        // Verify the item component was replaced with the updated label
        labels = getItemLabels(listBox);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("updated", labels.get(0));
    }

    @SuppressWarnings("unchecked")
    private List<String> getItemLabels(ListBoxBase<?, String, ?> listBox) {
        return listBox.getChildren().filter(VaadinItem.class::isInstance)
                .map(c -> ((VaadinItem<String>) c).getItem())
                .collect(Collectors.toList());
    }

    private ListBox<String> createListBoxWithBoundItems() {
        var listBox = new ListBox<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        listBox.bindItems(itemsSignal);
        ui.add(listBox);
        return listBox;
    }
}
