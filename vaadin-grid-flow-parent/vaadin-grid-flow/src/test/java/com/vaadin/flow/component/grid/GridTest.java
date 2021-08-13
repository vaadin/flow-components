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

package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;

public class GridTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void templateWarningSuppressed() {
        Grid<String> grid = new Grid<>();

        Assert.assertTrue("Template warning is not suppressed",
                grid.getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage(
                "GridListDataView only supports 'ListDataProvider' "
                        + "or it's subclasses, but was given a "
                        + "'AbstractBackEndDataProvider'");

        Grid<String> grid = new Grid<>();
        final GridListDataView<String> listDataView = grid
                .setItems(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider.fromCallbacks(
                query -> Arrays.asList("one").stream(), query -> 1);

        grid.setDataProvider(dataProvider);

        grid.getListDataView();
    }

    @Test
    public void selectItem_lazyDataSet_selectionWorks() {
        final Grid<String> grid = new Grid<>();
        grid.setItems(query -> Stream.of("foo", "bar"));
        grid.select("foo");
        Assert.assertEquals(1, grid.getSelectedItems().size());
        Assert.assertTrue(grid.getSelectedItems().contains("foo"));
    }

    @Test
    public void setHeightByRows_allRowsAreVisible() {
        final Grid<String> grid = new Grid<>();

        Assert.assertEquals(null,
                grid.getElement().getProperty("allRowsVisible"));

        grid.setHeightByRows(true);
        Assert.assertEquals("true",
                grid.getElement().getProperty("allRowsVisible"));
    }

    @Test
    public void setAllRowsVisible_allRowsAreVisible() {
        final Grid<String> grid = new Grid<>();

        Assert.assertEquals(null,
                grid.getElement().getProperty("allRowsVisible"));

        grid.setAllRowsVisible(true);
        Assert.assertEquals("true",
                grid.getElement().getProperty("allRowsVisible"));
    }

    @Test
    public void setAllRowsVisibleProperty_isHeightByRowsAndIsAllRowsVisibleWork() {
        final Grid<String> grid = new Grid<>();
        grid.getElement().setProperty("allRowsVisible", true);

        Assert.assertTrue(grid.isHeightByRows());
        Assert.assertTrue(grid.isAllRowsVisible());
    }

    @Test
    public void dataProviderListeners_gridAttached_oldDataProviderListenerRemoved() {
        AtomicInteger listenersCount = new AtomicInteger(0);
        AtomicBoolean oldListenerRemoved = new AtomicBoolean();

        // given
        Grid<String> grid = new Grid<>();
        ListDataProvider<String> dataProvider = new ListDataProvider<String>(
                Collections.emptyList()) {
            @Override
            public Registration addDataProviderListener(
                    DataProviderListener<String> listener) {
                listenersCount.incrementAndGet();
                Registration registration = super.addDataProviderListener(
                        listener);
                // the first listener is added by Grid in 'setDataProvider'
                // and it is the one we want to be removed.
                return listenersCount.get() == 1
                        ? Registration.combine(registration,
                                () -> oldListenerRemoved.set(true))
                        : registration;
            }
        };

        // when
        grid.setDataProvider(dataProvider);

        // then
        Assert.assertEquals(
                "Expected exactly two data provider listeners added by "
                        + "DataCommunicator's and Grid's 'setDataProvider' methods",
                2, listenersCount.get());
        Assert.assertFalse(
                "Expected data provider listener added by Grid not to be "
                        + "removed before Grid being attached",
                oldListenerRemoved.get());

        // given
        listenersCount.set(0);

        // when
        DataCommunicatorTest.MockUI mockUI = new DataCommunicatorTest.MockUI();
        mockUI.add(grid);
        fakeClientCommunication(mockUI);

        // then
        Assert.assertEquals(
                "Expected exactly two data provider listeners added by "
                        + "DataCommunicator's and Grid's attach handle methods",
                2, listenersCount.get());
        Assert.assertTrue(
                "Expected old data provider listener to be removed after "
                        + "grid being attached",
                oldListenerRemoved.get());
    }

    private void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
