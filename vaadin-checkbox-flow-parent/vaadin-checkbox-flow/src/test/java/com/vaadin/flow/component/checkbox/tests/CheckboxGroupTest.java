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
package com.vaadin.flow.component.checkbox.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;

class CheckboxGroupTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void hasEmptySetAsDefaultValue() {
        Set<Object> value = new CheckboxGroup<>().getValue();
        Assertions.assertNotNull(value);
        Assertions.assertTrue(value.isEmpty());
    }

    @Test
    void setValueNull_throws() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new CheckboxGroup<>().setValue(null));
        Assertions.assertTrue(
                exception.getMessage().contains("Use the clear-method"));
    }

    @Test
    void setReadOnlyCheckboxGroup_groupIsReadOnly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assertions.assertTrue(group.isReadOnly());

        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));
    }

    @Test
    void setReadOnlyDisabledCheckboxGroup_groupIsDisabledAndReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assertions.assertTrue(group.isReadOnly());
        Assertions.assertFalse(group.isEnabled());
        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    void unsetReadOnlyDisabledCheckboxGroup_groupIsDisabledAndNotReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assertions.assertFalse(group.isReadOnly());
        Assertions.assertFalse(group.isEnabled());
        Assertions.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    void unsetReadOnlyEnabledCheckboxGroup_groupIsEnabled() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
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
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> keys = group.getChildren().map(Component::getElement)
                .map(element -> element.getProperty("value"))
                .collect(Collectors.toList());
        String enabledKey = keys.get(0);
        String disabledKey = keys.get(1);

        ArrayNode array = JacksonUtils.createArrayNode();
        array.add(disabledKey);

        group.getElement().setPropertyJson("value", array);
        Assertions.assertTrue(group.getValue().isEmpty(),
                "Group value should be empty");
        Assertions.assertTrue(events.isEmpty());

        array = JacksonUtils.createArrayNode();
        array.add(enabledKey);

        group.getElement().setPropertyJson("value", array);
        Assertions.assertEquals(Collections.singleton("enabled"),
                group.getValue());
        Assertions.assertEquals(1, events.size());

        ValueChangeEvent<Set<String>> event = events.get(0);
        Assertions.assertTrue(event.getOldValue().isEmpty(),
                "Event old value should be empty");
        Assertions.assertEquals(Collections.singleton("enabled"),
                event.getValue());
    }

    @Test
    void changeItems_selectionIsReset() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        AtomicReference<Set<String>> capture = new AtomicReference<>();
        checkboxGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        checkboxGroup.setValue(Collections.singleton("Foo"));

        Assertions.assertEquals(Collections.singleton("Foo"), capture.get());

        Assertions.assertEquals(Collections.singleton("Foo"),
                checkboxGroup.getValue());

        checkboxGroup.setItems("Foo", "Baz");

        Assertions.assertTrue(checkboxGroup.getValue().isEmpty(),
                "Checkbox group value should be empty");
        Assertions.assertTrue(capture.get().isEmpty(),
                "Captured value should be empty");
    }

    @Test
    void deselectAll_selectionIsReset() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        AtomicReference<Set<String>> capture = new AtomicReference<>();
        checkboxGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        checkboxGroup.setValue(Collections.singleton("Foo"));

        Assertions.assertEquals(Collections.singleton("Foo"), capture.get());

        Assertions.assertEquals(Collections.singleton("Foo"),
                checkboxGroup.getValue());

        checkboxGroup.deselectAll();

        Assertions.assertTrue(checkboxGroup.getValue().isEmpty(),
                "Checkbox group value should be empty");
        Assertions.assertTrue(capture.get().isEmpty(),
                "Captured value should be empty");
    }

    @Test
    void updateSelection_checkboxesUpdated() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        List<Checkbox> checkboxes = checkboxGroup.getChildren()
                .map(Checkbox.class::cast).collect(Collectors.toList());

        checkboxGroup.select("Foo");
        Assertions.assertTrue(checkboxes.get(0).getValue());
        Assertions.assertFalse(checkboxes.get(1).getValue());

        checkboxGroup.deselectAll();
        Assertions.assertFalse(checkboxes.get(0).getValue());
        Assertions.assertFalse(checkboxes.get(1).getValue());
    }

    @Test
    void singleDataRefreshEvent() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup = getRefreshEventCheckboxGroup(
                items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getListDataView().refreshItem(item1);
        assertCheckboxLabels(checkboxGroup, "etc", "bar");

    }

    @Test
    void singleDataRefreshEvent_overrideDataProviderGetId() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup = getRefreshEventCheckboxGroupWithCustomDataProvider(
                items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getListDataView().refreshItem(new Wrapper(1));
        assertCheckboxLabels(checkboxGroup, "etc", "bar");

    }

    @Test
    void allDataRefreshEvent() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup = getRefreshEventCheckboxGroup(
                items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getListDataView().refreshAll();
        assertCheckboxLabels(checkboxGroup, "etc", "opt");

    }

    @Test
    void selectItem_setItemLabelGenerator_selectionIsRetained() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        checkboxGroup.setValue(Set.of("foo"));
        Assertions.assertEquals(Set.of("foo"), checkboxGroup.getValue());

        checkboxGroup.setItemLabelGenerator(item -> item + " (Updated)");

        Assertions.assertEquals(Set.of("foo"), checkboxGroup.getValue());
    }

    @Test
    void setItemLabelGenerator_labelIsUpdated() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        Checkbox cb = (Checkbox) checkboxGroup.getChildren().findFirst().get();
        Assertions.assertEquals("foo", cb.getLabel());

        checkboxGroup.setItemLabelGenerator(item -> item + " (Updated)");

        Assertions.assertEquals("foo (Updated)", cb.getLabel());
    }

    @Test
    void setItemLabelGenerator_removesItemRenderer() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");
        checkboxGroup.setRenderer(new TextRenderer<>());

        Assertions.assertTrue(
                checkboxGroup.getItemRenderer() instanceof TextRenderer);

        checkboxGroup.setItemLabelGenerator(item -> item);

        Assertions.assertNull(checkboxGroup.getItemRenderer());
    }

    @Test
    void addSelectionListener_selectionEventIsFired() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        AtomicReference<MultiSelectionEvent<CheckboxGroup<String>, String>> eventCapture = new AtomicReference<>();
        checkboxGroup.addSelectionListener(event -> {
            Assertions.assertNull(eventCapture.get());
            eventCapture.set(event);
        });

        checkboxGroup.setValue(Collections.singleton("bar"));

        Assertions.assertNotNull(eventCapture.get());
        Assertions.assertEquals(Collections.emptySet(),
                eventCapture.get().getOldValue());
        Assertions.assertEquals(Collections.singleton("bar"),
                eventCapture.get().getValue());

        eventCapture.set(null);

        checkboxGroup.select("foo", "bar");
        Assertions.assertNotNull(eventCapture.get());
        Assertions.assertEquals(Collections.singleton("bar"),
                eventCapture.get().getOldSelection());
        Assertions.assertEquals(2, eventCapture.get().getValue().size());

        Set<String> newSelection = eventCapture.get().getValue();
        Assertions.assertTrue(newSelection.contains("foo"));
        Assertions.assertTrue(newSelection.contains("bar"));
    }

    @Test // https://github.com/vaadin/vaadin-checkbox-flow/issues/81
    void disableParent_detachParent_notThrowing() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        Div parent = new Div(checkboxGroup);

        ui.add(parent);

        parent.setEnabled(false);
        ui.remove(parent);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-checkbox-group");
        ArrayNode array = JacksonUtils.createArrayNode();
        array.add("foo");
        element.setPropertyJson("value", array);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(CheckboxGroup.class))
                .thenAnswer(invocation -> new CheckboxGroup());
        CheckboxGroup field = Component.from(element, CheckboxGroup.class);
        ArrayNode propertyValue = (ArrayNode) field.getElement()
                .getPropertyRaw("value");
        Assertions.assertEquals(1, propertyValue.size());
        Assertions.assertEquals("foo", propertyValue.get(0).asString());
    }

    @Test
    void dataViewForFaultyDataProvider_throwsException() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        final CheckboxGroupListDataView<String> listDataView = checkboxGroup
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        checkboxGroup.setItems(dataProvider);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> checkboxGroup.getListDataView());
        Assertions.assertTrue(exception.getMessage().contains(
                "CheckboxGroupListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'"));
    }

    @Test
    void setIdentifierProvider_setItemsWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroupListDataView<CustomItem> listDataView = checkboxGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        checkboxGroup.setValue(Collections.singleton(new CustomItem(1L)));

        Assertions.assertNotNull(
                checkboxGroup.getSelectedItems().stream().findFirst().get());
        Assertions.assertEquals("First", checkboxGroup.getSelectedItems()
                .stream().map(CustomItem::getName).findFirst().get());
        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 1L }, selectedIds);

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        checkboxGroup.setValue(Collections.singleton(new CustomItem(2L)));

        selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroupListDataView<CustomItem> listDataView = checkboxGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        checkboxGroup.setValue(Collections.singleton(new CustomItem(1L)));

        Assertions.assertNotNull(
                checkboxGroup.getSelectedItems().stream().findFirst().get());
        Assertions.assertEquals("First", checkboxGroup.getSelectedItems()
                .stream().map(CustomItem::getName).findFirst().get());
        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 1L }, selectedIds);

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        checkboxGroup
                .setValue(Collections.singleton(new CustomItem(3L, "Second")));

        selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assertions.assertArrayEquals(new long[] { 3L }, selectedIds);
    }

    @Test
    void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroupListDataView<CustomItem> listDataView = checkboxGroup
                .setItems(items);

        checkboxGroup.setValue(
                Collections.singleton(new CustomItem(null, "Second")));

        Assertions.assertNotNull(checkboxGroup.getValue());

        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();

        Assertions.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroupListDataView<CustomItem> listDataView = checkboxGroup
                .setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        checkboxGroup
                .setValue(Collections.singleton(new CustomItem(null, "First")));
        Assertions.assertNull(checkboxGroup.getSelectedItems().stream()
                .findFirst().get().getId());
    }

    @Test
    void setItems_createsLabelValueEventAndItems() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>("label",
                capture::set, first, second, third);

        Assertions.assertEquals(3, checkboxGroup.getChildren().count(),
                "Invalid number of items");

        Assertions.assertEquals("label",
                checkboxGroup.getElement().getProperty("label"),
                "Invalid label for checkbox group ");
    }

    @Test
    void implementsHasTooltip() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        Assertions.assertTrue(group instanceof HasTooltip);
    }

    private CheckboxGroup<Wrapper> getRefreshEventCheckboxGroup(
            List<Wrapper> items) {
        CheckboxGroup<Wrapper> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItemLabelGenerator(Wrapper::getLabel);
        ListDataProvider<Wrapper> dataProvider = new ListDataProvider<>(items);
        checkboxGroup.setItems(dataProvider);
        return checkboxGroup;
    }

    private CheckboxGroup<Wrapper> getRefreshEventCheckboxGroupWithCustomDataProvider(
            List<Wrapper> items) {
        CheckboxGroup<Wrapper> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItemLabelGenerator(Wrapper::getLabel);
        ListDataProvider<Wrapper> dataProvider = new CustomDataProvider(items);
        checkboxGroup.setItems(dataProvider);
        return checkboxGroup;
    }

    private void assertCheckboxLabels(CheckboxGroup<Wrapper> checkboxGroup,
            String firstLabel, String secondLabel) {
        List<Component> components = checkboxGroup.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(2, components.size());
        Assertions.assertEquals(firstLabel,
                ((Checkbox) components.get(0)).getLabel());
        Assertions.assertEquals(secondLabel,
                ((Checkbox) components.get(1)).getLabel());
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(CheckboxGroup.class));
    }

    @Test
    void setAriaLabel() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setAriaLabel("aria-label");

        Assertions.assertTrue(group.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", group.getAriaLabel().get());

        group.setAriaLabel(null);
        Assertions.assertTrue(group.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(group.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                group.getAriaLabelledBy().get());

        group.setAriaLabelledBy(null);
        Assertions.assertTrue(group.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        CheckboxGroup<String> field = new CheckboxGroup<String>();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<CheckboxGroup<String>, Set<String>>, Set<String>>);
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(CheckboxGroup.class));
    }

    @Test
    void discardSelectionOnDataChange_noExtraChangeEventsFired() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);

        String selectedItem = items.get(0);
        group.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertTrue(group.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void preserveExistingSelectionOnDataChange_noExtraChangeEventsFired() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);

        String selectedItem = items.get(0);
        group.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        group.getDataProvider().refreshAll();
        Assertions.assertTrue(group.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void preserveAllSelectionOnDataChange_noExtraChangeEventsFired() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        group.setItems(items);

        group.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_ALL);

        String selectedItem = items.get(0);
        group.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertEquals(1, events.size());
        events.clear();

        group.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        group.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), group.getValue());
        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void refreshItem_selectFromClient_valueContainsUpdatedItem() {
        CheckboxGroup<CustomItem> group = new CheckboxGroup<>();
        CheckboxGroupListDataView<CustomItem> dataView = group.setItems(
                new CustomItem(1L, "foo"), new CustomItem(2L, "bar"),
                new CustomItem(3L, "baz"));
        dataView.setIdentifierProvider(CustomItem::getId);

        CustomItem updatedItem = new CustomItem(2L, "updated");
        dataView.refreshItem(updatedItem);

        AtomicReference<Set<CustomItem>> selectedItems = new AtomicReference<>();
        group.addValueChangeListener(e -> selectedItems.set(e.getValue()));

        // Simulate selecting an item from the client side via key
        String itemKey = group.getChildren().skip(1).findFirst().orElseThrow()
                .getElement().getProperty("value");
        ArrayNode selection = JacksonUtils.createArrayNode();
        selection.add(itemKey);
        group.getElement().setPropertyJson("value", selection);

        Assertions.assertEquals("updated",
                selectedItems.get().stream().findFirst().orElseThrow().name);
        Assertions.assertEquals("updated",
                group.getValue().stream().findFirst().orElseThrow().name);
    }

    /**
     * Used in the tests {@link #singleDataRefreshEvent()} and
     * {@link #allDataRefreshEvent()}
     */
    private class Wrapper {

        private int id;
        private String label;

        public Wrapper(int id) {
            this.id = id;
        }

        Wrapper(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        String getLabel() {
            return label;
        }

        void setLabel(String label) {
            this.label = label;
        }
    }

    private class CustomDataProvider extends ListDataProvider<Wrapper> {

        /**
         * Constructs a new ListDataProvider.
         * <p>
         * No protective copy is made of the list, and changes in the provided
         * backing Collection will be visible via this data provider. The caller
         * should copy the list if necessary.
         *
         * @param items
         *            the initial data, not null
         */
        public CustomDataProvider(Collection<Wrapper> items) {
            super(items);
        }

        /**
         * Gets an identifier for the given Wrapper. This identifier is used by
         * the framework to determine equality between two Wrappers.
         *
         * @param wrapper
         *            the Wrapper to get identifier for; not {@code null}
         * @return the identifier for given wrapper; not {@code null}
         */
        @Override
        public Object getId(Wrapper wrapper) {
            return wrapper.getId();
        }
    }

    private class CustomItem {
        private Long id;
        private String name;

        public CustomItem(Long id) {
            this(id, null);
        }

        public CustomItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CustomItem))
                return false;
            CustomItem that = (CustomItem) o;
            return Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName());
        }
    }
}
