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
