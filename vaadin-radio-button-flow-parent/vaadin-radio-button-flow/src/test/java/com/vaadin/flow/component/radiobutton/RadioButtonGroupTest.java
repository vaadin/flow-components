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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;
import com.vaadin.tests.dataprovider.DataProviderListenersTest;

class RadioButtonGroupTest {
    private static final String OUTER_HTML = "<vaadin-radio-button><label slot=\"label\"><span>%s</span></label></vaadin-radio-button>";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setReadOnlyRadioGroup_groupIsReadOnly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assertions.assertTrue(group.isReadOnly());

        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assertions.assertEquals(group.getChildren().count(),
                disabledChildCount);
    }

    @Test
    void setReadOnlyRadioGroup_checkedButtonIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setValue("foo");
        group.setReadOnly(true);

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assertions.assertEquals(1, disabledChildCount);
    }

    @Test
    void setReadOnlyRadioGroup_checkedButtonIsEnabled2() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        group.setValue("foo");

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assertions.assertEquals(1, disabledChildCount);
    }

    @Test
    void setReadOnlyDisabledRadioGroup_groupIsDisabledAndReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assertions.assertTrue(group.isReadOnly());
        Assertions.assertFalse(group.isEnabled());
        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    void unsetReadOnlyDisabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assertions.assertFalse(group.isReadOnly());
        Assertions.assertFalse(group.isEnabled());
        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    void setReadOnlyEnabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setReadOnly(true);
        group.setEnabled(true);

        Assertions.assertTrue(group.isReadOnly());
        Assertions.assertTrue(group.isEnabled());
        Assertions.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        group.setReadOnly(false);

        Assertions.assertTrue(group.isEnabled());
        Assertions.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assertions.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("readonly"));
    }

    @Test
    void unsetReadOnlyEnabledRadioGroup_groupIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);
        group.setEnabled(true);

        group.setReadOnly(false);

        Assertions.assertTrue(group.isEnabled());
        Assertions.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    void selectDisabledItem_noRedundantEvent() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> keys = group.getChildren().map(Component::getElement)
                .map(element -> element.getProperty("value"))
                .collect(Collectors.toList());
        String enabledKey = keys.get(0);
        String disabledKey = keys.get(1);

        group.getElement().setProperty("value", disabledKey);
        Assertions.assertNull(group.getValue());
        Assertions.assertTrue(events.isEmpty());

        group.getElement().setProperty("value", enabledKey);
        Assertions.assertEquals("enabled", group.getValue());
        Assertions.assertEquals(1, events.size());

        ValueChangeEvent<String> event = events.get(0);
        Assertions.assertNull(event.getOldValue());
        Assertions.assertEquals("enabled", event.getValue());
    }

    @Test
    void disabledItems_itemEnabledProvider_stayDisabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<RadioButton<String>> children = group.getChildren()
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());

        Assertions.assertTrue(children.get(0).isEnabled());
        Assertions.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assertions.assertFalse(children.get(0).isEnabled());
        Assertions.assertFalse(children.get(1).isEnabled());

        group.setEnabled(true);
        Assertions.assertTrue(children.get(0).isEnabled());
        Assertions.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assertions.assertFalse(children.get(0).isEnabled());
        Assertions.assertFalse(children.get(1).isEnabled());
    }

    @Test
    void changeItems_selectionIsReset() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setItems("Foo", "Bar");

        AtomicReference<String> capture = new AtomicReference<>();
        radioButtonGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        radioButtonGroup.setValue("Foo");

        Assertions.assertEquals("Foo", capture.get());

        Assertions.assertEquals("Foo", radioButtonGroup.getValue());

        radioButtonGroup.setItems("Foo", "Baz");

        Assertions.assertEquals(null, radioButtonGroup.getValue());
        Assertions.assertEquals(null, capture.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testResetAllItems() {
        RadioButtonGroup<ItemHelper> group = new RadioButtonGroup<ItemHelper>();
        ItemHelper item1 = new ItemHelper("foo", "01");
        ItemHelper item2 = new ItemHelper("baz", "02");

        RadioButtonGroupListDataView<ItemHelper> dataView = group
                .setItems(item1, item2);

        item1.setName("zoo");
        item2.setName("bar");
        dataView.refreshItem(item1);
        dataView.refreshItem(item2);

        List<Component> components = group.getChildren()
                .collect(Collectors.toList());
        RadioButton<ItemHelper> radioZoo = (RadioButton<ItemHelper>) components
                .get(0);
        RadioButton<ItemHelper> radioBar = (RadioButton<ItemHelper>) components
                .get(1);

        Assertions.assertEquals(String.format(OUTER_HTML, "zoo"),
                radioZoo.getElement().getOuterHTML());
        Assertions.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testResetSingleItem() {
        RadioButtonGroup<ItemHelper> group = new RadioButtonGroup<ItemHelper>();
        ItemHelper item1 = new ItemHelper("foo", "01");
        ItemHelper item2 = new ItemHelper("baz", "02");

        RadioButtonGroupListDataView<ItemHelper> dataView = group
                .setItems(item1, item2);

        item1.setName("zoo");
        item2.setName("bar");
        dataView.refreshItem(item2);

        List<Component> components = group.getChildren()
                .collect(Collectors.toList());
        RadioButton<ItemHelper> radioFoo = (RadioButton<ItemHelper>) components
                .get(0);
        RadioButton<ItemHelper> radioBar = (RadioButton<ItemHelper>) components
                .get(1);

        Assertions.assertEquals(String.format(OUTER_HTML, "foo"),
                radioFoo.getElement().getOuterHTML());
        Assertions.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-radio-group");
        element.setProperty("value", "foo");

        Instantiator instantiator = Mockito.mock(Instantiator.class);
        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(RadioButtonGroup.class))
                .thenAnswer(invocation -> new RadioButtonGroup());
        RadioButtonGroup field = Component.from(element,
                RadioButtonGroup.class);
        Assertions.assertEquals("foo",
                field.getElement().getPropertyRaw("value"));
    }

    @Test
    void dataViewForFaultyDataProvider_throwsException() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        final RadioButtonGroupListDataView<String> listDataView = radioButtonGroup
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        radioButtonGroup.setItems(dataProvider);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> radioButtonGroup.getListDataView());
        Assertions.assertTrue(exception.getMessage().contains(
                "RadioButtonGroupListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'"));
    }

    @Test
    void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        radioButtonGroup.setValue(new CustomItem(1L));

        Assertions.assertNotNull(radioButtonGroup.getValue());
        Assertions.assertEquals("First", radioButtonGroup.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        radioButtonGroup.setValue(new CustomItem(2L));

        Assertions.assertNotNull(radioButtonGroup.getValue());
        Assertions.assertEquals(Long.valueOf(2L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        radioButtonGroup.setValue(new CustomItem(1L));

        Assertions.assertNotNull(radioButtonGroup.getValue());
        Assertions.assertEquals("First", radioButtonGroup.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        radioButtonGroup.setValue(new CustomItem(3L, "Second"));

        Assertions.assertNotNull(radioButtonGroup.getValue());
        Assertions.assertEquals(Long.valueOf(3L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);

        radioButtonGroup.setValue(new CustomItem(null, "Second"));

        Assertions.assertNotNull(radioButtonGroup.getValue());
        Assertions.assertEquals(Long.valueOf(2L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        radioButtonGroup.setValue(new CustomItem(null, "First"));
        Assertions.assertNull(radioButtonGroup.getValue().getId());
    }

    @Test
    void addNullOption_setValue() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled", null);
        group.setValue(null);
        Assertions.assertEquals(null, group.getValue());
    }

    @Test
    void setItemEnabledProvider_nullValue_doesNotThrow() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("Foo", "Bar", "Baz");
        group.setValue("Foo");
        group.setItemEnabledProvider(it -> it.equals("Foo"));

        group.getElement().setProperty("value", null);

        ComponentUtil.fireEvent(group,
                new ComponentValueChangeEvent<>(group, group, "Foo", true));
    }

    @Test
    void dataProviderListeners_radioButtonGroupAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        new RadioButtonGroup<>(), 1, 1, new int[] { 0, 1 },
                        ui.getUI());
    }

    @Test
    void implementsHasTooltip() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        Assertions.assertTrue(group instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(RadioButtonGroup.class));
    }

    @Test
    void setAriaLabel() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setAriaLabel("aria-label");

        Assertions.assertTrue(group.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", group.getAriaLabel().get());

        group.setAriaLabel(null);
        Assertions.assertTrue(group.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(group.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                group.getAriaLabelledBy().get());

        group.setAriaLabelledBy(null);
        Assertions.assertTrue(group.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        RadioButtonGroup<String> field = new RadioButtonGroup<String>();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String>, String>);
    }

    @Test
    void discardSelectionOnDataChange_noExtraChangeEventsFired() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);

        String selectedItem = items.get(0);
        group.setValue(selectedItem);
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertNull(group.getValue());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void preserveExistingSelectionOnDataChange_noExtraChangeEventsFired() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);

        String selectedItem = items.get(0);
        group.setValue(selectedItem);
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        group.getDataProvider().refreshAll();
        Assertions.assertNull(group.getValue());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void preserveAllSelectionOnDataChange_noExtraChangeEventsFired() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_ALL);

        String selectedItem = items.get(0);
        group.setValue(selectedItem);
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(selectedItem, group.getValue());
        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void refreshItem_selectFromClient_valueContainsUpdatedItem() {
        RadioButtonGroup<CustomItem> group = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> dataView = group.setItems(
                new CustomItem(1L, "foo"), new CustomItem(2L, "bar"),
                new CustomItem(3L, "baz"));
        dataView.setIdentifierProvider(CustomItem::getId);

        CustomItem updatedItem = new CustomItem(2L, "updated");
        dataView.refreshItem(updatedItem);

        AtomicReference<CustomItem> selectedItem = new AtomicReference<>();
        group.addValueChangeListener(e -> selectedItem.set(e.getValue()));

        // Simulate selecting an item from the client side via key
        String itemKey = group.getChildren().skip(1).findFirst().orElseThrow()
                .getElement().getProperty("value");
        group.getElement().setProperty("value", itemKey);

        Assertions.assertEquals("updated", selectedItem.get().getName());
        Assertions.assertEquals("updated", group.getValue().getName());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(RadioButtonGroup.class));
    }
}
