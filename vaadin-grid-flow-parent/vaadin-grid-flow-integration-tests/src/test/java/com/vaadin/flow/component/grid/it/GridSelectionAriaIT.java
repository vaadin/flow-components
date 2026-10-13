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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/selection-aria")
public class GridSelectionAriaIT extends AbstractComponentIT {

    private GridElement multiGrid;
    private GridElement noneGrid;

    @Before
    public void init() {
        open();
        multiGrid = $(GridElement.class).id("multi-selection");
        noneGrid = $(GridElement.class).id("none-selection");
    }

    @Test
    public void multiSelect_selectFromServerAndClient_eventsAreFired() {
        TestBenchElement selectButton = $("button")
                .id("multi-selection-button");
        TestBenchElement message = $("div").id("multi-selection-message");

        selectButton.click();
        Assert.assertEquals(
                "[Person 1, Person 2] -> [Person 1, Person 2, "
                        + "Person 3, Person 4, Person 5], from client: false",
                message.getText());
        assertRowsSelected(0, 5);

        multiGrid.deselect(0);
        multiGrid.deselect(1);
        Assert.assertEquals(
                "[Person 2, Person 3, Person 4, Person 5] -> "
                        + "[Person 3, Person 4, Person 5], from client: true",
                message.getText());
        assertRowsSelected(2, 5);

        multiGrid.select(5);
        selectButton.click();
        assertRowsSelected(0, 6);
        Assert.assertFalse(multiGrid.getRow(6).isSelected());
    }

    @Test
    public void multiSelect_ariaSelectionAttributes() {
        TestBenchElement table = multiGrid.$("table").first();
        Assert.assertEquals("true",
                table.getDomAttribute("aria-multiselectable"));

        Assert.assertEquals("true",
                multiGrid.getRow(0).getDomAttribute("aria-selected"));
        Assert.assertEquals("true",
                multiGrid.getRow(1).getDomAttribute("aria-selected"));
        Assert.assertEquals("false",
                multiGrid.getRow(2).getDomAttribute("aria-selected"));

        multiGrid.select(2);
        Assert.assertEquals("true",
                multiGrid.getRow(2).getDomAttribute("aria-selected"));
    }

    @Test
    public void noneSelect_noAriaSelectionAttributes() {
        TestBenchElement table = noneGrid.$("table").first();
        Assert.assertFalse(table.hasAttribute("aria-multiselectable"));

        for (GridTRElement row : noneGrid.getVisibleRows()) {
            Assert.assertFalse(row.hasAttribute("aria-selected"));
            Assert.assertFalse(row.$("td").all().stream()
                    .anyMatch(cell -> cell.hasAttribute("aria-selected")));
        }
    }

    @Test
    public void noneSelect_clickRow_rowIsNotSelected() {
        noneGrid.getCell(1, 0).click();

        Assert.assertFalse(noneGrid.getRow(1).isSelected());
    }

    private void assertRowsSelected(int first, int last) {
        IntStream.range(first, last)
                .forEach(rowIndex -> Assert.assertTrue(
                        "Row " + rowIndex + " should be selected",
                        multiGrid.getRow(rowIndex).isSelected()));
    }
}
