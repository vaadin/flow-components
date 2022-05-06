package com.vaadin.flow.component.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.data.SelectListDataView;
import com.vaadin.flow.component.select.generated.GeneratedVaadinSelect;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class SelectTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Select<String> select;
    private Supplier<Select> selectSupplier = () -> select;

    @Before
    public void setUp() {
        select = new Select<>();
    }

    private static class TestSelect
            extends GeneratedVaadinSelect<TestSelect, String> {

        TestSelect() {
            super("", null, String.class, (select, value) -> value,
                    (select, value) -> value, true);
        }
    }

    @Test
    public void defaults() {
        Assert.assertNull("Default value should be null", select.getValue());
        Assert.assertNull("Empty value should be null", select.getEmptyValue());

        Assert.assertEquals("Select should not have any children by default", 0,
                select.getChildren().count());

        Assert.assertFalse("Empty selection is not allowed by default",
                select.isEmptySelectionAllowed());
        Assert.assertEquals(
                "Empty selection caption is empty string by default", "",
                select.getEmptySelectionCaption());

        Assert.assertNull("Default item enabled generator null",
                select.getItemEnabledProvider());
        Assert.assertNull("By default item label generator should not be set",
                select.getItemLabelGenerator());
        Assert.assertNull("Default renderer is null", select.getItemRenderer());

        Assert.assertNull("Default placeholder is null",
                select.getPlaceholder());
        Assert.assertNull("Default label is null", select.getLabel());
        Assert.assertFalse("No autofocus by default", select.isAutofocus());
        Assert.assertFalse("Component is in valid state by default",
                select.isInvalid());
        Assert.assertFalse("No required indicator by default",
                select.isRequiredIndicatorVisible());
        Assert.assertNull("Default error message is null",
                select.getErrorMessage());

        Assert.assertTrue("Component is enabled by default",
                select.isEnabled());
        Assert.assertFalse("Component is not readonly by default",
                select.isReadOnly());
    }

    @Test
    public void basicProperties() {
        select.setPlaceholder("placeholder");
        select.setLabel("label");
        select.setInvalid(true);
        select.setAutofocus(true);
        select.setRequiredIndicatorVisible(true);
        select.setErrorMessage("errorMessage");
        select.setReadOnly(true);
        select.setEnabled(false);

        Assert.assertEquals("Wrong placeholder", "placeholder",
                select.getPlaceholder());
        Assert.assertEquals("Wrong label", "label", select.getLabel());
        Assert.assertTrue("Autofocus not set", select.isAutofocus());
        Assert.assertTrue("Invalid state not set", select.isInvalid());
        Assert.assertTrue("Required indicator not set",
                select.isRequiredIndicatorVisible());
        Assert.assertEquals("Error message is not set", "errorMessage",
                select.getErrorMessage());
        Assert.assertTrue("ReadOnly not set", select.isReadOnly());
        Assert.assertFalse("Disabled not set", select.isEnabled());
    }

    @Test
    public void templateWarningSuppressed() {
        Assert.assertTrue("Template warning is not suppressed",
                select.getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void defaultValue_clearSetsToNull() {
        select.setItems("foo", "bar");
        select.setValue("foo");

        Assert.assertEquals("foo", select.getValue());

        select.clear();
        Assert.assertNull(select.getValue());
    }

    @Test
    public void setItems_createsItems() {
        Assert.assertEquals("Invalid number of items", 0,
                getListBox().getChildren().count());

        select = new Select<>("foo", "bar", "baz");

        Assert.assertEquals("Invalid number of items", 3,
                getListBox().getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "baz", null, true);

        select.setItems("1", "2", "3", "4");

        Assert.assertEquals("Invalid number of items", 4,
                getListBox().getChildren().count());

        validateItem(0, "1", null, true);
        validateItem(1, "2", null, true);
        validateItem(2, "3", null, true);
        validateItem(3, "4", null, true);
    }

    @Test
    public void setDataProvider_dataProviderRefreshes_itemsUpdated() {
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

        select.setDataProvider(dataProvider);

        Assert.assertEquals("Invalid number of items", 3,
                getListBox().getChildren().count());

        validateItem(0, "foo!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);

        beanToUpdate.setProperty("UPDATED");
        dataProvider.refreshItem(beanToUpdate);

        Assert.assertEquals("Invalid number of items", 3,
                getListBox().getChildren().count());

        validateItem(0, "UPDATED!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);
    }

    @Test
    public void itemEnabledProvider_updatedEnabledState() {
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
    public void itemLabelGenerator_updatesLabelProperty() {
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
    public void renderer_defaultRendererUsesToString() {
        Select<Bean> select = new Select<>();
        select.setItems(new Bean("foo"), new Bean("bar"), new Bean("baz"));
        selectSupplier = () -> select;

        validateItem(0, "foo!", null, true);
        validateItem(1, "bar!", null, true);
        validateItem(2, "baz!", null, true);
    }

    @Test
    public void renderer_setRendererShorthandForString() {
        Select<Bean> select = new Select<>();
        select.setItems(new Bean("foo"), new Bean("bar"), new Bean("baz"));
        selectSupplier = () -> select;
        select.setTextRenderer(bean -> "!" + bean.getProperty());

        Assert.assertEquals(
                "<vaadin-select-item value=\"1\">\n <span>!foo</span>\n</vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assert.assertEquals(
                "<vaadin-select-item value=\"2\">\n <span>!bar</span>\n</vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
        Assert.assertEquals(
                "<vaadin-select-item value=\"3\">\n <span>!baz</span>\n</vaadin-select-item>",
                getListBoxChild(2).getOuterHTML());
    }

    @Test
    public void renderer_customRendererUsed() {
        select.setItems("foo", "bar", "baz");
        select.setRenderer(new ComponentRenderer<>(
                (SerializableFunction<String, Span>) Span::new));

        Assert.assertEquals(
                "<vaadin-select-item value=\"1\">\n <span>foo</span>\n</vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assert.assertEquals(
                "<vaadin-select-item value=\"2\">\n <span>bar</span>\n</vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
        Assert.assertEquals(
                "<vaadin-select-item value=\"3\">\n <span>baz</span>\n</vaadin-select-item>",
                getListBoxChild(2).getOuterHTML());

        select.setItems("1", "2");
        Assert.assertEquals(
                "<vaadin-select-item value=\"4\">\n <span>1</span>\n</vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
        Assert.assertEquals(
                "<vaadin-select-item value=\"5\">\n <span>2</span>\n</vaadin-select-item>",
                getListBoxChild(1).getOuterHTML());
    }

    @Test
    public void renderer_itemLabelGenerator_prefersRenderer() {
        select.setItems("foo");
        select.setRenderer(new ComponentRenderer<>(
                (SerializableFunction<String, Span>) Span::new));
        select.setItemLabelGenerator(item -> "bar");
        Assert.assertEquals(
                "<vaadin-select-item value=\"1\" label=\"bar\">\n <span>foo</span>\n</vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());
    }

    @Test
    public void emptySelectionAllowed_emptySelectionCaptionChanged() {
        select.setItems("foo", "bar");

        validateItem(0, "foo", null, true);

        select.setEmptySelectionAllowed(true);

        // getOuterHTML jsoup interprets the property value with "" as value as
        // a boolean
        Assert.assertEquals("<vaadin-select-item value></vaadin-select-item>",
                getListBoxChild(0).getOuterHTML());

        validateItem(0, "", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "bar", null, true);

        select.setEmptySelectionAllowed(false);

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.setEmptySelectionAllowed(true);
        select.setEmptySelectionCaption("EMPTY");

        Assert.assertEquals(
                "<vaadin-select-item value>\n EMPTY\n</vaadin-select-item>",
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
    public void emptySelectionItem_itemEnabledProviderCanDisableIt() {
        select.setItems("foo", "bar");
        select.setEmptySelectionAllowed(true);
        select.setItemEnabledProvider(Objects::nonNull);

        validateItem(0, "", null, false);
        validateItem(1, "foo", null, true);
        validateItem(2, "bar", null, true);
    }

    @Test
    public void emptySelectionItem_itemLabelGeneratorCanCustomizeIt() {
        select.setItems("foo", "bar");
        select.setEmptySelectionAllowed(true);
        select.setItemLabelGenerator(
                string -> string == null ? "FOOBAR" : string + "!");

        validateItem(0, "", "FOOBAR", true);
        validateItem(1, "foo!", "foo!", true);
        validateItem(2, "bar!", "bar!", true);
    }

    @Test
    public void valueProperty_convertsToPresentationAndModel() {
        Select<Bean> select = new Select<>();
        Bean foo = new Bean("foo");
        Bean bar = new Bean("bar");
        select.setItems(foo, bar, new Bean("baz"));
        selectSupplier = () -> select;

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select.addValueChangeListener(capture::set);

        Assert.assertNull("Wrong value", select.getValue());

        select.setValue(bar);
        Assert.assertEquals("Wrong value", bar, select.getValue());
        Assert.assertEquals("No value change", bar, capture.get().getValue());
        Assert.assertNull("Wrong old value", capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client based",
                capture.get().isFromClient());
        Assert.assertEquals("Element has incorrect value", "2",
                select.getElement().getProperty("value"));

        select.getElement().setProperty("value", "1");
        Assert.assertEquals("Wrong value", foo, select.getValue());
        Assert.assertEquals("No value change", foo, capture.get().getValue());
        Assert.assertEquals("Wrong old value", bar,
                capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client based",
                capture.get().isFromClient());
        Assert.assertEquals("Element has incorrect value", "1",
                select.getElement().getProperty("value"));

        select.setValue(null);
        Assert.assertNull("Wrong value", select.getValue());
        Assert.assertNull("No value change", capture.get().getValue());
        Assert.assertEquals("Wrong old value", foo,
                capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client based",
                capture.get().isFromClient());
        Assert.assertEquals("Element has incorrect value", "",
                select.getElement().getProperty("value"));
    }

    @Test
    public void setDisabledItemAsValue_valueChangeRejected() {
        select.setItems("foo", "bar");
        select.setItemEnabledProvider(item -> item.equals("foo"));

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select.addValueChangeListener(capture::set);

        select.setValue("foo");

        Assert.assertEquals("Wrong value", "foo", select.getValue());
        Assert.assertEquals("No value change", "foo", capture.get().getValue());
        Assert.assertNull("Wrong old value", capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client based",
                capture.get().isFromClient());
        Assert.assertEquals("Element has incorrect value", "1",
                select.getElement().getProperty("value"));

        // value change will be rejected due to disabled item, even though the
        // property is changed
        select.getElement().setProperty("value", "2");

        Assert.assertEquals("Wrong value", "foo", select.getValue());
        Assert.assertEquals("No value change", "foo", capture.get().getValue());
        Assert.assertNull("Wrong old value", capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client based",
                capture.get().isFromClient());

        Assert.assertEquals("Element has incorrect value", "2",
                select.getElement().getProperty("value"));
    }

    @Test
    public void disable_makesElementAndItemsDisabled() {
        select.setItems("foo", "bar");
        select.setEnabled(false);

        Assert.assertTrue("disabled property not set",
                select.getElement().getProperty("disabled", false));

        validateItem(0, "foo", null, false);
        validateItem(1, "bar", null, false);

        select.setItemEnabledProvider(item -> item.equals("foo"));

        validateItem(0, "foo", null, false);
        validateItem(1, "bar", null, false);

        select.setEnabled(true);

        Assert.assertFalse("disabled property not removed",
                select.getElement().getProperty("disabled", false));

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, false);
    }

    @Test
    public void clientSideValueUpdate_componentIsReadOnly_preventsValueUpdate() {
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

        Assert.assertTrue("readonly property not set",
                select.getElement().getProperty("readonly", false));

        clientSideValueUpdater[0].accept("foo");

        Assert.assertNull("No event should be fired when in readonly",
                capture.get());
        Assert.assertNull("Value changed when in read only", select.getValue());

        // value should be changeable from server side even in readonly mode
        select.setValue("bar");

        Assert.assertEquals("Wrong value", "bar", select.getValue());
        Assert.assertEquals("No value change", "bar", capture.get().getValue());
        Assert.assertNull("Wrong old value", capture.get().getOldValue());
        Assert.assertFalse("Value change should not be client basedd",
                capture.get().isFromClient());
        Assert.assertEquals("Element has incorrect value", "2",
                select.getElement().getProperty("value"));

        select.setReadOnly(false);

        Assert.assertFalse("readonly property not removed",
                select.getElement().getProperty("readonly", false));

        clientSideValueUpdater[0].accept("foo");

        Assert.assertEquals("Wrong value", "foo", select.getValue());
        Assert.assertEquals("No value change", "foo", capture.get().getValue());
        Assert.assertEquals("Wrong old value", "bar",
                capture.get().getOldValue());
        Assert.assertTrue("Value change should be client based",
                capture.get().isFromClient());
        // element property value is not updated with out mock updater so it has
        // stayed the same
    }

    @Test
    public void addRemoveComponents_componentsIntendedForListBox() {
        select.setItems("foo", "bar");

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 0,
                select.getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        Span span = new Span("span 1");
        select.add(span);

        Assert.assertEquals("Invalid number of items", 3,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 1,
                select.getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "span 1", null, true);

        Assert.assertEquals("<span>span 1</span>",
                getListBoxChild(2).getOuterHTML());

        select.remove(span);

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 0,
                select.getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.add(new Span("another span"), new Span("yet another span"));
        select.addComponentAsFirst(new Span("first"));
        select.addComponentAtIndex(2, new Span("index 2"));

        Assert.assertEquals("Invalid number of items", 6,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 4,
                select.getChildren().count());

        validateItem(0, "first", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "index 2", null, true);
        validateItem(3, "bar", null, true);
        validateItem(4, "another span", null, true);
        validateItem(5, "yet another span", null, true);

        select.removeAll();

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);

        select.add(span);
    }

    @Test
    public void addComponentAfterBeforeItem_goesIntoCorrectPlace() {
        select.setItems("foo", "bar");
        select.addComponents("foo", new Span("after foo"));
        select.prependComponents("foo", new Span("before foo"));

        Assert.assertEquals("Invalid number of items", 4,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 2,
                select.getChildren().count());

        validateItem(0, "before foo", null, true);
        validateItem(1, "foo", null, true);
        validateItem(2, "after foo", null, true);
        validateItem(3, "bar", null, true);

        select.removeAll();

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 0,
                select.getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
    }

    @Test
    public void addToPrefix_prefixComponentsGoToSelectChildren() {
        select.setItems("foo", "bar");

        select.addToPrefix(new Span("prefix1"));

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 1,
                select.getChildren().count());

        Assert.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());

        Span span = new Span("prefix2");
        span.getElement().setAttribute("slot", "prefix");
        select.add(span);

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 2,
                select.getChildren().count());

        Assert.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());
        Assert.assertEquals("<span slot=\"prefix\">prefix2</span>",
                select.getChildren().collect(Collectors.toList()).get(1)
                        .getElement().getOuterHTML());

        select.remove(span);

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 1,
                select.getChildren().count());

        Assert.assertEquals("<span slot=\"prefix\">prefix1</span>", select
                .getChildren().findFirst().get().getElement().getOuterHTML());

        select.removeAll();

        Assert.assertEquals("Invalid number of items", 2,
                getListBox().getChildren().count());
        Assert.assertEquals("Invalid number of items", 0,
                select.getChildren().count());
    }

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage(
                "SelectListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'");

        Select<String> select = new Select<>();
        select.setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Stream.of("one"), query -> 1);

        select.setDataProvider(dataProvider);

        select.getListDataView();
    }

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-select");
        element.setProperty("value", "foo");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TestSelect.class))
                .thenAnswer(invocation -> new TestSelect());
        TestSelect field = Component.from(element, TestSelect.class);
        Assert.assertEquals("foo", field.getElement().getPropertyRaw("value"));
    }

    @Test
    public void setIdentifierProvider_setItemWithIdentifierOnly_shouldSelectCorrectItem() {
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

        Assert.assertNotNull(select.getValue());
        Assert.assertEquals("First", select.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item not with the reference of existing item, but instead
        // with just the Id:
        select.setValue(new CustomItem(2L));

        Assert.assertNotNull(select.getValue());
        Assert.assertEquals(Long.valueOf(2L), select.getValue().getId());
    }

    @Test
    public void setIdentifierProvider_setItemWithIdAndWrongName_shouldSelectCorrectItemBasedOnIdNotEquals() {
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

        Assert.assertNotNull(select.getValue());
        Assert.assertEquals("First", select.getValue().getName());

        // Make the names similar to the name of not selected one to mess
        // with the <equals> implementation in CustomItem:
        first.setName("Second");
        listDataView.refreshItem(first);
        third.setName("Second");
        listDataView.refreshItem(third);

        // Select the item with an Id and the name that can be wrongly equals to
        // another items, should verify that <equals> method is not in use:
        select.setValue(new CustomItem(3L, "Second"));

        Assert.assertNotNull(select.getValue());
        Assert.assertEquals(Long.valueOf(3L), select.getValue().getId());
    }

    @Test
    public void withoutSettingIdentifierProvider_setItemWithNullId_shouldSelectCorrectItemBasedOnEquals() {
        CustomItem first = new CustomItem(1L, "First");
        CustomItem second = new CustomItem(2L, "Second");
        CustomItem third = new CustomItem(3L, "Third");
        List<CustomItem> items = new ArrayList<>(
                Arrays.asList(first, second, third));

        Select<CustomItem> select = new Select<>();
        SelectListDataView<CustomItem> listDataView = select.setItems(items);

        select.setValue(new CustomItem(null, "Second"));

        Assert.assertNotNull(select.getValue());
        Assert.assertEquals(Long.valueOf(2L), select.getValue().getId());
    }

    @Test
    public void setIdentifierProviderOnId_setItemWithNullId_shouldFailToSelectExistingItemById() {
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
        Assert.assertNull(select.getValue().getId());
    }

    @Test
    public void setItems_createsLabelValueEventAndItems() {
        Assert.assertEquals("Invalid number of items", 0,
                getListBox().getChildren().count());

        AtomicReference<HasValue.ValueChangeEvent> capture = new AtomicReference<>();
        select = new Select<>("label", capture::set, "foo", "bar", "baz");

        Assert.assertEquals("Invalid number of items", 3,
                getListBox().getChildren().count());

        validateItem(0, "foo", null, true);
        validateItem(1, "bar", null, true);
        validateItem(2, "baz", null, true);

        Assert.assertEquals("Invalid label for select ", "label",
                select.getElement().getProperty("label"));
    }

    private void validateItem(int index, String textContent, String label,
            boolean enabled) {
        Element item = getListBoxChild(index);
        Assert.assertEquals("Invalid text content for item " + index,
                textContent, item.getText());
        Assert.assertEquals("Invalid label for item " + index, label,
                item.getAttribute("label"));
        Assert.assertEquals("Invalid enabled state for item " + index, enabled,
                item.isEnabled());
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
