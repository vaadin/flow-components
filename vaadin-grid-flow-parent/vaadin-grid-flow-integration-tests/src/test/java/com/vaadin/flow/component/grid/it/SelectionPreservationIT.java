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
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/selection-preservation")
public class SelectionPreservationIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).single();
    }

    @Test
    public void multiSelect_preserveAll_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-multi-select");
        clickElementWithJs("mode-preserve-all");

        grid.select(4);
        grid.select(5);
        grid.select(6);

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of(4, 5, 6), getSelectedRowIndices());
    }

    @Test
    public void multiSelect_preserveExisting_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-multi-select");
        clickElementWithJs("mode-preserve-existing");

        grid.select(2);
        grid.select(7);

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of(2, 7), getSelectedRowIndices());
    }

    @Test
    public void multiSelect_preserveAll_removeSelectedItem_refreshAll_serverValueStillContainsRemovedItem() {
        clickElementWithJs("mode-multi-select");
        clickElementWithJs("mode-preserve-all");

        grid.select(4);
        grid.select(5);
        grid.select(6);

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,5,6", getServerValue());
        Assert.assertEquals(List.of(4, 5), getSelectedRowIndices());
    }

    @Test
    public void multiSelect_preserveExisting_removeSelectedItem_refreshAll_removedItemDeselected() {
        clickElementWithJs("mode-multi-select");
        clickElementWithJs("mode-preserve-existing");

        grid.select(4);
        grid.select(5);
        grid.select(6);

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,6", getServerValue());
        Assert.assertEquals(List.of(4, 5), getSelectedRowIndices());
    }

    @Test
    public void multiSelect_discard_selectMultiple_refreshAll_selectionCleared() {
        clickElementWithJs("mode-multi-select");
        clickElementWithJs("mode-discard");

        grid.select(1);
        grid.select(3);

        clickElementWithJs("refresh-all");

        Assert.assertTrue(getSelectedRowIndices().isEmpty());
    }

    @Test
    public void singleSelect_preserveAll_selectItem_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-single-select");
        clickElementWithJs("mode-preserve-all");

        grid.select(5);

        clickElementWithJs("refresh-all");

        Assert.assertTrue(grid.getRow(5).isSelected());
    }

    @Test
    public void singleSelect_preserveAll_removeSelectedItem_refreshAll_serverValueStillPreserved() {
        clickElementWithJs("mode-single-select");
        clickElementWithJs("mode-preserve-all");

        grid.select(5);

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("5", getServerValue());
    }

    private String getServerValue() {
        clickElementWithJs("show-server-value");
        return $("span").id("server-value").getText();
    }

    private List<Integer> getSelectedRowIndices() {
        return IntStream.range(0, grid.getRowCount())
                .filter(i -> grid.getRow(i).isSelected()).boxed().toList();
    }
}
