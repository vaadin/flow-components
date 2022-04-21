/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import elemental.json.JsonArray;

public class CheckboxGroupTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void hasEmptySetAsDefaultValue() {
        Set<Object> value = new CheckboxGroup<>().getValue();
        Assert.assertNotNull(value);
        Assert.assertTrue(value.isEmpty());
    }

    @Test
    public void setValueNull_throws() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Use the clear-method");
        new CheckboxGroup<>().setValue(null);
    }

    @Test
    public void setReadOnlyCheckboxGroup_groupIsReadOnlyAndDisabled() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assert.assertTrue(group.isReadOnly());

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(group.getChildren().count(), disabledChildCount);
    }

    @Test
    public void setReadOnlyDisabledCheckboxGroup_groupIsDisabledAndReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyDisabledCheckboxGroup_groupIsDisabledAndNotReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assert.assertFalse(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void setReadOnlyEnabledCheckboxGroup_groupIsDisabledAndNotReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setReadOnly(true);
        group.setEnabled(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyEnabledCheckboxGroup_groupIsEnabled() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);
        group.setEnabled(true);

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void selectDisabledItem_noRedundantEvent() {
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

        JsonArray array = Json.createArray();
        array.set(0, disabledKey);

        group.getElement().setPropertyJson("value", array);
        Assert.assertThat(group.getValue(), IsEmptyCollection.empty());
        Assert.assertTrue(events.isEmpty());

        array = Json.createArray();
        array.set(0, enabledKey);

        group.getElement().setPropertyJson("value", array);
        Assert.assertEquals(Collections.singleton("enabled"), group.getValue());
        Assert.assertEquals(1, events.size());

        ValueChangeEvent<Set<String>> event = events.get(0);
        Assert.assertThat(event.getOldValue(), IsEmptyCollection.empty());
        Assert.assertEquals(Collections.singleton("enabled"), event.getValue());
    }

    @Test
    public void changeItems_selectionIsReset() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        AtomicReference<Set<String>> capture = new AtomicReference<>();
        checkboxGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        checkboxGroup.setValue(Collections.singleton("Foo"));

        Assert.assertEquals(Collections.singleton("Foo"), capture.get());

        Assert.assertEquals(Collections.singleton("Foo"),
                checkboxGroup.getValue());

        checkboxGroup.setItems("Foo", "Baz");

        Assert.assertThat(checkboxGroup.getValue(), IsEmptyCollection.empty());
        Assert.assertThat(capture.get(), IsEmptyCollection.empty());
    }

    @Test
    public void deselectAll_selectionIsReset() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        AtomicReference<Set<String>> capture = new AtomicReference<>();
        checkboxGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        checkboxGroup.setValue(Collections.singleton("Foo"));

        Assert.assertEquals(Collections.singleton("Foo"), capture.get());

        Assert.assertEquals(Collections.singleton("Foo"),
                checkboxGroup.getValue());

        checkboxGroup.deselectAll();

        Assert.assertThat(checkboxGroup.getValue(), IsEmptyCollection.empty());
        Assert.assertThat(capture.get(), IsEmptyCollection.empty());
    }

    @Test
    public void updateSelection_checkboxesUpdated() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        List<Checkbox> checkboxes = checkboxGroup.getChildren()
                .map(Checkbox.class::cast).collect(Collectors.toList());

        checkboxGroup.select("Foo");
        Assert.assertTrue(checkboxes.get(0).getValue());
        Assert.assertFalse(checkboxes.get(1).getValue());

        checkboxGroup.deselectAll();
        Assert.assertFalse(checkboxes.get(0).getValue());
        Assert.assertFalse(checkboxes.get(1).getValue());
    }

    @Test
    public void singleDataRefreshEvent() {
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
        checkboxGroup.getDataProvider().refreshItem(item1);
        assertCheckboxLabels(checkboxGroup, "etc", "bar");

    }

    @Test
    public void singleDataRefreshEvent_overrideDataProviderGetId() {
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
        checkboxGroup.getDataProvider().refreshItem(new Wrapper(1));
        assertCheckboxLabels(checkboxGroup, "etc", "bar");

    }

    @Test
    public void allDataRefreshEvent() {
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
        checkboxGroup.getDataProvider().refreshAll();
        assertCheckboxLabels(checkboxGroup, "etc", "opt");

    }

    @Test
    public void addSelectionListener_selectionEventIsFired() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        AtomicReference<MultiSelectionEvent<CheckboxGroup<String>, String>> eventCapture = new AtomicReference<>();
        checkboxGroup.addSelectionListener(event -> {
            Assert.assertNull(eventCapture.get());
            eventCapture.set(event);
        });

        checkboxGroup.setValue(Collections.singleton("bar"));

        Assert.assertNotNull(eventCapture.get());
        Assert.assertEquals(Collections.emptySet(),
                eventCapture.get().getOldValue());
        Assert.assertEquals(Collections.singleton("bar"),
                eventCapture.get().getNewSelection());

        eventCapture.set(null);

        checkboxGroup.select("foo", "bar");
        Assert.assertNotNull(eventCapture.get());
        Assert.assertEquals(Collections.singleton("bar"),
                eventCapture.get().getOldSelection());
        Assert.assertEquals(2, eventCapture.get().getNewSelection().size());

        Set<String> newSelection = eventCapture.get().getNewSelection();
        Assert.assertTrue(newSelection.contains("foo"));
        Assert.assertTrue(newSelection.contains("bar"));
    }

    @Test // https://github.com/vaadin/vaadin-checkbox-flow/issues/81
    public void disableParent_detachParent_notThrowing() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("foo", "bar");

        Div parent = new Div(checkboxGroup);

        UI ui = new UI();
        ui.add(parent);

        parent.setEnabled(false);
        ui.remove(parent);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-checkbox-group");
        JsonArray array = Json.createArray();
        array.set(0, "foo");
        element.setPropertyJson("value", array);
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(CheckboxGroup.class))
                .thenAnswer(invocation -> new CheckboxGroup());
        CheckboxGroup field = Component.from(element, CheckboxGroup.class);
        JsonArray propertyValue = (JsonArray) field.getElement()
                .getPropertyRaw("value");
        Assert.assertEquals(1, propertyValue.length());
        Assert.assertEquals("foo", propertyValue.getString(0));
    }

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(
                "CheckboxGroupListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'");

        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        final CheckboxGroupListDataView<String> listDataView = checkboxGroup
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        checkboxGroup.setDataProvider(dataProvider);

        checkboxGroup.getListDataView();
    }

    @Test
    public void setIdentifierProvider_setItemsWithIdentifierOnly_shouldSelectCorrectItem() {
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

        Assert.assertNotNull(
                checkboxGroup.getSelectedItems().stream().findFirst().get());
        Assert.assertEquals("First", checkboxGroup.getSelectedItems().stream()
                .map(CustomItem::getName).findFirst().get());
        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assert.assertArrayEquals(new long[] { 1L }, selectedIds);

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
        Assert.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    public void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
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

        Assert.assertNotNull(
                checkboxGroup.getSelectedItems().stream().findFirst().get());
        Assert.assertEquals("First", checkboxGroup.getSelectedItems().stream()
                .map(CustomItem::getName).findFirst().get());
        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();
        Assert.assertArrayEquals(new long[] { 1L }, selectedIds);

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
        Assert.assertArrayEquals(new long[] { 3L }, selectedIds);
    }

    @Test
    public void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
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

        Assert.assertNotNull(checkboxGroup.getValue());

        long[] selectedIds = checkboxGroup.getSelectedItems().stream()
                .mapToLong(CustomItem::getId).toArray();

        Assert.assertArrayEquals(new long[] { 2L }, selectedIds);
    }

    @Test
    public void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
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
        Assert.assertNull(checkboxGroup.getSelectedItems().stream().findFirst()
                .get().getId());
    }

    @Test
    public void setItems_createsLabelValueEventAndItems() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        CheckboxGroup<CustomItem> checkboxGroup = new CheckboxGroup<>("label",
                capture::set, first, second, third);

        Assert.assertEquals("Invalid number of items", 3,
                checkboxGroup.getChildren().count());

        Assert.assertEquals("Invalid label for checkbox group ", "label",
                checkboxGroup.getElement().getProperty("label"));
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
        Assert.assertEquals(2, components.size());
        Assert.assertEquals(firstLabel,
                ((Checkbox) components.get(0)).getLabel());
        Assert.assertEquals(secondLabel,
                ((Checkbox) components.get(1)).getLabel());
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
