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
package com.vaadin.flow.component.grid;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

class GridHiddenColumnRenderingTest {

    private static List<String> ITEMS = IntStream.range(0, 10)
            .mapToObj(i -> "Item " + i).toList();

    @RegisterExtension
    private MockUIExtension ui = new MockUIExtension();

    private Grid<String> grid;

    private Grid.Column<String> column;

    private AtomicInteger dataProviderFetchCallCount = new AtomicInteger(0);
    private AtomicInteger dataProviderSizeCallCount = new AtomicInteger(0);
    private AtomicInteger columnValueProviderCallCount = new AtomicInteger(0);
    private AtomicInteger columnTooltipGeneratorCallCount = new AtomicInteger(
            0);
    private AtomicInteger columnPartNameGeneratorCallCount = new AtomicInteger(
            0);

    @BeforeEach
    void setup() {
        grid = new Grid<>();

        grid.setItems(query -> {
            dataProviderFetchCallCount.incrementAndGet();
            return ITEMS.stream().skip(query.getOffset())
                    .limit(query.getLimit());
        }, query -> {
            dataProviderSizeCallCount.incrementAndGet();
            return ITEMS.size();
        });

        column = grid.addColumn(s -> {
            columnValueProviderCallCount.incrementAndGet();
            return s;
        });

        column.setPartNameGenerator(s -> {
            columnPartNameGeneratorCallCount.incrementAndGet();
            return "part";
        });

        column.setTooltipGenerator(s -> {
            columnTooltipGeneratorCallCount.incrementAndGet();
            return "tooltip";
        });

        ui.add(grid);
    }

    @Test
    void initiallyVisibleColumn_columnDataSent() {
        ui.fakeClientCommunication();
        assertColumnDataSent();
    }

    @Test
    void initiallyVisibleColumn_detachAndAttachGrid_columnDataSentOnReattach() {
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        ui.remove(grid);
        ui.add(grid);
        ui.fakeClientCommunication();
        assertColumnDataSent();
    }

    @Test
    void initiallyHiddenColumn_columnDataNotSent() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        assertColumnDataNotSent();
    }

    @Test
    void initiallyHiddenColumn_detachAndAttachGrid_columnDataNotSentOnReattach() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        ui.remove(grid);
        ui.add(grid);
        ui.fakeClientCommunication();
        assertColumnDataNotSent();
    }

    @Test
    void hideColumn_columnDataNotSent() {
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        column.setVisible(false);
        ui.fakeClientCommunication();
        assertColumnDataNotSent();
    }

    @Test
    void hideAndImmediatelyShowColumn_columnDataSent() {
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        column.setVisible(false);
        column.setVisible(true);
        ui.fakeClientCommunication();
        assertColumnDataSent();
    }

    @Test
    void showColumn_columnDataSent() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        column.setVisible(true);
        ui.fakeClientCommunication();
        assertColumnDataSent();
    }

    @Test
    void showAndImmediatelyHideColumn_columnDataNotSent() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        column.setVisible(true);
        column.setVisible(false);
        ui.fakeClientCommunication();
        assertColumnDataNotSent();
    }

    @Test
    void hiddenColumn_setRenderer_columnDataNotSent() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        resetColumnDataSpies();

        column.setRenderer(new ColumnPathRenderer<>("value", s -> {
            columnValueProviderCallCount.incrementAndGet();
            return s;
        }));
        ui.fakeClientCommunication();
        assertColumnDataNotSent();
    }

    @Test
    void hideColumn_dataProviderNotCalled() {
        ui.fakeClientCommunication();
        resetDataProviderSpies();

        column.setVisible(false);
        ui.fakeClientCommunication();
        assertDataProviderNotCalled();
    }

    @Test
    void showColumn_dataProviderNotCalled() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        resetDataProviderSpies();

        column.setVisible(true);
        ui.fakeClientCommunication();
        assertDataProviderNotCalled();
    }

    private void resetColumnDataSpies() {
        columnValueProviderCallCount.set(0);
        columnTooltipGeneratorCallCount.set(0);
        columnPartNameGeneratorCallCount.set(0);
    }

    private void assertColumnDataSent() {
        Assertions.assertEquals(ITEMS.size(),
                columnValueProviderCallCount.get());
        Assertions.assertEquals(ITEMS.size(),
                columnTooltipGeneratorCallCount.get());
        Assertions.assertEquals(ITEMS.size(),
                columnPartNameGeneratorCallCount.get());
    }

    private void assertColumnDataNotSent() {
        Assertions.assertEquals(0, columnValueProviderCallCount.get());
        Assertions.assertEquals(0, columnTooltipGeneratorCallCount.get());
        Assertions.assertEquals(0, columnPartNameGeneratorCallCount.get());
    }

    private void resetDataProviderSpies() {
        dataProviderFetchCallCount.set(0);
        dataProviderSizeCallCount.set(0);
    }

    private void assertDataProviderNotCalled() {
        Assertions.assertEquals(0, dataProviderFetchCallCount.get());
        Assertions.assertEquals(0, dataProviderSizeCallCount.get());
    }
}
