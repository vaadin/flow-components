/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.grid.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractComponentDataViewTest;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.IdentifierProvider;

public class GridDataViewTest extends AbstractComponentDataViewTest {

    @Test
    public void dataViewWithItems_getItem_returnsCorrectItem() {
        Assert.assertEquals(items.get(0), dataView.getItem(0));
        Assert.assertEquals(items.get(1), dataView.getItem(1));
        Assert.assertEquals(items.get(2), dataView.getItem(2));
    }

    @Test
    public void setIdentifierProvider_customIdentifier_keyMapperUsesIdentifier() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        DataProvider<Item, ?> dataProvider = DataProvider.ofCollection(items);
        Grid<Item> component = new Grid<>();

        // Generic grid data view
        DataView<Item> dataView = component.setItems(dataProvider);
        DataKeyMapper<Item> keyMapper = component.getDataCommunicator()
                .getKeyMapper();
        items.forEach(keyMapper::key);

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));

        dataView.setIdentifierProvider(IdentifierProvider.identity());

        // In-memory grid data view
        dataView = component.getListDataView();

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void getItem_itemRequested_dataCommunicatorInvoked() {
        DataCommunicator<String> dataCommunicator = Mockito
                .mock(DataCommunicator.class);
        Mockito.when(dataCommunicator.getDataProvider())
                .thenReturn((DataProvider) DataProvider.ofItems());
        GridDataView<String> dataView = new GridDataView<>(dataCommunicator,
                new Grid<>());
        dataView.getItem(42);
        Mockito.verify(dataCommunicator).getItem(42);
    }

    @Override
    protected HasDataView<String, ? extends DataView<String>> getComponent() {
        return new Grid<>();
    }
}
