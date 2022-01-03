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
package com.vaadin.flow.component.grid.contextmenu;

import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-grid/dynamic-context-menu-grid")
public class DynamicContextMenuGridIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        verifyClosed();
    }

    @Test
    public void shouldNotOpenContextMenuWhenClickedOnRowWithPersonUnder30Years() {
        grid.getCell(25, 0).contextClick();
        verifyClosed();
    }

    @Test
    public void shouldOpenContextMenuWhenClickedOnRowWithPersonAbove30Years() {
        grid.getCell(40, 0).contextClick();
        verifyOpened();

        Assert.assertEquals("Person 40",
                $(OVERLAY_TAG).first().getAttribute("innerText"));

        $("body").first().click();
        verifyClosed();
    }

    @Test
    public void shouldOpenContextMenuWhenClickingOnTheEdgeOfCell() {
        GridTHTDElement cell = grid.getCell(40, 0);
        // Move cursor to upper edge of the cell
        // moveToElement moves to center, so we subtract half of the height to
        // approximately get to the cells edge
        Rectangle cellRectangle = cell.getWrappedElement().getRect();
        int offsetToCellStart = (int) Math
                .ceil((float) cellRectangle.height / 2);
        (new Actions(this.getDriver()))
                .moveToElement(cell, 0, -offsetToCellStart).contextClick()
                .build().perform();

        verifyOpened();
        Assert.assertEquals("Person 40",
                $(OVERLAY_TAG).first().getAttribute("innerText"));
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }
}
