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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/click-listeners")
public class GridViewClickListenersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void itemClickListener_singleClick_doubleClickFireClick() {
        GridElement grid = $(GridElement.class).id("item-click-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        row.click(10, 10);

        WebElement clickInfo = findElement(By.id("clicked-item"));

        Assert.assertEquals("Clicked Item: Person 1", clickInfo.getText());

        // Clear the message
        clickInfo.click();
        // self check
        Assert.assertEquals("", clickInfo.getText());

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        headerCell.click(10, 10);

        // No event
        Assert.assertEquals("", clickInfo.getText());
    }

    @Test
    public void itemClickListener_singleClick_preventClickIgnored() {
        GridElement grid = $(GridElement.class).id("item-click-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        GridTHTDElement cell = row.getCell(grid.getColumn("Action"));

        WebElement span = cell.getContext().findElement(By.tagName("span"));
        span.click();

        WebElement clickInfo = findElement(By.id("clicked-item"));

        Assert.assertEquals("", clickInfo.getText());
    }

    @Test
    public void itemClickListener_singleClick_focusableElementClickIgnored() {
        GridElement grid = $(GridElement.class).id("item-click-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        GridTHTDElement cell = row.getCell(grid.getColumn("Button"));

        WebElement icon = cell.getContext()
                .findElement(By.tagName("vaadin-icon"));
        icon.click();

        WebElement clickInfo = findElement(By.id("clicked-item"));

        Assert.assertEquals("", clickInfo.getText());
    }

    @Test
    public void itemDoubleClickListener() {
        GridElement grid = $(GridElement.class).id("item-doubleclick-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        row.doubleClick();

        WebElement clickInfo = findElement(By.id("doubleclicked-item"));

        Assert.assertEquals("Double Clicked Item: Person 1",
                clickInfo.getText());

        // Clear the message
        clickInfo.click();
        // self check
        Assert.assertEquals("", clickInfo.getText());

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        headerCell.doubleClick();

        // No event
        Assert.assertEquals("", clickInfo.getText());
    }
}
