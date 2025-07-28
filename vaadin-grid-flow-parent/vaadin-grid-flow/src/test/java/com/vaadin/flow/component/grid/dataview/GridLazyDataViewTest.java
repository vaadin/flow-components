/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;

public class GridLazyDataViewTest {

    private GridLazyDataView<String> dataView;
    private Grid<String> grid;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void setup() {
        BackEndDataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of("foo", "bar", "baz");
                }, query -> 3);

        grid = new Grid<>();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(grid);

        dataView = grid.setItems(dataProvider);
    }

    @Test
    public void setItemCountCallback_switchFromUndefinedSize_definedSize() {
        Assert.assertTrue(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountUnknown();
        Assert.assertFalse(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountCallback(query -> 5);
        Assert.assertTrue(grid.getDataCommunicator().isDefinedSize());
    }

    @Test
    public void setItemCountCallback_setAnotherCountCallback_itemCountChanged() {
        final AtomicInteger itemCount = new AtomicInteger(0);
        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));
        grid.getDataCommunicator().setViewportRange(0, 50);

        dataView.setItemCountCallback(query -> 2);

        fakeClientCommunication();

        Assert.assertEquals("Invalid item count reported", 2, itemCount.get());
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
