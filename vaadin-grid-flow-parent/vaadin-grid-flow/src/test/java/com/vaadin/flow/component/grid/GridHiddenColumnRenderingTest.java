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
        VaadinSession session = Mockito.mock(VaadinSession.class);
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
    public void columnWithValueProvider_rendererCalledOncePerItem() {
        addColumnWithValueProvider();
        initiallyVisibleColumn_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenColumnWithValueProvider_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyHiddenColumn_assertRendererNotCalled(column);
    }

    @Test
    public void columnWithValueProvider_setHidden_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyVisibleColumn_setHidden_assertRendererNotCalled(column);
    }

    @Test
    public void initiallyHiddenColumnWithValueProvider_setVisible_rendererCalledOncePerItem() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyHiddenColumn_setVisible_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void columnWithValueProvider_toggleHiddenTwiceInRoundTrip_rendererCalledOncePerItem() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyVisibleColumn_toggleHiddenTwiceInRoundTrip_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void initiallyHiddenColumnWithValueProvider_toggleHiddenTwiceInRoundTrip_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyHiddenColumn_toggleHiddenTwiceInRoundTrip_assertRendererNotCalled(
                column);
    }

    @Test
    public void componentColumn_rendererCalledOncePerItem() {
        addComponentColumn();
        initiallyVisibleColumn_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenComponentColumn_rendererNotCalled() {
        Grid.Column<String> column = addComponentColumn();
        initiallyHiddenColumn_assertRendererNotCalled(column);
    }

    @Test
    public void componentColumn_setHidden_rendererNotCalled() {
        Grid.Column<String> column = addComponentColumn();
        initiallyVisibleColumn_setHidden_assertRendererNotCalled(column);
    }

    @Test
    public void initiallyHiddenComponentColumn_setVisible_rendererCalledOncePerItem() {
        Grid.Column<String> column = addComponentColumn();
        initiallyHiddenColumn_setVisible_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void componentColumn_toggleHiddenTwiceInRoundTrip_rendererCalledOncePerItem() {
        Grid.Column<String> column = addComponentColumn();
        initiallyVisibleColumn_toggleHiddenTwiceInRoundTrip_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void initiallyHiddenComponentColumn_toggleHiddenTwiceInRoundTrip_rendererNotCalled() {
        Grid.Column<String> column = addComponentColumn();
        initiallyHiddenColumn_toggleHiddenTwiceInRoundTrip_assertRendererNotCalled(
                column);
    }

    @Test
    public void columnWithCustomRenderer_rendererCalledOncePerItem() {
        addColumnWithCustomRenderer();
        initiallyVisibleColumn_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenColumnWithCustomRenderer_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyHiddenColumn_assertRendererNotCalled(column);
    }

    @Test
    public void columnWithCustomRenderer_setHidden_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyVisibleColumn_setHidden_assertRendererNotCalled(column);
    }

    @Test
    public void initiallyHiddenColumnWithCustomRenderer_setVisible_rendererCalledOncePerItem() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyHiddenColumn_setVisible_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void columnWithCustomRenderer_toggleHiddenTwiceInRoundTrip_rendererCalledOncePerItem() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyVisibleColumn_toggleHiddenTwiceInRoundTrip_assertRendererCalledOncePerItem(
                column);
    }

    @Test
    public void initiallyHiddenColumnWithCustomRenderer_toggleHiddenTwiceInRoundTrip_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyHiddenColumn_toggleHiddenTwiceInRoundTrip_assertRendererNotCalled(
                column);
    }

    @Test
    public void columnWithValueProvider_detachAndReattachGrid_rendererCalledOncePerItem() {
        addColumnWithValueProvider();
        initiallyVisibleColumn_detachAndReattachGrid_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenColumnWithValueProvider_detachAndReattachGrid_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithValueProvider();
        initiallyHiddenColumn_detachAndReattachGrid_assertRendererNotCalled(
                column);
    }

    @Test
    public void componentColumn_detachAndReattachGrid_rendererCalledOncePerItem() {
        addComponentColumn();
        initiallyVisibleColumn_detachAndReattachGrid_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenComponentColumn_detachAndReattachGrid_rendererNotCalled() {
        Grid.Column<String> column = addComponentColumn();
        initiallyHiddenColumn_detachAndReattachGrid_assertRendererNotCalled(
                column);
    }

    @Test
    public void columnWithCustomRenderer_detachAndReattachGrid_rendererCalledOncePerItem() {
        addColumnWithCustomRenderer();
        initiallyVisibleColumn_detachAndReattachGrid_assertRendererCalledOncePerItem();
    }

    @Test
    public void initiallyHiddenColumnWithCustomRenderer_detachAndReattachGrid_rendererNotCalled() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        initiallyHiddenColumn_detachAndReattachGrid_assertRendererNotCalled(
                column);
    }

    @Test
    public void columnWithCustomRenderer_setAnotherRenderer_onlyNewRendererCalled() {
        Grid.Column<String> column = addColumnWithCustomRenderer();
        fakeClientCommunication();
        callCount.set(0);
        AtomicInteger newRendererCallCount = new AtomicInteger(0);
        Renderer<String> newRenderer = LitRenderer
                .<String> of("<span>${item.displayName}</span>")
                .withProperty("displayName", s -> {
                    newRendererCallCount.incrementAndGet();
                    return s;
                });
        column.setRenderer(newRenderer);
        fakeClientCommunication();
        Assert.assertEquals(0, callCount.get());
        Assert.assertEquals(ITEM_COUNT, newRendererCallCount.get());
    }

    @Test
    public void addColumn_itemsSentOnlyOnce() {
        List<String> items = getItems();
        AtomicInteger fetchCount = new AtomicInteger(0);
        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
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

    private Grid.Column<String> addColumnWithValueProvider() {
        return grid.addColumn(getValueProvider());
    }

    private Grid.Column<String> addComponentColumn() {
        return grid.addComponentColumn(this::getComponent);
    }

    private Grid.Column<String> addColumnWithCustomRenderer() {
        return grid.addColumn(getCustomRenderer());
    }

    private void initiallyHiddenColumn_detachAndReattachGrid_assertRendererNotCalled(
            Grid.Column<String> column) {
        column.setVisible(false);
        ui.remove(grid);
        fakeClientCommunication();
        ui.add(grid);
        fakeClientCommunication();
        Assert.assertEquals(0, callCount.get());
    }

    private void initiallyVisibleColumn_detachAndReattachGrid_assertRendererCalledOncePerItem() {
        ui.remove(grid);
        fakeClientCommunication();
        callCount.set(0);
        ui.add(grid);
        fakeClientCommunication();
        Assert.assertEquals(ITEM_COUNT, callCount.get());
    }

    private void initiallyHiddenColumn_assertRendererNotCalled(
            Grid.Column<String> column) {
        column.setVisible(false);
        fakeClientCommunication();
        Assert.assertEquals(0, callCount.get());
    }

    private void initiallyVisibleColumn_assertRendererCalledOncePerItem() {
        fakeClientCommunication();
        Assert.assertEquals(ITEM_COUNT, callCount.get());
    }

    private void initiallyVisibleColumn_setHidden_assertRendererNotCalled(
            Grid.Column<String> column) {
        fakeClientCommunication();
        callCount.set(0);
        column.setVisible(false);
        fakeClientCommunication();
        Assert.assertEquals(0, callCount.get());
    }

    private void initiallyHiddenColumn_setVisible_assertRendererCalledOncePerItem(
            Grid.Column<String> column) {
        column.setVisible(false);
        fakeClientCommunication();
        column.setVisible(true);
        fakeClientCommunication();
        Assert.assertEquals(ITEM_COUNT, callCount.get());
    }

    private void initiallyVisibleColumn_toggleHiddenTwiceInRoundTrip_assertRendererCalledOncePerItem(
            Grid.Column<String> column) {
        fakeClientCommunication();
        column.setVisible(false);
        column.setVisible(true);
        callCount.set(0);
        fakeClientCommunication();
        Assert.assertEquals(ITEM_COUNT, callCount.get());
    }

    private void initiallyHiddenColumn_toggleHiddenTwiceInRoundTrip_assertRendererNotCalled(
            Grid.Column<String> column) {
        column.setVisible(false);
        fakeClientCommunication();
        column.setVisible(true);
        column.setVisible(false);
        fakeClientCommunication();
        Assert.assertEquals(0, callCount.get());
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
}
