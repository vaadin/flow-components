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

package com.vaadin.flow.component.combobox.dataview;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableConsumer;

import elemental.json.JsonValue;

public class ComboBoxLazyDataViewTest {

    private static final String TYPE_ERROR_MESSAGE = "ComboBoxLazyDataView "
            + "only supports 'BackEndDataProvider' or it's subclasses, "
            + "but was given a 'ListDataProvider'." + System.lineSeparator()
            + "Use either 'getLazyDataView()', 'getListDataView()' or "
            + "'getGenericDataView()' according to the used data type.";

    private String[] items = { "foo", "bar", "baz" };
    private ComboBoxLazyDataView<String> dataView;
    private ComboBox<String> comboBox;
    private DataCommunicatorTest.MockUI ui;
    private DataCommunicator<String> dataCommunicator;
    private ArrayUpdater arrayUpdater;
    private SerializableConsumer<DataCommunicator.Filter<String>> filterSlot;

    private CallbackDataProvider<String, String> undefinedItemCountDataProvider = DataProvider
            .fromFilteringCallbacks(
                    query -> IntStream.range(0, 1000)
                            .mapToObj(index -> "Item " + index)
                            .filter(item -> item
                                    .contains(query.getFilter().orElse("")))
                            .skip(query.getOffset()).limit(query.getLimit()),
                    query -> {
                        Assert.fail("No item count query expected");
                        return 0;
                    });

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        BackEndDataProvider<String, String> dataProvider = DataProvider
                .fromFilteringCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of(items).filter(
                            item -> item.contains(query.getFilter().orElse("")))
                            .skip(query.getOffset()).limit(query.getLimit());
                }, query -> (int) Stream.of(items).filter(
                        item -> item.contains(query.getFilter().orElse("")))
                        .count());

        comboBox = new ComboBox<>();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);

        ArrayUpdater.Update update = new ArrayUpdater.Update() {

            @Override
            public void clear(int start, int length) {

            }

            @Override
            public void set(int start, List<JsonValue> items) {

            }

            @Override
            public void commit(int updateId) {

            }
        };

        arrayUpdater = Mockito.mock(ArrayUpdater.class);
        Mockito.when(arrayUpdater.startUpdate(Mockito.anyInt()))
                .thenReturn(update);

        dataCommunicator = new DataCommunicator<>((item, jsonObject) -> {
        }, arrayUpdater, null, comboBox.getElement().getNode());

        // set combo box client-side filter to empty by default
        filterSlot = dataCommunicator.setDataProvider(dataProvider, "", true);

        dataView = new ComboBoxLazyDataView<>(dataCommunicator, comboBox);
    }

    @Test
    public void setItemCountCallback_switchFromUndefinedSize_definedSize() {
        Assert.assertTrue(dataCommunicator.isDefinedSize());

        dataView.setItemCountUnknown();
        Assert.assertFalse(dataCommunicator.isDefinedSize());

        dataView.setItemCountCallback(query -> 5);
        Assert.assertTrue(dataCommunicator.isDefinedSize());
    }

    @Test
    public void setItemCountCallback_setAnotherCountCallback_itemCountChanged() {
        final AtomicInteger itemCount = new AtomicInteger(0);
        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));
        dataCommunicator.setRequestedRange(0, 50);

        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);

        Assert.assertEquals("Expected 3 items before setItemCountCallback()", 3,
                itemCount.getAndSet(0));

        dataView.setItemCountCallback(query -> 2);

        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);

        Assert.assertEquals("Expected 2 items after setItemCountCallback()", 2,
                itemCount.get());
    }

    @Test
    public void getLazyDataView_defaultDataProvider_dataViewReturned() {
        ComboBox<String> comboBox = new ComboBox<>();
        ComboBoxLazyDataView<String> lazyDataView = comboBox.getLazyDataView();

        Assert.assertNotNull(lazyDataView);
    }

    @Test
    public void lazyDataViewAPI_comboBoxNotOpenedYet_dataProviderVerificationPassed() {
        ComboBox<String> comboBox = new ComboBox<>();
        ComboBoxLazyDataView<String> dataView = comboBox
                .setItems(query -> Stream.of("foo"));
        dataView.setItemCountEstimate(1000);
        dataView.setItemCountEstimateIncrease(1000);
        dataView.setItemCountUnknown();
        dataView.getItemCountEstimateIncrease();
        dataView.getItemCountEstimate();
        dataView.setItemCountFromDataProvider();
        dataView.setItemCountCallback(query -> 0);
    }

    @Test
    public void setItemCountEstimate_defaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountEstimate(1000);
    }

    @Test
    public void setItemCountEstimateIncrease_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountEstimateIncrease(1000);
    }

    @Test
    public void setItemCountCallback_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountCallback(query -> 0);
    }

    @Test
    public void setItemCountFromDataProvider_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountFromDataProvider();
    }

    @Test
    public void setItemCountUnknown_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().setItemCountUnknown();
    }

    @Test
    public void getItemCountEstimate_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().getItemCountEstimate();
    }

    @Test
    public void getItemCountEstimateIncrease_lazyAPIWithDefaultDataProvider_throws() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(TYPE_ERROR_MESSAGE);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getLazyDataView().getItemCountEstimateIncrease();
    }

    @Test
    public void getItems_withDefinedItemCountAndNoClientSideFilter_returnsNotFilteredItems() {
        Stream<String> filteredItems = dataView.getItems();

        Assert.assertArrayEquals("Unexpected items obtained",
                new String[] { "foo", "bar", "baz" }, filteredItems.toArray());
    }

    @Test
    public void getItems_withUnknownItemCountAndNoClientSideFilter_returnsNotFilteredItems() {
        dataCommunicator.setDataProvider(undefinedItemCountDataProvider, "",
                true);
        dataView.setItemCountUnknown();

        List<String> items = dataView.getItems().collect(Collectors.toList());
        Assert.assertEquals(1000, items.size());
        Assert.assertEquals("Item 0", items.get(0));
        Assert.assertEquals("Item 999", items.get(items.size() - 1));
    }

    @Test
    public void getItem_withDefinedItemCountAndNoClientSideFilter_returnsItemFromNotFilteredSet() {
        Assert.assertEquals("Invalid item on index 1", "bar",
                dataView.getItem(1));
    }

    @Test
    public void getItem_withUnknownItemCountAndNoClientSideFilter_returnsItemFromNotFilteredSet() {
        dataCommunicator.setDataProvider(undefinedItemCountDataProvider, "",
                true);
        dataView.setItemCountUnknown();

        Assert.assertEquals("Invalid item on index 777", "Item 777",
                dataView.getItem(777));
    }

    @Test
    public void getItem_negativeIndex_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Index must be non-negative");
        dataView.getItem(-1);
    }

    @Test
    public void getItem_definedItemCountAndEmptyData_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Requested index 0 on empty data.");
        dataCommunicator.setDataProvider(DataProvider.fromCallbacks(query -> {
            query.getOffset();
            query.getLimit();
            return Stream.empty();
        }, query -> 0), null);

        dataView.getItem(0);
    }

    @Test
    public void getItem_undefinedItemCountAndEmptyData_returnEmptyItem() {
        dataCommunicator.setDataProvider(DataProvider.fromCallbacks(query -> {
            query.getOffset();
            query.getLimit();
            return Stream.empty();
        }, query -> 0), null);
        dataView.setItemCountUnknown();

        Assert.assertNull(dataView.getItem(1234567));
    }

    @Test
    public void getItem_definedItemCountAndOutsideOfRange_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 3 should be less than the item count '3'");
        dataView.getItem(3);
    }

    @Test
    public void getItem_undefinedItemCountAndOutsideOfRange_returnEmptyItem() {
        dataCommunicator.setDataProvider(undefinedItemCountDataProvider, "",
                true);
        dataView.setItemCountUnknown();

        Assert.assertNull(dataView.getItem(1234567));
    }

    @Test
    public void getItem_withCountCallbackAndOutsideOfRange_throw() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 1234567 should be less than the item count '1000'");
        dataCommunicator.setDataProvider(undefinedItemCountDataProvider, "",
                true);
        dataView.setItemCountCallback(query -> 1000);

        dataView.getItem(1234567);
    }

}
