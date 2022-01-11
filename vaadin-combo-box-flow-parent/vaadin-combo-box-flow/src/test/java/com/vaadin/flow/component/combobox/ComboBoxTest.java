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
package com.vaadin.flow.component.combobox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.DataProviderListenersTest;

import elemental.json.Json;
import static org.junit.Assert.assertEquals;

public class ComboBoxTest {

    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static class TestComboBox extends ComboBox<String> {

        private List<String> items;

        @Override
        public void setDataProvider(ListDataProvider<String> dataProvider) {
            super.setDataProvider(dataProvider);
            items = new ArrayList<>(dataProvider.getItems());
        }

    }

    private enum Category {
        CATEGORY_1, CATEGORY_2, CATEGORY_3;
    }

    private static class Bean {
        Category category;

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }
    }

    private class ComboBoxWithInitialValue
            extends GeneratedVaadinComboBox<ComboBoxWithInitialValue, String> {
        ComboBoxWithInitialValue() {
            super("", null, String.class, (combo, value) -> value,
                    (combo, value) -> value, true);
        }
    }

    @Test
    public void templateWarningSuppressed() {
        ComboBox<Object> comboBox = new ComboBox<>();

        Assert.assertTrue("Template warning is not suppressed", comboBox
                .getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void setItems_jsonItemsAreSet() {
        TestComboBox comboBox = new TestComboBox();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertEquals(2, comboBox.items.size());
        assertItem(comboBox, 0, "foo");
        assertItem(comboBox, 1, "bar");
    }

    @Test
    public void updateDataProvider_valueIsReset() {
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue("bar");
        Assert.assertEquals("bar", comboBox.getValue());
        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertNull(comboBox.getValue());
    }

    @Test
    public void setValue_disableComboBox_getValue() {
        ComboBox<String> comboBox = new ComboBox<>("foo", "bar", "paa");
        comboBox.setValue("bar");
        comboBox.setEnabled(false);
        assertEquals("bar", comboBox.getValue());
    }

    @Test
    public void setNull_thrownException() {
        expectNullPointerException("The data provider can not be null");
        ComboBox<Object> comboBox = new ComboBox<>();
        DataProvider<Object, String> dp = null;
        comboBox.setDataProvider(dp);
    }

    @Test
    public void nullItemGenerator_throw() {
        expectNullPointerException("The item label generator can not be null");
        ComboBox<Object> comboBox = new ComboBox<>();
        comboBox.setItemLabelGenerator(null);
    }

    @Test
    public void labelItemGeneratorReturnsNull_throw() {
        expectIllegalStateException(
                "Got 'null' as a label value for the item 'foo'. 'ItemLabelGenerator' instance may not return 'null' values");
        TestComboBox comboBox = new TestComboBox();

        comboBox.setItemLabelGenerator(obj -> null);
        comboBox.setItems(Arrays.asList("foo", "bar"));

        comboBox.getDataGenerator().generateData("foo", Json.createObject());
    }

    @Test
    public void boxWithBinderAndEnums_readBean_valueIsUpdated() {
        ComboBox<Category> combo = new ComboBox<>();
        combo.setItemLabelGenerator(Category::name);
        combo.setItems(Category.values());
        Assert.assertNull(combo.getValue());

        Binder<Bean> binder = new Binder<>();
        binder.forField(combo).bind(Bean::getCategory, Bean::setCategory);

        Bean bean = new Bean();
        bean.setCategory(Category.CATEGORY_2);
        binder.readBean(bean);
        Assert.assertEquals(Category.CATEGORY_2, combo.getValue());

        Assert.assertFalse(binder.hasChanges());

        bean.setCategory(Category.CATEGORY_3);
        binder.readBean(bean);
        Assert.assertEquals(Category.CATEGORY_3, combo.getValue());
    }

    @Test
    public void setValue_selectionPropertySet_listenersAreNotified() {
        ComboBox<Category> combo = new ComboBox<>();
        combo.setItems(Category.values());

        AtomicReference<Category> selected = new AtomicReference<>();
        combo.addValueChangeListener(event -> selected.set(combo.getValue()));

        combo.setValue(Category.CATEGORY_2);

        Assert.assertEquals(Category.CATEGORY_2, selected.get());
    }

    // Ensure https://github.com/vaadin/vaadin-combo-box-flow/issues/36 does not
    // reoccur
    @Test
    public void ensureComboBoxIsFocusable() {
        Assert.assertTrue("ComboBox should be focusable",
                Focusable.class.isAssignableFrom(ComboBox.class));
    }

    @Test
    public void setAutoOpenDisabled() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assert.assertTrue(comboBox.isAutoOpen());
        comboBox.setAutoOpen(false);
        Assert.assertTrue(comboBox.getElement()
                .getProperty(PROP_AUTO_OPEN_DISABLED, false));
        Assert.assertFalse(comboBox.isAutoOpen());
    }

    @Test
    public void setEnabled() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEnabled(true);
        Assert.assertTrue(comboBox.isEnabled());
        comboBox.setEnabled(false);
        Assert.assertFalse(comboBox.isEnabled());
    }

    @Test
    public void addCustomValueSetListener_customValueIsAllowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.addCustomValueSetListener(e -> {
        });
        Assert.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_removeListener_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        registration.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_disableCustomValue_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });

        comboBox.setAllowCustomValue(false);
        Assert.assertFalse(comboBox.isAllowCustomValue());

        // nothing has changed when the listener is removed
        registration.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_addTwoListeners_removeListenerSeveralTimes_customValueIsAllowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());

        // removes the fist listener one more time which is no-op
        registration.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_addTwoListeners_removeListeners_customValueIsDisallowed() {
        ComboBox<String> comboBox = new ComboBox<>();
        Registration registration1 = comboBox.addCustomValueSetListener(e -> {
        });
        Registration registration2 = comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration1.remove();
        Assert.assertTrue(comboBox.isAllowCustomValue());

        // removes the second listener
        registration2.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void getPageSize_default50() {
        ComboBox<String> comboBox = new ComboBox<>();
        Assert.assertEquals(50, comboBox.getPageSize());
    }

    @Test
    public void setPageSize_getPageSize() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPageSize(111);
        Assert.assertEquals(111, comboBox.getPageSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPageSizeZero_throws() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPageSize(0);
    }

    @Test
    public void setValueNull_selectedItemNull() {
        ComboBox<String> comboBox = new ComboBox<>("1", "2");
        comboBox.setValue("1");
        comboBox.setValue(null);
        Assert.assertNull(
                "The selectedItem property must be null when there's no value. "
                        + "Otherwise the 'clear value'-button will be shown.",
                comboBox.getSelectedItemJsonObject());
    }

    @Test
    public void setValueWithoutItems_throw() {
        expectIllegalStateException(
                "Cannot set a value for a ComboBox without items.");
        ComboBox<String> combo = new ComboBox<>();
        combo.setValue("foo");
    }

    // https://github.com/vaadin/vaadin-flow-components/issues/391
    @Test
    public void setValueWithLazyItems_doesntThrow() {
        final ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(query -> Stream.of("foo", "bar"));
        comboBox.setValue("foo");

        Assert.assertEquals("foo", comboBox.getValue());
    }

    @Test
    public void clearWithoutItems_doesNotThrow() {
        ComboBox<String> combo = new ComboBox<>();
        combo.clear();
    }

    @Test
    public void setClearButtonVisible_isClearButtonVisible() {
        ComboBox<String> combo = new ComboBox<>();
        Assert.assertFalse("Clear button should not be visible by default",
                combo.isClearButtonVisible());
        combo.setClearButtonVisible(true);
        Assert.assertTrue("Getter should reflect the set value.",
                combo.isClearButtonVisible());
    }

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-combo-box");
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
                instantiator.createComponent(ComboBoxWithInitialValue.class))
                .thenAnswer(invocation -> new ComboBoxWithInitialValue());
        ComboBoxWithInitialValue field = Component.from(element,
                ComboBoxWithInitialValue.class);
        Assert.assertEquals("foo", field.getElement().getPropertyRaw("value"));
    }

    @Test
    public void dataCommunicator_newComboBoxCreated_dataCommunicatorWithEmptyDataProviderCreated() {
        ComboBox<Object> comboBox = new ComboBox<>();
        DataProvider<Object, ?> dataProvider = comboBox.getDataProvider();

        Assert.assertNotNull(
                "Data Communicator and Data Provider should be created "
                        + "within combo box constructor",
                dataProvider);
        Assert.assertEquals(DataCommunicator.EmptyDataProvider.class,
                dataProvider.getClass());
    }

    @Test
    public void setDataProvider_inMemoryDataProvider_fetchesEagerly() {
        ComboBox<Object> comboBox = new ComboBox<>();
        DataCommunicatorTest.MockUI ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);

        DataProvider<Object, Void> dataProvider = Mockito
                .spy(new AbstractDataProvider<Object, Void>() {

                    @Override
                    public boolean isInMemory() {
                        return true;
                    }

                    @Override
                    public int size(Query query) {
                        return 0;
                    }

                    @Override
                    public Stream<Object> fetch(Query query) {
                        return Stream.empty();
                    }
                });

        comboBox.setDataProvider(dataProvider, filter -> null);

        // Verify that the data communicator and data provider have been created
        Assert.assertNotNull(
                "Data Communicator and Data Provider should be created "
                        + "within setDataProvider()",
                comboBox.getDataProvider());

        fakeClientCommunication(ui);
        Mockito.verify(dataProvider).size(Mockito.any());
    }

    @Test
    public void setDataProvider_backendDataProvider_fetchesOnOpened() {
        ComboBox<Object> comboBox = new ComboBox<>();
        DataCommunicatorTest.MockUI ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);

        DataProvider<Object, Void> dataProvider = Mockito.spy(DataProvider
                .fromCallbacks(query -> Stream.empty(), query -> 0));

        comboBox.setDataProvider(dataProvider, filter -> null);
        // Verify that the data communicator and data provider have been created
        Assert.assertNotNull(
                "Data Communicator and Data Provider should be created "
                        + "within setDataProvider()",
                comboBox.getDataProvider());

        fakeClientCommunication(ui);
        Mockito.verify(dataProvider, Mockito.times(0)).size(Mockito.any());

        // Simulate open event and reset
        comboBox.setOpened(true);
        comboBox.setPageSize(42);
        fakeClientCommunication(ui);

        Mockito.verify(dataProvider).size(Mockito.any());
    }

    @Test
    public void setItems_withItemFilterAndArrayOfItems_shouldReturnMutableListDataView() {
        ComboBox<String> comboBox = new ComboBox<>();
        ComboBox.ItemFilter<String> itemFilter = (item, filter) -> true;
        ComboBoxListDataView<String> listDataView = comboBox
                .setItems(itemFilter, "First", "Second", "Third");
        listDataView.addItem("Fourth");
        listDataView.removeItem("First");
        listDataView.removeItem("Third");
        Assert.assertEquals(2L, listDataView.getItemCount());
    }

    @Test
    public void dataProviderListeners_comboBoxAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        new ComboBox<>(), 2, 2, new int[] { 1, 3 },
                        new DataCommunicatorTest.MockUI());
    }

    private void assertItem(TestComboBox comboBox, int index, String caption) {
        String value1 = comboBox.items.get(index);
        Assert.assertEquals(caption, value1);
    }

    private void expectIllegalArgumentException(String expectedMessage) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedMessage);
    }

    private void expectNullPointerException(String expectedMessage) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(expectedMessage);
    }

    private void expectIllegalStateException(String expectedMessage) {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(expectedMessage);
    }

    private void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
