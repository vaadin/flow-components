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
package com.vaadin.flow.component.radiobutton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.tests.DataProviderListenersTest;

public class RadioButtonGroupTest {

    private static final String OUTER_HTML = "<vaadin-radio-button>\n <label slot=\"label\"><span>%s</span></label>\n</vaadin-radio-button>";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private class RadioButtonWithInitialValue extends
            GeneratedVaadinRadioGroup<RadioButtonWithInitialValue, String> {
        RadioButtonWithInitialValue() {
            super("", null, String.class, (group, value) -> value,
                    (group, value) -> value, true);
        }
    }

    @Test
    public void setReadOnlyRadioGroup_groupIsReadOnly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assert.assertTrue(group.isReadOnly());

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(group.getChildren().count(), disabledChildCount);
    }

    @Test
    public void setReadOnlyRadioGroup_checkedButtonIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setValue("foo");
        group.setReadOnly(true);

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(1, disabledChildCount);
    }

    @Test
    public void setReadOnlyRadioGroup_checkedButtonIsEnabled2() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        group.setValue("foo");

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(1, disabledChildCount);
    }

    @Test
    public void setReadOnlyDisabledRadioGroup_groupIsDisabledAndReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyDisabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assert.assertFalse(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void setReadOnlyEnabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setReadOnly(true);
        group.setEnabled(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("readonly"));
    }

    @Test
    public void unsetReadOnlyEnabledRadioGroup_groupIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
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
        Assert.assertNull(group.getValue());
        Assert.assertTrue(events.isEmpty());

        group.getElement().setProperty("value", enabledKey);
        Assert.assertEquals("enabled", group.getValue());
        Assert.assertEquals(1, events.size());

        ValueChangeEvent<String> event = events.get(0);
        Assert.assertNull(event.getOldValue());
        Assert.assertEquals("enabled", event.getValue());
    }

    @Test
    public void disabledItems_itemEnabledProvider_stayDisabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<RadioButton<String>> children = group.getChildren()
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());

        Assert.assertTrue(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assert.assertFalse(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(true);
        Assert.assertTrue(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assert.assertFalse(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());
    }

    @Test
    public void changeItems_selectionIsReset() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setItems("Foo", "Bar");

        AtomicReference<String> capture = new AtomicReference<>();
        radioButtonGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        radioButtonGroup.setValue("Foo");

        Assert.assertEquals("Foo", capture.get());

        Assert.assertEquals("Foo", radioButtonGroup.getValue());

        radioButtonGroup.setItems("Foo", "Baz");

        Assert.assertEquals(null, radioButtonGroup.getValue());
        Assert.assertEquals(null, capture.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResetAllItems() {
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

        Assert.assertEquals(String.format(OUTER_HTML, "zoo"),
                radioZoo.getElement().getOuterHTML());
        Assert.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResetSingleItem() {
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

        Assert.assertEquals(String.format(OUTER_HTML, "foo"),
                radioFoo.getElement().getOuterHTML());
        Assert.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-radio-group");
        element.setProperty("value", "foo");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(
                instantiator.createComponent(RadioButtonWithInitialValue.class))
                .thenAnswer(invocation -> new RadioButtonWithInitialValue());
        RadioButtonWithInitialValue field = Component.from(element,
                RadioButtonWithInitialValue.class);
        Assert.assertEquals("foo", field.getElement().getPropertyRaw("value"));
    }

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(
                "RadioButtonGroupListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'");

        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        final RadioButtonGroupListDataView<String> listDataView = radioButtonGroup
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        radioButtonGroup.setItems(dataProvider);

        radioButtonGroup.getListDataView();
    }

    @Test
    public void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
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

        Assert.assertNotNull(radioButtonGroup.getValue());
        Assert.assertEquals(radioButtonGroup.getValue().getName(), "First");

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        radioButtonGroup.setValue(new CustomItem(2L));

        Assert.assertNotNull(radioButtonGroup.getValue());
        Assert.assertEquals(Long.valueOf(2L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    public void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
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

        Assert.assertNotNull(radioButtonGroup.getValue());
        Assert.assertEquals("First", radioButtonGroup.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        radioButtonGroup.setValue(new CustomItem(3L, "Second"));

        Assert.assertNotNull(radioButtonGroup.getValue());
        Assert.assertEquals(Long.valueOf(3L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    public void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        RadioButtonGroup<CustomItem> radioButtonGroup = new RadioButtonGroup<>();
        RadioButtonGroupListDataView<CustomItem> listDataView = radioButtonGroup
                .setItems(items);

        radioButtonGroup.setValue(new CustomItem(null, "Second"));

        Assert.assertNotNull(radioButtonGroup.getValue());
        Assert.assertEquals(Long.valueOf(2L),
                radioButtonGroup.getValue().getId());
    }

    @Test
    public void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
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
        Assert.assertNull(radioButtonGroup.getValue().getId());
    }

    @Test
    public void addNullOption_setValue() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled", null);
        group.setValue(null);
        Assert.assertEquals(group.getValue(), null);
    }

    @Test
    public void dataProviderListeners_radioButtonGroupAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        new RadioButtonGroup<>(), 1, 1, new int[] { 0, 1 },
                        new DataCommunicatorTest.MockUI());
    }
}
