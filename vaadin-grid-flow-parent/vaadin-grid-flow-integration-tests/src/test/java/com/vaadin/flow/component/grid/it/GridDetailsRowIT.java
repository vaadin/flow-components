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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@TestPath("vaadin-grid/grid-details-row")
public class GridDetailsRowIT extends AbstractComponentIT {

    @Test
    public void gridTwoItemsSelectedWhenOpen() {
        open();
        GridElement grid = $(GridElement.class).first();

        // each detail contain a button
        List<WebElement> detailsElements = getDetailsElements(grid);

        Assert.assertEquals(2, detailsElements.size());

        Assert.assertEquals("Person 1", detailsElements.get(0).getText());
        Assert.assertEquals("Person 2", detailsElements.get(1).getText());
    }

    @Test
    public void shouldNotThrowOnDetailsClick() {
        open();
        GridElement grid = $(GridElement.class).first();
        grid.getRow(1).getDetails().click(0, 0);
        checkLogsForErrors();
    }

    /**
     * Click on an item, hide the other details
     */
    @Test
    public void gridSelectItem4DisplayDetails() {
        open();
        GridElement grid = $(GridElement.class).first();
        // select row 4
        grid.getCell(3, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        // detail on row 3 has the correct text
        Assert.assertEquals("Person 4",
                getDetailsElements(grid).get(0).getText());
    }

    /**
     * If the details of an item is opened and the item updated then the detail
     * should be updated
     */
    @Test
    public void gridUpdateItemUpdateDetails() {
        open();
        GridElement grid = $(GridElement.class).first();
        // select row 3
        grid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        // detail on row 3 has the correct text
        Assert.assertEquals("Person 3",
                getDetailsElements(grid).get(0).getText());

        WebElement updateButton = findElement(By.id("update-button"));
        updateButton.click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        // detail on row 3 has the correct text
        Assert.assertEquals("Person 3 - updates 1",
                getDetailsElements(grid).get(0).getText());

    }

    private List<WebElement> getDetailsElements(GridElement grid) {
        return grid.findElements(By.tagName("vaadin-button"));
    }
}
