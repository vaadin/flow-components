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
package com.vaadin.flow.component.checkbox;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class CheckboxGroupSignalTest extends AbstractSignalsUnitTest {

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup.setDataProvider(
                DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup
                .setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var checkboxGroup = createCheckboxGroupWithBoundItems();
        checkboxGroup.setItems("New Item 1", "New Item 2");
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        CheckboxGroup<String> group = new CheckboxGroup<>("Options",
                listSignal);
        ui.add(group);

        List<String> items = group.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("One", items.get(0));
        Assert.assertEquals("Two", items.get(1));
        Assert.assertEquals("Options", group.getLabel());

        listSignal.insertLast("Three");

        items = group.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Three", items.get(2));
    }

    @Test
    public void bindItems_updateItemSignalValue_updatesCheckbox() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var group = new CheckboxGroup<String>();
        group.bindItems(listSignal);
        ui.add(group);

        Assert.assertEquals(1, getCheckboxLabels(group).size());
        Assert.assertEquals("original", getCheckboxLabels(group).get(0));

        // Update the item signal value (identity change)
        listSignal.peek().getFirst().set("updated");

        // Verify the checkbox child was replaced with the updated label
        List<String> labels = getCheckboxLabels(group);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("updated", labels.get(0));
    }

    @Test
    public void bindItems_selectItem_updateIdentity_selectionPreserved() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("a");
        listSignal.insertLast("b");

        var group = new CheckboxGroup<String>();
        group.bindItems(listSignal);
        ui.add(group);

        // Select the first item
        group.setValue(Set.of("a"));
        Assert.assertEquals(Set.of("a"), group.getValue());

        // Change the identity of the selected item
        listSignal.peek().getFirst().set("a-updated");

        // Verify selection is preserved with the new item
        Assert.assertEquals(Set.of("a-updated"), group.getValue());
        Assert.assertEquals(List.of("a-updated", "b"),
                getCheckboxLabels(group));
    }

    @SuppressWarnings("unchecked")
    private List<String> getCheckboxLabels(CheckboxGroup<String> group) {
        return group.getChildren().filter(Checkbox.class::isInstance)
                .map(c -> ((Checkbox) c).getLabel()).toList();
    }

    private CheckboxGroup<String> createCheckboxGroupWithBoundItems() {
        var checkboxGroup = new CheckboxGroup<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        checkboxGroup.bindItems(itemsSignal);
        ui.add(checkboxGroup);
        return checkboxGroup;
    }
}
