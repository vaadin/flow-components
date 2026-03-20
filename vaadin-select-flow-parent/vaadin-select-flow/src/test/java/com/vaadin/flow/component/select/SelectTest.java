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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.data.SelectListDataView;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.tests.MockUIExtension;

class SelectTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Select<String> select;
    private Supplier<Select> selectSupplier = () -> select;

    @BeforeEach
    void setup() {
        select = new Select<>();
    }

    @Test
    void defaults() {
        Assertions.assertNull(select.getValue(),
                "Default value should be null");
        Assertions.assertNull(select.getEmptyValue(),
                "Empty value should be null");

        Assertions.assertEquals(0, select.getChildren().count(),
                "Select should not have any children by default");

        Assertions.assertFalse(select.isEmptySelectionAllowed(),
                "Empty selection is not allowed by default");
        Assertions.assertEquals("", select.getEmptySelectionCaption(),
                "Empty selection caption is empty string by default");

        Assertions.assertNull(select.getItemEnabledProvider(),
                "Default item enabled generator null");
        Assertions.assertNull(select.getItemLabelGenerator(),
                "By default item label generator should not be set");
        Assertions.assertNull(select.getItemRenderer(),
                "Default renderer is null");

        Assertions.assertNull(select.getPlaceholder(),
                "Default placeholder is null");
        Assertions.assertNull(select.getLabel(), "Default label is null");
        Assertions.assertFalse(select.isAutofocus(), "No autofocus by default");
        Assertions.assertFalse(select.isInvalid(),
                "Component is in valid state by default");
        Assertions.assertFalse(select.isRequiredIndicatorVisible(),
                "No required indicator by default");
        Assertions.assertNull(select.getErrorMessage(),
                "Default error message is null");

        Assertions.assertTrue(select.isEnabled(),
                "Component is enabled by default");
        Assertions.assertFalse(select.isReadOnly(),
                "Component is not readonly by default");
    }

    @Test
    void basicProperties() {
        select.setPlaceholder("placeholder");
        select.setLabel("label");
        select.setInvalid(true);
        select.setAutofocus(true);
        select.setRequiredIndicatorVisible(true);
        select.setErrorMessage("errorMessage");
        select.setReadOnly(true);
        select.setEnabled(false);

        Assertions.assertEquals("placeholder", select.getPlaceholder(),
                "Wrong placeholder");
        Assertions.assertEquals("label", select.getLabel(), "Wrong label");
        Assertions.assertTrue(select.isAutofocus(), "Autofocus not set");
        Assertions.assertTrue(select.isInvalid(), "Invalid state not set");
        Assertions.assertTrue(select.isRequiredIndicatorVisible(),
                "Required indicator not set");
        Assertions.assertEquals("errorMessage", select.getErrorMessage(),
                "Error message is not set");
        Assertions.assertTrue(select.isReadOnly(), "ReadOnly not set");
        Assertions.assertFalse(select.isEnabled(), "Disabled not set");
    }

    @Test
    void defaultValue_clearSetsToNull() {
        select.setItems("foo", "bar");
        select.setValue("foo");

        Assertions.assertEquals("foo", select.getValue());

        select.clear();
        Assertions.assertNull(select.getValue());
    }

    @Test
    void setItems_createsItems() {
        Assertions.assertEquals(0, getListBox().getChildren().count(),
                "Invalid number of items");

        select = new Select<>("label", "foo", "bar", "baz");

        Assertions.assertEquals(3, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "baz", null, true);

        select.setItems("1", "2", "3", "4");

        Assertions.assertEquals(4, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "1", null, true);
        validateItem(1, "2", null, true);
        validateItem(2, "3", null, true);
        validateItem(3, "4", null, true);
    }

    @Test
    void setDataProvider_dataProviderRefreshes_itemsUpdated() {
        Select<Bean> select = new Select<>();
        Bean beanToUpdate = new Bean("foo");
        List<Bean> beans = Arrays.asList(beanToUpdate, new Bean("bar"),
                new Bean("baz"));
        selectSupplier = () -> select;

        ListDataProvider<Bean> dataProvider = new ListDataProvider<Bean>(
                beans) {
            @Override
            public Object getId(Bean item) {
                return item.id;
            }
        };

        select.setItems(dataProvider);

        Assertions.assertEquals(3, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);

        beanToUpdate.setProperty("UPDATED");
        dataProvider.refreshItem(beanToUpdate);

        Assertions.assertEquals(3, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "UPDATED!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);
    }

    @Test
    void itemEnabledProvider_updatedEnabledState() {
        select.setItems("1", "2", "3");
        select.setItemEnabledProvider(item -> item.contains("1"));

        validateItem(0, "1", null, true);
        validateItem(1, "2", null, false);
        validateItem(2, "3", null, false);

        select.setItems("0", "1", "2", "11");

        validateItem(0, "0", null, false);
        validateItem(1, "1", null, true);
        validateItem(2, "2", null, false);
        validateItem(3, "11", null, true);

        select.setItemEnabledProvider(item -> !item.contains("1"));

        validateItem(0, "0", null, true);
        validateItem(1, "1", null, false);
        validateItem(2, "2", null, true);
        validateItem(3, "11", null, false);
    }

    @Test
    void itemLabelGenerator_updatesLabelProperty() {
        select.setItems("1", "2", "3");
        select.setItemLabelGenerator(item -> item + " LABEL");

        validateItem(0, "1 LABEL", "1 LABEL", true);
        validateItem(1, "2 LABEL", "2 LABEL", true);
        validateItem(2, "3 LABEL", "3 LABEL", true);

        select.setItems("one", "two");

        validateItem(0, "one LABEL", "one LABEL", true);
        validateItem(1, "two LABEL", "two LABEL", true);

        select.setItemLabelGenerator(item -> "LABEL " + item);

        validateItem(0, "LABEL one", "LABEL one", true);
        validateItem(1, "LABEL two", "LABEL two", true);
    }

    @Test
    void renderer_defaultRendererUsesToString() {
        Select<Bean> select = new Select<>();
        select.setItems(new Bean("foo"), new Bean("bar"), new Bean("baz"));
        selectSupplier = () -> select;

        validateItem(0, "foo!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);
    }

    @Test
    void renderer_setRendererShorthandForString() {
        Select<Bean> select = new Select<>();
        select.setItems(new Bean("foo"), new Bean("bar"), new Bean("baz"));
        selectSupplier = () -> select;
        select.setTextRenderer(bean -> "!" + bean.getProperty());

        Assertions.assertEquals(
                "<vaadin-select-item value=\"1\"><span>!foo</span></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assertions.assertEquals(
                "<vaadin-select-item value=\"2\"><span>!bar</span></vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
        Assertions.assertEquals(
                "<vaadin-select-item value=\"3\"><span>!baz</span></vaadin-select-item>",
                getListBoxChild(2).getOuterHTML());
    }

    @Test
    void renderer_customRendererUsed() {
        select.setItems("foo", "bar", "baz");
        select.setRenderer(new ComponentRenderer<>(
                (SerializableFunction<String, Span>) Span::new));

        Assertions.assertEquals(
                "<vaadin-select-item value=\"1\"><span>foo</span></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assertions.assertEquals(
                "<vaadin-select-item value=\"2\"><span>bar</span></vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
        Assertions.assertEquals(
                "<vaadin-select-item value=\"3\"><span>baz</span></vaadin-select-item>",
                getListBoxChild(2).getOuterHTML());

        select.setItems("1", "2");
        Assertions.assertEquals(
                "<vaadin-select-item value=\"4\"><span>1</span></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assertions.assertEquals(
                "<vaadin-select-item value=\"5\"><span>2</span></vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
    }

    @Test
    void renderer_itemLabelGenerator_prefersRenderer() {
        select.setItems("foo");
        select.setRenderer(new ComponentRenderer<>(
                (SerializableFunction<String, Span>) Span::new));
        select.setItemLabelGenerator(item -> "bar");
        Assertions.assertEquals(
                "<vaadin-select-item value=\"1\" label=\"bar\"><span>foo</span></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
    }

    @Test
    void emptySelectionAllowed_emptySelectionCaptionChanged() {
        select.setItems("foo", "bar");

        validateItem(0, "foo", null, true);

        select.setEmptySelectionAllowed(true);

        // getOuterHTML jsoup interprets the property value with "" as value as
        // a boolean
        Assertions.assertEquals(
                "<vaadin-select-item value></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());

        validateItem(0, "", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "bar", null, true);

        select.setEmptySelectionAllowed(false);

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.setEmptySelectionAllowed(true);
        select.setEmptySelectionCaption("EMPTY");

        Assertions.assertEquals(
                "<vaadin-select-item value>EMPTY</vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());

        validateItem(0, "EMPTY", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "bar", null, true);

        select.setEmptySelectionCaption("changed");
        validateItem(0, "changed", null, true);

        select.setItems("1", "2");
        validateItem(0, "changed", null, true);
        validateItem(1, "1", null, true);
        validateItem(2, "2", null, true);
    }

    @Test
    void emptySelectionItem_itemEnabledProviderCanDisableIt() {
        select.setItems("foo", "bar");
        select.setEmptySelectionAllowed(true);
        select.setItemEnabledProvider(Objects::nonNull);

        validateItem(0, "", null, false);
        validateItem(1, "foo", null, true);
        validateItem(2, "bar", null, true);
    }

    @Test
    void emptySelectionItem_itemLabelGeneratorCanCustomizeIt() {
        select.setItems("foo", "bar");
        select.setEmptySelectionAllowed(true);
        select.setItemLabelGenerator(
                string -> string == null ? "FOOBAR" : string + "!");

        validateItem(0, "", "FOOBAR", true);
        validateItem(1, "foo!", "foo!", true);
        validateItem(2, "bar!", "bar!", true);
    }

    @Test
    void valueProperty_convertsToPresentationAndModel() {
        Select<Bean> select = new Select<>();
        Bean foo = new Bean("foo");
        Bean bar = new Bean("bar");
        select.setItems(foo, bar, new Bean("baz"));
        selectSupplier = () -> select;

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select.addValueChangeListener(capture::set);

        Assertions.assertNull(select.getValue(), "Wrong value");

        select.setValue(bar);
        Assertions.assertEquals(bar, select.getValue(), "Wrong value");
        Assertions.assertEquals(bar, capture.get().getValue(),
                "No value change");
        Assertions.assertNull(capture.get().getOldValue(), "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client based");
        Assertions.assertEquals("2", select.getElement().getProperty("value"),
                "Element has incorrect value");

        select.getElement().setProperty("value", "1");
        Assertions.assertEquals(foo, select.getValue(), "Wrong value");
        Assertions.assertEquals(foo, capture.get().getValue(),
                "No value change");
        Assertions.assertEquals(bar, capture.get().getOldValue(),
                "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client based");
        Assertions.assertEquals("1", select.getElement().getProperty("value"),
                "Element has incorrect value");

        select.setValue(null);
        Assertions.assertNull(select.getValue(), "Wrong value");
        Assertions.assertNull(capture.get().getValue(), "No value change");
        Assertions.assertEquals(foo, capture.get().getOldValue(),
                "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client based");
        Assertions.assertEquals("", select.getElement().getProperty("value"),
                "Element has incorrect value");
    }

    @Test
    void setDisabledItemAsValue_valueChangeRejected() {
        select.setItems("foo", "bar");
        select.setItemEnabledProvider(item -> item.equals("foo"));

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select.addValueChangeListener(capture::set);

        select.setValue("foo");

        Assertions.assertEquals("foo", select.getValue(), "Wrong value");
        Assertions.assertEquals("foo", capture.get().getValue(),
                "No value change");
        Assertions.assertNull(capture.get().getOldValue(), "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client based");
        Assertions.assertEquals("1", select.getElement().getProperty("value"),
                "Element has incorrect value");

        // value change will be rejected due to disabled item, even though the
        // property is changed
        select.getElement().setProperty("value", "2");

        Assertions.assertEquals("foo", select.getValue(), "Wrong value");
        Assertions.assertEquals("foo", capture.get().getValue(),
                "No value change");
        Assertions.assertNull(capture.get().getOldValue(), "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client based");

        Assertions.assertEquals("2", select.getElement().getProperty("value"),
                "Element has incorrect value");
    }

    @Test
    void disable_makesElementAndItemsDisabled() {
        select.setItems("foo", "bar");
        select.setEnabled(false);

        Assertions.assertTrue(
                select.getElement().getProperty("disabled", false),
                "disabled property not set");

        validateItem(0, "foo", null, false);
        validateItem(1, "bar", null, false);

        select.setItemEnabledProvider(item -> item.equals("foo"));

        validateItem(0, "foo", null, false);
        validateItem(1, "bar", null, false);

        select.setEnabled(true);

        Assertions.assertFalse(
                select.getElement().getProperty("disabled", false),
                "disabled property not removed");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, false);
    }

    @Test
    void clientSideValueUpdate_componentIsReadOnly_preventsValueUpdate() {
        // need to allow updating value via internal method to mock client
        // update
        final Consumer<String>[] clientSideValueUpdater = new Consumer[1];
        select = new Select<String>() {
            {
                clientSideValueUpdater[0] = value -> this.setModelValue(value,
                        true);
            }
        };
        select.setItems("foo", "bar");
        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select.addValueChangeListener(capture::set);
        select.setReadOnly(true);

        Assertions.assertTrue(
                select.getElement().getProperty("readonly", false),
                "readonly property not set");

        clientSideValueUpdater[0].accept("foo");

        Assertions.assertNull(capture.get(),
                "No event should be fired when in readonly");
        Assertions.assertNull(select.getValue(),
                "Value changed when in read only");

        // value should be changeable from server side even in readonly mode
        select.setValue("bar");

        Assertions.assertEquals("bar", select.getValue(), "Wrong value");
        Assertions.assertEquals("bar", capture.get().getValue(),
                "No value change");
        Assertions.assertNull(capture.get().getOldValue(), "Wrong old value");
        Assertions.assertFalse(capture.get().isFromClient(),
                "Value change should not be client basedd");
        Assertions.assertEquals("2", select.getElement().getProperty("value"),
                "Element has incorrect value");

        select.setReadOnly(false);

        Assertions.assertFalse(
                select.getElement().getProperty("readonly", false),
                "readonly property not removed");

        clientSideValueUpdater[0].accept("foo");

        Assertions.assertEquals("foo", select.getValue(), "Wrong value");
        Assertions.assertEquals("foo", capture.get().getValue(),
                "No value change");
        Assertions.assertEquals("bar", capture.get().getOldValue(),
                "Wrong old value");
        Assertions.assertTrue(capture.get().isFromClient(),
                "Value change should be client based");
        // element property value is not updated with out mock updater so it has
        // stayed the same
    }

    @Test
    void addRemoveComponents_componentsIntendedForListBox() {
        select.setItems("foo", "bar");

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(0, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        Span span = new Span("span 1");
        select.add(span);

        Assertions.assertEquals(3, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(1, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "span 1", null, true);

        Assertions.assertEquals("<span>span 1</span>",
                getListBoxChild(2).getOuterHTML());

        select.remove(span);

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(0, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.add(new Span("another span"), new Span("yet another span"));
        select.addComponentAsFirst(new Span("first"));
        select.addComponentAtIndex(2, new Span("index 2"));

        Assertions.assertEquals(6, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(4, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "first", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "index 2", null, true);
        validateItem(3, "bar", null, true);
        validateItem(4, "another span", null, true);
        validateItem(5, "yet another span", null, true);

        select.removeAll();

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.add(span);
    }

    @Test
    void addComponentAfterBeforeItem_goesIntoCorrectPlace() {
        select.setItems("foo", "bar");
        select.addComponents("foo", new Span("after foo"));
        select.prependComponents("foo", new Span("before foo"));

        Assertions.assertEquals(4, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(2, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "before foo", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "after foo", null, true);
        validateItem(3, "bar", null, true);

        select.removeAll();

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(0, select.getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
    }

    @Test
    void setPrefixComponent_prefixComponentGoesToSelectChildren() {
        select.setItems("foo", "bar");

        select.setPrefixComponent(new Span("prefix1"));

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(1, select.getChildren().count(),
                "Invalid number of items");

        Assertions.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());

        Span span = new Span("prefix2");
        span.getElement().setAttribute("slot", "prefix");
        select.add(span);

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(2, select.getChildren().count(),
                "Invalid number of items");

        Assertions.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());
        Assertions.assertEquals("<span slot=\"prefix\">prefix2</span>",
                select.getChildren().collect(Collectors.toList()).get(1)
                        .getElement().getOuterHTML());

        select.remove(span);

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(1, select.getChildren().count(),
                "Invalid number of items");

        Assertions.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());

        select.removeAll();

        Assertions.assertEquals(2, getListBox().getChildren().count(),
                "Invalid number of items");
        Assertions.assertEquals(0, select.getChildren().count(),
                "Invalid number of items");
    }

    @Test
    void dataViewForFaultyDataProvider_throwsException() {
        Select<String> select = new Select<>();
        select.setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        select.setItems(dataProvider);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class, () -> select.getListDataView());
        Assertions.assertTrue(exception.getMessage()
                .contains("SelectListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'"));
    }

    @Test
    void elementHasValue_wrapIntoField_doesNotThrow() {
        Element element = new Element("vaadin-select");
        element.setProperty("value", "foo");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(Select.class))
                .thenAnswer(invocation -> new Select());
        Component.from(element, Select.class);
    }

    @Test
    void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> listDataView = select.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        select.setValue(new CustomItem(1L));

        Assertions.assertNotNull(select.getValue());
        Assertions.assertEquals("First", select.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        select.setValue(new CustomItem(2L));

        Assertions.assertNotNull(select.getValue());
        Assertions.assertEquals(Long.valueOf(2L), select.getValue().getId());
    }

    @Test
    void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> listDataView = select.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        select.setValue(new CustomItem(1L));

        Assertions.assertNotNull(select.getValue());
        Assertions.assertEquals("First", select.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        select.setValue(new CustomItem(3L, "Second"));

        Assertions.assertNotNull(select.getValue());
        Assertions.assertEquals(Long.valueOf(3L), select.getValue().getId());
    }

    @Test
    void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> listDataView = select.setItems(items);

        select.setValue(new CustomItem(null, "Second"));

        Assertions.assertNotNull(select.getValue());
        Assertions.assertEquals(Long.valueOf(2L), select.getValue().getId());
    }

    @Test
    void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> listDataView = select.setItems(items);
        // Setting the following Identifier Provider makes the component
        // independent from the CustomItem's equals method implementation:
        listDataView.setIdentifierProvider(CustomItem::getId);

        select.setValue(new CustomItem(null, "First"));
        Assertions.assertNull(select.getValue().getId());
    }

    @Test
    void setItems_createsLabelValueEventAndItems() {
        Assertions.assertEquals(0, getListBox().getChildren().count(),
                "Invalid number of items");

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select = new Select<>("label", capture::set, "foo", "bar", "baz");

        Assertions.assertEquals(3, getListBox().getChildren().count(),
                "Invalid number of items");

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "baz", null, true);

        Assertions.assertEquals("label",
                select.getElement().getProperty("label"),
                "Invalid label for select ");
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(select instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(HasAriaLabel.class.isAssignableFrom(Select.class),
                "Select should support aria-label and aria-labelledby");
    }

    @Test
    void setAriaLabel() {
        Select<String> select = new Select<>();

        select.setAriaLabel("aria-label");
        Assertions.assertTrue(select.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", select.getAriaLabel().get());

        select.setAriaLabel(null);
        Assertions.assertTrue(select.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        Select<String> select = new Select<>();

        select.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(select.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                select.getAriaLabelledBy().get());

        select.setAriaLabelledBy(null);
        Assertions.assertTrue(select.getAriaLabelledBy().isEmpty());
    }

    @Test
    void setNoVerticalOverlap() {
        Select<String> select = new Select<>();

        Assertions.assertFalse(select.isNoVerticalOverlap());
        select.setNoVerticalOverlap(true);
        Assertions.assertTrue(select.isNoVerticalOverlap());
        select.setNoVerticalOverlap(false);
        Assertions.assertFalse(select.isNoVerticalOverlap());
    }

    @Test
    void setOverlayWidth() {
        Select<String> select = new Select<>();

        select.setOverlayWidth(null);
        Assertions.assertNull(
                select.getStyle().get("--vaadin-select-overlay-width"));
        select.setOverlayWidth("30em");
        Assertions.assertEquals("30em",
                select.getStyle().get("--vaadin-select-overlay-width"));
        select.setOverlayWidth(-1, Unit.EM);
        Assertions.assertNull(
                select.getStyle().get("--vaadin-select-overlay-width"));
        select.setOverlayWidth(100, Unit.PIXELS);
        Assertions.assertEquals("100.0px",
                select.getStyle().get("--vaadin-select-overlay-width"));
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        var listenerInvokedCount = new AtomicInteger(0);
        select.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        select.setOpened(true);
        select.setOpened(false);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void unregisterInvalidChangeListenerOnEvent() {
        var listenerInvokedCount = new AtomicInteger(0);
        select.addInvalidChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        select.setInvalid(true);
        select.setInvalid(false);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void implementsInputField() {
        Assertions.assertTrue(
                select instanceof InputField<AbstractField.ComponentValueChangeEvent<Select<String>, String>, String>);
    }

    @Test
    void getItemPosition_shouldReturnItemIndexIfItemExists() {
        select.setItems("foo", "bar", "buzz");
        Assertions.assertEquals(0, select.getItemPosition("foo"));
        Assertions.assertEquals(1, select.getItemPosition("bar"));
        Assertions.assertEquals(2, select.getItemPosition("buzz"));
        Assertions.assertEquals(-1, select.getItemPosition("does not exist"));
    }

    @Test
    void refreshItem_selectFromClient_valueContainsUpdatedItem() {
        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> dataView = select.setItems(
                new CustomItem(1L, "foo"), new CustomItem(2L, "bar"),
                new CustomItem(3L, "baz"));
        dataView.setIdentifierProvider(CustomItem::getId);
        selectSupplier = () -> select;

        CustomItem updatedItem = new CustomItem(2L, "updated");
        dataView.refreshItem(updatedItem);

        AtomicReference<CustomItem> selectedItem = new AtomicReference<>();
        select.addValueChangeListener(e -> selectedItem.set(e.getValue()));

        // Simulate selecting an item from the client side via key
        String itemKey = getListBoxChild(1).getProperty("value");
        select.getElement().setProperty("value", itemKey);

        Assertions.assertEquals("updated", selectedItem.get().name);
        Assertions.assertEquals("updated", select.getValue().name);
    }

    private void validateItem(int index, String textContent, String label,
            boolean enabled) {
        Element item = getListBoxChild(index);
        Assertions.assertEquals(textContent, item.getText(),
                "Invalid text content for item " + index);
        Assertions.assertEquals(label, item.getAttribute("label"),
                "Invalid label for item " + index);
        Assertions.assertEquals(enabled, item.isEnabled(),
                "Invalid enabled state for item " + index);
    }

    private Element getListBoxChild(int index) {
        return getListBox().getChild(index);
    }

    private Element getListBox() {
        return selectSupplier.get().getElement().getChild(0);
    }

    private static class Bean {
        private String property;
        private final String id;

        private Bean(String property) {
            this.property = property;
            this.id = property;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        @Override
        public String toString() {
            return property + "!";
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
