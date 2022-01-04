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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.tests.AbstractComponentIT;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.WebElement;

import static com.vaadin.flow.component.grid.it.ItemClickListenerPage.GRID_FILTER_FOCUSABLE_HEADER;

@TestPath("vaadin-grid/item-click-listener")
public class ItemClickListenerIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void doubleClickGoesWithSingleClicks() throws InterruptedException {
        GridTRElement firstRow = grid.getRow(0);
        firstRow.doubleClick();

        Assert.assertEquals("foofoo", getClickMessage());

        String yCoord = getDoubleClickMessage();

        Assert.assertThat(Integer.parseInt(yCoord),
                CoreMatchers.allOf(
                        Matchers.greaterThan(firstRow.getLocation().getY()),
                        Matchers.lessThan(firstRow.getLocation().getY()
                                + firstRow.getSize().getHeight())));
    }

    @Test
    public void clickCheckboxInCell_noItemClickEventFired() {
        TestBenchElement checkbox = grid.getCell(0, 1).$("vaadin-checkbox")
                .first();
        checkbox.click();
        Assert.assertEquals("", getClickMessage());
    }

    @Test
    public void clickCell_clickCheckboxInCell_onlyOneClickEventFired() {
        grid.getCell(0, 0).click();
        TestBenchElement checkbox = grid.getCell(0, 1).$("vaadin-checkbox")
                .first();
        checkbox.click();
        Assert.assertEquals("foo", getClickMessage());
    }

    @Test
    public void doubleClickCheckboxInCell_noEventsFired() {
        TestBenchElement checkbox = grid.getCell(0, 1).$("vaadin-checkbox")
                .first();
        checkbox.doubleClick();
        Assert.assertEquals("", getClickMessage());
        Assert.assertEquals("", getDoubleClickMessage());
        Assert.assertEquals("", getColumnClickMessage());
    }

    @Test
    public void clickCell_columnNameAvailable() {
        grid.getCell(0, 0).click();
        Assert.assertEquals("Name", getColumnClickMessage());
    }

    @Test
    public void doubleClickCell_columnNameAvailable() {
        grid.getCell(0, 0).doubleClick();
        Assert.assertEquals("Name", getColumnDoubleClickMessage());
    }

    @Test
    public void clickDetailsCell_noItemClickEventFired() {
        waitUntil(driver -> grid
                .findElements(By.className("row-details")) != null);
        WebElement details = findElement(By.id("details-bar"));
        details.click();
        Assert.assertEquals("", getColumnClickMessage());
        Assert.assertEquals("", getColumnDoubleClickMessage());
        Assert.assertEquals("", getDoubleClickMessage());
        Assert.assertEquals("", getClickMessage());
    }

    @Test
    public void doubleClickDetailsCell_noItemClickEventFired() {
        waitUntil(driver -> grid
                .findElements(By.className("row-details")) != null);
        WebElement details = findElement(By.id("details-bar"));
        ((TestBenchElement) details).doubleClick();
        Assert.assertEquals("", getColumnClickMessage());
        Assert.assertEquals("", getColumnDoubleClickMessage());
        Assert.assertEquals("", getDoubleClickMessage());
        Assert.assertEquals("", getClickMessage());
    }

    // Regression test for this issue:
    // https://github.com/vaadin/flow-components/issues/2247
    @Test
    public void gridItemKeysChanged_whenFocusableHeaderElementClicked_shouldNotRaiseItemClickEvent() {
        open();

        // wait for grid to be loaded
        waitUntil(driver -> $(GridElement.class)
                .id(GRID_FILTER_FOCUSABLE_HEADER).getRowCount() > 0);

        GridElement gridElement = $(GridElement.class)
                .id(GRID_FILTER_FOCUSABLE_HEADER);

        // Select an item with specific key
        gridElement.select(0);

        // Trigger key change on grid items by filtering
        ButtonElement filterButton = $(ButtonElement.class).id("filterButton");
        ButtonElement clearFilterButton = $(ButtonElement.class)
                .id("clearFilterButton");
        filterButton.click();
        clearFilterButton.click();

        WebElement focusableHeader = gridElement
                .findElement(By.id("focusableHeader"));
        focusableHeader.click();

        TestBenchElement span = $("span").id("item-click-event-log");
        Assert.assertEquals("", span.getText());
    }

    private String getColumnDoubleClickMessage() {
        return findElement(By.id("columnDblClickMsg")).getText();
    }

    private String getColumnClickMessage() {
        return findElement(By.id("columnClickMsg")).getText();
    }

    private String getClickMessage() {
        return findElement(By.id("clickMsg")).getText();
    }

    private String getDoubleClickMessage() {
        return findElement(By.id("dblClickMsg")).getText();
    }

}
