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
package com.vaadin.flow.component.radiobutton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class RadioButtonGroupSignalTest extends AbstractSignalsUnitTest {
    private final RadioButtonGroup<String> group = new RadioButtonGroup<>();
    private final ValueSignal<Boolean> readonlySignal = new ValueSignal<>(
            false);

    @Test
    public void bindReadOnly_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);

        Assert.assertFalse(group.isReadOnly());

        readonlySignal.set(true);
        Assert.assertTrue(group.isReadOnly());
    }

    @Test
    public void bindReadOnly_elementNotAttached_initialValueApplied() {
        readonlySignal.set(true);
        group.bindReadOnly(readonlySignal);

        // Initial value is applied immediately (effect runs on creation)
        Assert.assertTrue(group.isReadOnly());

        UI.getCurrent().add(group);
        Assert.assertTrue(group.isReadOnly());
    }

    @Test(expected = BindingActiveException.class)
    public void setReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        group.setReadOnly(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        group.bindReadOnly(new ValueSignal<>(true));
    }

    @Test
    public void bindReadOnly_disablesUncheckedButtons() {
        group.setItems("One", "Two", "Three");
        group.setValue("One");
        group.bindReadOnly(readonlySignal);
        UI.getCurrent().add(group);

        readonlySignal.set(true);

        List<RadioButton<String>> buttons = getRadioButtons();
        Assert.assertTrue("Selected button should remain enabled",
                buttons.get(0).isEnabled());
        Assert.assertFalse("Unchecked button should be disabled",
                buttons.get(1).isEnabled());
        Assert.assertFalse("Unchecked button should be disabled",
                buttons.get(2).isEnabled());
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProvider_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup.setDataProvider(
                DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProvider_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup
                .setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProvider_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup.setItems(DataProvider
                .ofCollection(Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithVarargs_throws() {
        var radioButtonGroup = createRadioButtonGroupWithBoundItems();
        radioButtonGroup.setItems("New Item 1", "New Item 2");
    }

    @SuppressWarnings("unchecked")
    private List<RadioButton<String>> getRadioButtons() {
        return group.getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());
    }

    private RadioButtonGroup<String> createRadioButtonGroupWithBoundItems() {
        var radioButtonGroup = new RadioButtonGroup<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        radioButtonGroup.bindItems(itemsSignal);
        ui.add(radioButtonGroup);
        return radioButtonGroup;
    }

    @Test
    public void bindItems_updateItemSignalValue_updatesRadioButton() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("original");

        var radioGroup = new RadioButtonGroup<String>();
        radioGroup.bindItems(listSignal);
        ui.add(radioGroup);

        List<String> labels = getRadioButtonItems(radioGroup);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("original", labels.get(0));

        // Update the item signal value (identity change)
        listSignal.peek().getFirst().set("updated");

        // Verify the radio button child was replaced with the updated item
        labels = getRadioButtonItems(radioGroup);
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("updated", labels.get(0));
    }

    @Test
    public void bindItems_selectItem_updateIdentity_selectionPreserved() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("a");
        listSignal.insertLast("b");

        var radioGroup = new RadioButtonGroup<String>();
        radioGroup.bindItems(listSignal);
        ui.add(radioGroup);

        // Select the first item
        radioGroup.setValue("a");
        Assert.assertEquals("a", radioGroup.getValue());

        // Change the identity of the selected item
        listSignal.peek().getFirst().set("a-updated");

        // Verify selection is preserved with the new item
        Assert.assertEquals("a-updated", radioGroup.getValue());
        Assert.assertEquals(List.of("a-updated", "b"),
                getRadioButtonItems(radioGroup));
    }

    @SuppressWarnings("unchecked")
    private List<String> getRadioButtonItems(RadioButtonGroup<String> group) {
        return group.getChildren().filter(RadioButton.class::isInstance)
                .map(c -> ((RadioButton<String>) c).getItem()).toList();
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        RadioButtonGroup<String> group = new RadioButtonGroup<>("Options",
                listSignal);
        UI.getCurrent().add(group);

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
}
