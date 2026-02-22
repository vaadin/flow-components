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
package com.vaadin.flow.component.combobox.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.tests.MockUIRule;

import tools.jackson.databind.JsonNode;

public class ComboBoxLazyDataViewTest {
    @Rule
    public MockUIRule ui = new MockUIRule();

    private static final String TYPE_ERROR_MESSAGE = "ComboBoxLazyDataView "
            + "only supports 'BackEndDataProvider' or it's subclasses, "
            + "but was given a 'ListDataProvider'." + System.lineSeparator()
            + "Use either 'getLazyDataView()', 'getListDataView()' or "
            + "'getGenericDataView()' according to the used data type.";

    private String[] items = { "foo", "bar", "baz" };
    private ComboBoxLazyDataView<String> dataView;
    private ComboBox<String> comboBox;
    private DataCommunicator<String> dataCommunicator;
    private ArrayUpdater arrayUpdater;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        BackEndDataProvider<String, String> dataProvider = DataProvider
                .fromFilteringCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of(items);
                }, query -> 3);

        comboBox = new ComboBox<>();
        ui.add(comboBox);

        ArrayUpdater.Update update = new ArrayUpdater.Update() {

            @Override
            public void clear(int start, int length) {

            }

            @Override
            public void set(int start, List<JsonNode> items) {

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

        dataCommunicator.setDataProvider(dataProvider, null);

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
        dataCommunicator.setViewportRange(0, 50);

        ui.fakeClientCommunication();

        Assert.assertEquals("Expected 3 items before setItemCountCallback()", 3,
                itemCount.getAndSet(0));

        dataView.setItemCountCallback(query -> 2);

        ui.fakeClientCommunication();

        Assert.assertEquals("Expected 2 items after setItemCountCallback()", 2,
                itemCount.get());
    }

    @Test
    public void getLazyDataView_defaulDataProvider_dataViewReturned() {
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
    public void setIdentifierProvider_customIdentifier_keyMapperUsesIdentifier() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        ComboBox<Item> component = new ComboBox<>();

        DataCommunicator<Item> dataCommunicator = new DataCommunicator<>(
                (item, jsonObject) -> {
                }, null, null, component.getElement().getNode());
        dataCommunicator.setDataProvider(
                new CallbackDataProvider<>(query -> Stream.of(), query -> 0),
                "", true);

        ComboBoxLazyDataView<Item> dataView = new ComboBoxLazyDataView<>(
                dataCommunicator, component);
        DataKeyMapper<Item> keyMapper = dataCommunicator.getKeyMapper();
        items.forEach(keyMapper::key);

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));
    }
}
