/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.VaadinSession;

public class GridHiddenColumnRenderingTest {

    private static final int ITEM_COUNT = 10;

    private UI ui;

    private Grid<String> grid;

    private AtomicInteger callCount;

    @Before
    public void setup() {
        ui = new UI();
        UI.setCurrent(ui);
        var session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        grid = new Grid<>();
        grid.setItems(getItems());
        ui.add(grid);
        fakeClientCommunication();
        callCount = new AtomicInteger(0);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void generateDataWhenHiddenTrueByDefault() {
        var column = grid.addColumn(s -> s);
        Assert.assertTrue(column.isGenerateDataWhenHidden());
    }

    @Test
    public void setGenerateDataWhenHidden_valueIsCorrectlySet() {
        var column = grid.addColumn(s -> s).setGenerateDataWhenHidden(false);
        Assert.assertFalse(column.isGenerateDataWhenHidden());
        column.setGenerateDataWhenHidden(true);
        Assert.assertTrue(column.isGenerateDataWhenHidden());
    }

    @Test
    public void dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void setGenerateDataWhenHiddenFalse_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void initiallyHiddenColumn_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void initiallyHiddenColumn_setGenerateDataWhenHiddenFalse_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            fakeClientCommunication();
            resetCallCount();
            column.setGenerateDataWhenHidden(false);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void setHidden_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            fakeClientCommunication();
            resetCallCount();
            column.setVisible(false);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void setGenerateDataWhenHiddenFalse_setHidden_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            fakeClientCommunication();
            resetCallCount();
            column.setVisible(false);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void initiallyHiddenColumn_setVisible_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            fakeClientCommunication();
            resetCallCount();
            column.setVisible(true);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void initiallyHiddenColumn_setGenerateDataWhenHiddenFalse_setVisible_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            column.setVisible(false);
            fakeClientCommunication();
            column.setVisible(true);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void toggleHiddenTwiceInRoundTrip_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            fakeClientCommunication();
            column.setVisible(false);
            column.setVisible(true);
            resetCallCount();
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void setGenerateDataWhenHiddenFalse_toggleHiddenTwiceInRoundTrip_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            fakeClientCommunication();
            column.setVisible(false);
            column.setVisible(true);
            resetCallCount();
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void initiallyHiddenColumn_toggleHiddenTwiceInRoundTrip_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            fakeClientCommunication();
            resetCallCount();
            column.setVisible(true);
            column.setVisible(false);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void initiallyHiddenColumn_setGenerateDataWhenHiddenFalse_toggleHiddenTwiceInRoundTrip_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            fakeClientCommunication();
            resetCallCount();
            column.setGenerateDataWhenHidden(false);
            column.setVisible(true);
            column.setVisible(false);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void detachAndReattachGrid_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            ui.remove(grid);
            fakeClientCommunication();
            resetCallCount();
            ui.add(grid);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void setGenerateDataWhenHiddenFalse_detachAndReattachGrid_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            ui.remove(grid);
            fakeClientCommunication();
            resetCallCount();
            ui.add(grid);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void initiallyHiddenColumn_detachAndReattachGrid_dataGeneratorCalledOncePerItem() {
        runTestCodeForMultipleColumns(column -> {
            column.setVisible(false);
            ui.remove(grid);
            fakeClientCommunication();
            resetCallCount();
            ui.add(grid);
            fakeClientCommunication();
            assertCalledOncePerItem();
        });
    }

    @Test
    public void initiallyHiddenColumn_setGenerateDataWhenHiddenFalse_detachAndReattachGrid_dataGeneratorNotCalled() {
        runTestCodeForMultipleColumns(column -> {
            column.setGenerateDataWhenHidden(false);
            column.setVisible(false);
            ui.remove(grid);
            fakeClientCommunication();
            ui.add(grid);
            fakeClientCommunication();
            assertNotCalled();
        });
    }

    @Test
    public void columnWithCustomRenderer_setAnotherRenderer_onlyNewRendererCalled() {
        var column = addColumnWithCustomRenderer();
        fakeClientCommunication();
        resetCallCount();
        var newRendererCallCount = new AtomicInteger(0);
        var newRenderer = LitRenderer
                .<String> of("<span>${item.displayName}</span>")
                .withProperty("displayName", s -> {
                    newRendererCallCount.incrementAndGet();
                    return s;
                });
        column.setRenderer(newRenderer);
        fakeClientCommunication();
        assertNotCalled();
        Assert.assertEquals(ITEM_COUNT, newRendererCallCount.get());
    }

    @Test
    public void columnWithCustomRenderer_setGenerateDataWhenHiddenFalse_setAnotherRenderer_onlyNewRendererCalled() {
        var column = addColumnWithCustomRenderer()
                .setGenerateDataWhenHidden(false);
        fakeClientCommunication();
        resetCallCount();
        var newRendererCallCount = new AtomicInteger(0);
        var newRenderer = LitRenderer
                .<String> of("<span>${item.displayName}</span>")
                .withProperty("displayName", s -> {
                    newRendererCallCount.incrementAndGet();
                    return s;
                });
        column.setRenderer(newRenderer);
        fakeClientCommunication();
        assertNotCalled();
        Assert.assertEquals(ITEM_COUNT, newRendererCallCount.get());
    }

    @Test
    public void addColumn_itemsSentOnlyOnce() {
        var items = getItems();
        var fetchCount = new AtomicInteger(0);
        var dataProvider = DataProvider.<String> fromCallbacks(query -> {
            fetchCount.incrementAndGet();
            return items.stream().skip(query.getOffset())
                    .limit(query.getLimit());
        }, query -> items.size());
        grid.setDataProvider(dataProvider);
        fakeClientCommunication();
        fetchCount.set(0);
        addColumnWithValueProvider();
        fakeClientCommunication();
        Assert.assertEquals(1, fetchCount.get());
    }

    @Test
    public void addColumn_setGenerateDataWhenHiddenFalse_itemsSentOnlyOnce() {
        var items = getItems();
        var fetchCount = new AtomicInteger(0);
        var dataProvider = DataProvider.<String> fromCallbacks(query -> {
            fetchCount.incrementAndGet();
            return items.stream().skip(query.getOffset())
                    .limit(query.getLimit());
        }, query -> items.size());
        grid.setDataProvider(dataProvider);
        fakeClientCommunication();
        fetchCount.set(0);
        addColumnWithValueProvider().setGenerateDataWhenHidden(false);
        fakeClientCommunication();
        Assert.assertEquals(1, fetchCount.get());
    }

    private void runTestCodeForMultipleColumns(
            Consumer<Grid.Column<String>> testCode) {
        getColumnSuppliers().forEach(columnSupplier -> {
            resetCallCount();
            var column = columnSupplier.get();
            testCode.accept(column);
            grid.removeColumn(column);
        });
    }

    private List<Supplier<Grid.Column<String>>> getColumnSuppliers() {
        return List.of(this::addColumnWithValueProvider,
                this::addComponentColumn, this::addColumnWithCustomRenderer);
    }

    private Grid.Column<String> addColumnWithValueProvider() {
        return grid.addColumn(getValueProvider());
    }

    private Grid.Column<String> addComponentColumn() {
        return grid.addComponentColumn(this::getComponent);
    }

    private Grid.Column<String> addColumnWithCustomRenderer() {
        return grid.addColumn(getCustomRenderer());
    }

    private ValueProvider<String, ?> getValueProvider() {
        return s -> {
            callCount.incrementAndGet();
            return s;
        };
    }

    private Component getComponent(String string) {
        callCount.incrementAndGet();
        return new NativeButton(string);
    }

    private Renderer<String> getCustomRenderer() {
        return LitRenderer.<String> of("<div>${item.displayName}</div>")
                .withProperty("displayName", getValueProvider());
    }

    private List<String> getItems() {
        return IntStream.range(0, ITEM_COUNT).mapToObj(i -> "Item " + i)
                .toList();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private void assertNotCalled() {
        Assert.assertEquals(0, getCallCount());
    }

    private void assertCalledOncePerItem() {
        Assert.assertEquals(ITEM_COUNT, getCallCount());
    }

    private int getCallCount() {
        return callCount.get();
    }

    private void resetCallCount() {
        callCount.set(0);
    }
}
