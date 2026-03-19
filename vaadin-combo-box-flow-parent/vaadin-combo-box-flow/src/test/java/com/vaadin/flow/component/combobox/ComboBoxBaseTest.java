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
package com.vaadin.flow.component.combobox;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.MockUIExtension;
import com.vaadin.tests.dataprovider.DataProviderListenersTest;

/**
 * Suite of basic tests that apply to both, ComboBox and MultiSelectComboBox
 * Their respective test classes should extend from this class to run tests for
 * both components
 */
abstract class ComboBoxBaseTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    protected abstract <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass);

    @Test
    void implementsFocusable() {
        Assertions.assertTrue(
                Focusable.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should be focusable");
    }

    @Test
    void implementsHasLabel() {
        Assertions.assertTrue(
                HasLabel.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should support setting a label");
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should support setting aria-label and aria-labelledby");
    }

    @Test
    void implementsHasAllowedCharPattern() {
        Assertions.assertTrue(
                HasAllowedCharPattern.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should support allowed char pattern");
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(
                HasTooltip.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should support setting a tooltip");
    }

    @Test
    void implementsHasPlaceholder() {
        Assertions.assertTrue(
                HasPlaceholder.class.isAssignableFrom(
                        createComboBox(String.class).getClass()),
                "ComboBox should support setting a placeholder");
    }

    @Test
    void setAutoOpenDisabled() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assertions.assertTrue(comboBox.isAutoOpen());
        comboBox.setAutoOpen(false);
        Assertions.assertTrue(
                comboBox.getElement().getProperty("autoOpenDisabled", false));
        Assertions.assertFalse(comboBox.isAutoOpen());
    }

    @Test
    void setEnabled() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setEnabled(true);
        Assertions.assertTrue(comboBox.isEnabled());
        comboBox.setEnabled(false);
        Assertions.assertFalse(comboBox.isEnabled());
    }

    @Test
    void addCustomValueSetListener_customValueIsAllowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.addCustomValueSetListener(e -> {
        });
        Assertions.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    void addCustomValueSetListener_removeListener_customValueIsDisallowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        registration.remove();
        Assertions.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    void addCustomValueSetListener_disableCustomValue_customValueIsDisallowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });

        comboBox.setAllowCustomValue(false);
        Assertions.assertFalse(comboBox.isAllowCustomValue());

        // nothing has changed when the listener is removed
        registration.remove();
        Assertions.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    void addCustomValueSetListener_addTwoListeners_removeListenerSeveralTimes_customValueIsAllowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration.remove();
        Assertions.assertTrue(comboBox.isAllowCustomValue());

        // removes the fist listener one more time which is no-op
        registration.remove();
        Assertions.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    void addCustomValueSetListener_addTwoListeners_removeListeners_customValueIsDisallowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Registration registration1 = comboBox.addCustomValueSetListener(e -> {
        });
        Registration registration2 = comboBox.addCustomValueSetListener(e -> {
        });
        // remove the first listener
        registration1.remove();
        Assertions.assertTrue(comboBox.isAllowCustomValue());

        // removes the second listener
        registration2.remove();
        Assertions.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    void getPageSize_default50() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assertions.assertEquals(50, comboBox.getPageSize());
    }

    @Test
    void setPageSize_getPageSize() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setPageSize(111);
        Assertions.assertEquals(111, comboBox.getPageSize());
    }

    @Test
    void setPageSizeZero_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> comboBox.setPageSize(0));
    }

    @Test
    void clearWithoutItems_doesNotThrow() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.clear();
    }

    @Test
    void setClearButtonVisible_isClearButtonVisible() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assertions.assertFalse(comboBox.isClearButtonVisible(),
                "Clear button should not be visible by default");
        comboBox.setClearButtonVisible(true);
        Assertions.assertTrue(comboBox.isClearButtonVisible(),
                "Getter should reflect the set value.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void setItems_createsListDataProvider() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setItems(Arrays.asList("foo", "bar"));

        ListDataProvider<String> dataProvider = (ListDataProvider<String>) comboBox
                .getDataProvider();
        Collection<String> items = dataProvider.getItems();
        Assertions.assertEquals(2, items.size());
        Assertions.assertTrue(items.contains("foo"));
        Assertions.assertTrue(items.contains("bar"));
    }

    @Test
    void setNullDataProvider_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataProvider<String, String> dp = null;
        Assertions.assertThrows(NullPointerException.class,
                () -> comboBox.setItems(dp));
    }

    @Test
    void setNullItemLabelGenerator_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assertions.assertThrows(NullPointerException.class,
                () -> comboBox.setItemLabelGenerator(null));
    }

    @Test
    void itemLabelGeneratorReturnsNull_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setItemLabelGenerator(obj -> null);
        comboBox.setItems(Arrays.asList("foo", "bar"));

        Assertions.assertThrows(IllegalStateException.class,
                () -> comboBox.getDataGenerator().generateData("foo",
                        JacksonUtils.createObjectNode()));
    }

    @Test
    void dataCommunicator_newComboBoxCreated_dataCommunicatorWithEmptyDataProviderCreated() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataProvider<String, ?> dataProvider = comboBox.getDataProvider();

        Assertions.assertNotNull(dataProvider,
                "Data Communicator and Data Provider should be created "
                        + "within combo box constructor");
        Assertions.assertEquals(DataCommunicator.EmptyDataProvider.class,
                dataProvider.getClass());
    }

    @Test
    void setDataProvider_inMemoryDataProvider_fetchesEagerly() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        ui.add(comboBox);

        DataProvider<String, String> dataProvider = Mockito
                .spy(new AbstractDataProvider<String, String>() {

                    @Override
                    public boolean isInMemory() {
                        return true;
                    }

                    @Override
                    public int size(Query query) {
                        return 0;
                    }

                    @Override
                    public Stream<String> fetch(Query query) {
                        return Stream.empty();
                    }
                });

        comboBox.setItems(dataProvider);

        // Verify that the data communicator and data provider have been created
        Assertions.assertNotNull(comboBox.getDataProvider(),
                "Data Communicator and Data Provider should be created "
                        + "within setDataProvider()");

        ui.fakeClientCommunication();
        Mockito.verify(dataProvider).size(Mockito.any());
    }

    @Test
    void setDataProvider_backendDataProvider_fetchesOnOpened() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        ui.add(comboBox);

        DataProvider<String, String> dataProvider = Mockito.spy(DataProvider
                .fromFilteringCallbacks(query -> Stream.empty(), query -> 0));

        comboBox.setItems(dataProvider);
        // Verify that the data communicator and data provider have been created
        Assertions.assertNotNull(comboBox.getDataProvider(),
                "Data Communicator and Data Provider should be created "
                        + "within setDataProvider()");

        ui.fakeClientCommunication();
        Mockito.verify(dataProvider, Mockito.times(0)).size(Mockito.any());

        // Simulate open event and reset
        comboBox.setOpened(true);
        comboBox.setPageSize(42);
        ui.fakeClientCommunication();

        Mockito.verify(dataProvider).size(Mockito.any());
    }

    @Test
    void setItems_withItemFilterAndArrayOfItems_shouldReturnMutableListDataView() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        ComboBox.ItemFilter<String> itemFilter = (item, filter) -> true;
        ComboBoxListDataView<String> listDataView = comboBox
                .setItems(itemFilter, "First", "Second", "Third");
        listDataView.addItem("Fourth");
        listDataView.removeItem("First");
        listDataView.removeItem("Third");
        Assertions.assertEquals(2L, listDataView.getItemCount());
    }

    @Test
    void dataProviderListeners_comboBoxAttachedAndDetached_oldDataProviderListenerRemoved() {
        DataProviderListenersTest
                .checkOldListenersRemovedOnComponentAttachAndDetach(
                        createComboBox(Object.class), 2, 2, new int[] { 1, 3 },
                        ui.getUI());
    }

    @Test
    void setAriaLabel() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);

        comboBox.setAriaLabel("aria-label");
        Assertions.assertTrue(comboBox.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", comboBox.getAriaLabel().get());

        comboBox.setAriaLabel(null);
        Assertions.assertTrue(comboBox.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);

        comboBox.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(comboBox.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                comboBox.getAriaLabelledBy().get());

        comboBox.setAriaLabelledBy(null);
        Assertions.assertTrue(comboBox.getAriaLabelledBy().isEmpty());
    }
}
