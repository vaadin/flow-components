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
package com.vaadin.flow.component.grid.dataview;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.tests.MockUIExtension;

class GridLazyDataViewTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private GridLazyDataView<String> dataView;
    private Grid<String> grid;

    @BeforeEach
    void setup() {
        BackEndDataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of("foo", "bar", "baz");
                }, query -> 3);

        grid = new Grid<>();
        ui.add(grid);

        dataView = grid.setItems(dataProvider);
    }

    @Test
    void setItemCountCallback_switchFromUndefinedSize_definedSize() {
        Assertions.assertTrue(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountUnknown();
        Assertions.assertFalse(grid.getDataCommunicator().isDefinedSize());

        dataView.setItemCountCallback(query -> 5);
        Assertions.assertTrue(grid.getDataCommunicator().isDefinedSize());
    }

    @Test
    void setItemCountCallback_setAnotherCountCallback_itemCountChanged() {
        final AtomicInteger itemCount = new AtomicInteger(0);
        dataView.addItemCountChangeListener(
                event -> itemCount.set(event.getItemCount()));
        grid.getDataCommunicator().setViewportRange(0, 50);

        dataView.setItemCountCallback(query -> 2);

        ui.fakeClientCommunication();

        Assertions.assertEquals(2, itemCount.get(),
                "Invalid item count reported");
    }
}
