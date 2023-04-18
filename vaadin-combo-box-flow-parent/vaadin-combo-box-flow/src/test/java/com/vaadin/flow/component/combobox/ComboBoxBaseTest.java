/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.DataProviderListenersTest;
import elemental.json.Json;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Suite of basic tests that apply to both, ComboBox and MultiSelectComboBox
 * Their respective test classes should extend from this class to run tests for
 * both components
 */
public abstract class ComboBoxBaseTest {

    protected abstract <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass);

    @Test
    public void implementsFocusable() {
        Assert.assertTrue("ComboBox should be focusable", Focusable.class
                .isAssignableFrom(createComboBox(String.class).getClass()));
    }

    @Test
    public void implementsHasLabel() {
        Assert.assertTrue("ComboBox should support setting a label",
                HasLabel.class.isAssignableFrom(
                        createComboBox(String.class).getClass()));
    }

    @Test
    public void implementsHasAriaLabel() {
        Assert.assertTrue(
                "ComboBox should support setting aria-label and aria-labelledby",
                HasAriaLabel.class.isAssignableFrom(
                        createComboBox(String.class).getClass()));
    }

    @Test
    public void implementsHasAllowedCharPattern() {
        Assert.assertTrue("ComboBox should support allowed char pattern",
                HasAllowedCharPattern.class.isAssignableFrom(
                        createComboBox(String.class).getClass()));
    }

    @Test
    public void implementsHasOverlayClassName() {
        Assert.assertTrue("ComboBox should support overlay class name",
                HasOverlayClassName.class.isAssignableFrom(
                        createComboBox(String.class).getClass()));
    }

    @Test
    public void implementsHasTooltip() {
        Assert.assertTrue("ComboBox should support setting a tooltip",
                HasTooltip.class.isAssignableFrom(
                        createComboBox(String.class).getClass()));
    }

    @Test
    public void setAutoOpenDisabled() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assert.assertTrue(comboBox.isAutoOpen());
        comboBox.setAutoOpen(false);
        Assert.assertTrue(
                comboBox.getElement().getProperty("autoOpenDisabled", false));
        Assert.assertFalse(comboBox.isAutoOpen());
    }

    @Test
    public void setEnabled() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setEnabled(true);
        Assert.assertTrue(comboBox.isEnabled());
        comboBox.setEnabled(false);
        Assert.assertFalse(comboBox.isEnabled());
    }

    @Test
    public void addCustomValueSetListener_customValueIsAllowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.addCustomValueSetListener(e -> {
        });
        Assert.assertTrue(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_removeListener_customValueIsDisallowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Registration registration = comboBox.addCustomValueSetListener(e -> {
        });
        registration.remove();
        Assert.assertFalse(comboBox.isAllowCustomValue());
    }

    @Test
    public void addCustomValueSetListener_disableCustomValue_customValueIsDisallowed() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
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
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
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
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
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
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assert.assertEquals(50, comboBox.getPageSize());
    }

    @Test
    public void setPageSize_getPageSize() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setPageSize(111);
        Assert.assertEquals(111, comboBox.getPageSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPageSizeZero_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setPageSize(0);
    }

    @Test
    public void clearWithoutItems_doesNotThrow() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.clear();
    }

    @Test
    public void setClearButtonVisible_isClearButtonVisible() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        Assert.assertFalse("Clear button should not be visible by default",
                comboBox.isClearButtonVisible());
        comboBox.setClearButtonVisible(true);
        Assert.assertTrue("Getter should reflect the set value.",
                comboBox.isClearButtonVisible());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setItems_createsListDataProvider() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setItems(Arrays.asList("foo", "bar"));

        ListDataProvider<String> dataProvider = (ListDataProvider<String>) comboBox
                .getDataProvider();
        Collection<String> items = dataProvider.getItems();
        Assert.assertEquals(2, items.size());
        Assert.assertTrue(items.contains("foo"));
        Assert.assertTrue(items.contains("bar"));
    }

    @Test(expected = NullPointerException.class)
    public void setNullDataProvider_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataProvider<String, String> dp = null;
        comboBox.setItems(dp);
    }

    @Test(expected = NullPointerException.class)
    public void setNullItemLabelGenerator_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setItemLabelGenerator(null);
    }

    @Test(expected = IllegalStateException.class)
    public void itemLabelGeneratorReturnsNull_throws() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        comboBox.setItemLabelGenerator(obj -> null);
        comboBox.setItems(Arrays.asList("foo", "bar"));

        comboBox.getDataGenerator().generateData("foo", Json.createObject());
    }

    @Test
    public void dataCommunicator_newComboBoxCreated_dataCommunicatorWithEmptyDataProviderCreated() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataProvider<String, ?> dataProvider = comboBox.getDataProvider();

        Assert.assertNotNull(
                "Data Communicator and Data Provider should be created "
                        + "within combo box constructor",
                dataProvider);
        Assert.assertEquals(DataCommunicator.EmptyDataProvider.class,
                dataProvider.getClass());
    }

    @Test
    public void setDataProvider_inMemoryDataProvider_fetchesEagerly() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataCommunicatorTest.MockUI ui = new DataCommunicatorTest.MockUI();
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
        Assert.assertNotNull(
                "Data Communicator and Data Provider should be created "
                        + "within setDataProvider()",
                comboBox.getDataProvider());

        fakeClientCommunication(ui);
        Mockito.verify(dataProvider).size(Mockito.any());
    }

    @Test
    public void setDataProvider_backendDataProvider_fetchesOnOpened() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
        DataCommunicatorTest.MockUI ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);

        DataProvider<String, String> dataProvider = Mockito.spy(DataProvider
                .fromFilteringCallbacks(query -> Stream.empty(), query -> 0));

        comboBox.setItems(dataProvider);
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
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);
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
                        createComboBox(Object.class), 2, 2, new int[] { 1, 3 },
                        new DataCommunicatorTest.MockUI());
    }

    @Test
    public void setAriaLabel() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);

        comboBox.setAriaLabel("aria-label");
        Assert.assertTrue(comboBox.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", comboBox.getAriaLabel().get());

        comboBox.setAriaLabel(null);
        Assert.assertTrue(comboBox.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        ComboBoxBase<?, String, ?> comboBox = createComboBox(String.class);

        comboBox.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(comboBox.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby",
                comboBox.getAriaLabelledBy().get());

        comboBox.setAriaLabelledBy(null);
        Assert.assertTrue(comboBox.getAriaLabelledBy().isEmpty());
    }

    private void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
