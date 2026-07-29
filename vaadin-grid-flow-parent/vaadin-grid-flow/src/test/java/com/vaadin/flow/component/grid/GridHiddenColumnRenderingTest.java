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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

class GridHiddenColumnRenderingTest {

    @RegisterExtension
    private MockUIExtension ui = new MockUIExtension();

    private Grid<String> grid;
    private Grid.Column<String> column;

    private AtomicInteger dataProviderFetchCallCount = new AtomicInteger(0);
    private AtomicInteger dataProviderSizeCallCount = new AtomicInteger(0);
    private AtomicInteger columnGenerateDataCallCount = new AtomicInteger(0);
    private AtomicInteger columnRefreshDataCallCount = new AtomicInteger(0);
    private AtomicInteger columnTooltipGeneratorCallCount = new AtomicInteger(
            0);
    private AtomicInteger columnPartNameGeneratorCallCount = new AtomicInteger(
            0);

    @BeforeEach
    void setup() {
        grid = new Grid<>();

        grid.setItems(query -> {
            dataProviderFetchCallCount.incrementAndGet();
            return Stream.of("Item 0").skip(query.getOffset())
                    .limit(query.getLimit());
        }, query -> {
            dataProviderSizeCallCount.incrementAndGet();
            return 1;
        });

        column = grid.addColumn(createSpyRenderer());

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
    void initiallyVisibleColumn_columnDataGenerated() {
        ui.fakeClientCommunication();
        assertColumnDataGenerated();
    }

    @Test
    void initiallyHiddenColumn_columnDataNotGenerated() {
        column.setVisible(false);
        ui.fakeClientCommunication();
        assertColumnDataNotGenerated();
    }

    @Nested
    class InitiallyVisibleColumnClass {
        @BeforeEach
        void setup() {
            ui.fakeClientCommunication();
            resetColumnDataSpies();
            resetDataProviderSpies();
        }

        @Test
        void refreshItem_columnDataRefreshed() {
            grid.getDataProvider().refreshItem("Item 0");
            ui.fakeClientCommunication();
            assertColumnDataRefreshed();
        }

        @Test
        void detachAndAttachGrid_columnDataGenerated() {
            ui.remove(grid);
            ui.add(grid);
            ui.fakeClientCommunication();
            assertColumnDataGenerated();
        }

        @Test
        void hideColumn_columnDataNotGenerated() {
            column.setVisible(false);
            ui.fakeClientCommunication();
            assertColumnDataNotGenerated();
        }

        @Test
        void hideColumn_dataProviderNotCalled() {
            column.setVisible(false);
            ui.fakeClientCommunication();
            assertDataProviderNotCalled();
        }

        @Test
        void hideAndImmediatelyShowColumn_columnDataGenerated() {
            column.setVisible(false);
            column.setVisible(true);
            ui.fakeClientCommunication();
            assertColumnDataGenerated();
        }
    }

    @Nested
    class InitiallyHiddenColumnClass {
        @BeforeEach
        void setup() {
            column.setVisible(false);
            ui.fakeClientCommunication();
            resetColumnDataSpies();
            resetDataProviderSpies();
        }

        @Test
        void refreshItem_columnDataNotRefreshed() {
            grid.getDataProvider().refreshItem("Item 0");
            ui.fakeClientCommunication();
            assertColumnDataNotRefreshed();
        }

        @Test
        void detachAndAttachGrid_columnDataNotGenerated() {
            ui.remove(grid);
            ui.add(grid);
            ui.fakeClientCommunication();
            assertColumnDataNotGenerated();
        }

        @Test
        void setRenderer_columnDataNotGenerated() {
            column.setRenderer(createSpyRenderer());
            ui.fakeClientCommunication();
            assertColumnDataNotGenerated();
        }

        @Test
        void showColumn_columnDataGenerated() {
            column.setVisible(true);
            ui.fakeClientCommunication();
            assertColumnDataGenerated();
        }

        @Test
        void showColumn_dataProviderNotCalled() {
            column.setVisible(true);
            ui.fakeClientCommunication();
            assertDataProviderNotCalled();
        }

        @Test
        void showAndImmediatelyHideColumn_columnDataNotGenerated() {
            column.setVisible(true);
            column.setVisible(false);
            ui.fakeClientCommunication();
            assertColumnDataNotGenerated();
        }
    }

    private Renderer<String> createSpyRenderer() {
        return new Renderer<>() {
            @Override
            public Rendering<String> render(Element container,
                    DataKeyMapper<String> keyMapper, String rendererName) {
                return () -> Optional.of(new DataGenerator<String>() {
                    @Override
                    public void generateData(String item,
                            ObjectNode jsonObject) {
                        columnGenerateDataCallCount.incrementAndGet();
                    }

                    @Override
                    public void refreshData(String item) {
                        columnRefreshDataCallCount.incrementAndGet();
                    }
                });
            }
        };
    }

    private void resetColumnDataSpies() {
        columnRefreshDataCallCount.set(0);
        columnGenerateDataCallCount.set(0);
        columnTooltipGeneratorCallCount.set(0);
        columnPartNameGeneratorCallCount.set(0);
    }

    private void assertColumnDataGenerated() {
        Assertions.assertEquals(1, columnGenerateDataCallCount.get());
        Assertions.assertEquals(1, columnTooltipGeneratorCallCount.get());
        Assertions.assertEquals(1, columnPartNameGeneratorCallCount.get());
    }

    private void assertColumnDataNotGenerated() {
        Assertions.assertEquals(0, columnGenerateDataCallCount.get());
        Assertions.assertEquals(0, columnTooltipGeneratorCallCount.get());
        Assertions.assertEquals(0, columnPartNameGeneratorCallCount.get());
    }

    private void assertColumnDataRefreshed() {
        Assertions.assertEquals(1, columnRefreshDataCallCount.get());
    }

    private void assertColumnDataNotRefreshed() {
        Assertions.assertEquals(0, columnRefreshDataCallCount.get());
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
