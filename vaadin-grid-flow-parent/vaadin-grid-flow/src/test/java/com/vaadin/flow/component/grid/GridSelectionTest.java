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
package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.selection.SingleSelect;

/**
 * Unit tests for Grid selection.
 *
 * @author Vaadin Ltd.
 */
public class GridSelectionTest {

    @Test
    public void singleSelection_selectCurrent_noEvent() {
        Grid<String> grid = new Grid<>();
        grid.setItems("one", "two");

        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();
        singleSelect.setValue("one");

        singleSelect.addValueChangeListener(event -> Assert
                .fail("Selection change should not be triggered"));

        singleSelect.setValue(singleSelect.getValue());
    }

    @Test
    public void multiSelectionListeners() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);

        Set<String> oldSelection = new LinkedHashSet<>();
        Set<String> selection = new LinkedHashSet<>(
                Arrays.asList("0", "1", "2"));

        AtomicInteger selectionListenerCalled = new AtomicInteger();
        grid.addSelectionListener(event -> {
            selectionListenerCalled.incrementAndGet();
            Assert.assertEquals(selection, event.getAllSelectedItems());
        });

        AtomicInteger valueChangeListenerCalled = new AtomicInteger();
        grid.asMultiSelect().addValueChangeListener(event -> {
            valueChangeListenerCalled.incrementAndGet();
            Assert.assertEquals(oldSelection, event.getOldValue());
            Assert.assertEquals(selection, event.getValue());
        });

        AtomicInteger multiSelectionListenerCalled = new AtomicInteger();
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .addMultiSelectionListener(event -> {
                    multiSelectionListenerCalled.incrementAndGet();
                    Assert.assertEquals(oldSelection, event.getOldSelection());
                    Assert.assertEquals(selection, event.getNewSelection());

                    Set<String> oldCopy = new LinkedHashSet<>(oldSelection);
                    Set<String> copy = new LinkedHashSet<>(selection);
                    oldCopy.removeAll(copy);
                    Assert.assertEquals(oldCopy, event.getRemovedSelection());
                    oldCopy = new LinkedHashSet<>(oldSelection);
                    copy.removeAll(oldCopy);
                    Assert.assertEquals(copy, event.getAddedSelection());
                });

        grid.asMultiSelect().setValue(selection);

        oldSelection.addAll(selection);
        selection.clear();
        selection.addAll(Arrays.asList("10", "1", "5"));
        grid.asMultiSelect().setValue(selection);

        Assert.assertEquals(2, selectionListenerCalled.get());
        Assert.assertEquals(2, valueChangeListenerCalled.get());
        Assert.assertEquals(2, multiSelectionListenerCalled.get());
    }
}
