/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-details-row")
public class GridDetailsRowIT extends AbstractComponentIT {
    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void initiallyOpenedDetailsDisplayed() {
        List<WebElement> detailsElements = getDetailsElements();
        Assert.assertEquals(2, detailsElements.size());
        Assert.assertEquals("Person 0", detailsElements.get(0).getText());
        Assert.assertEquals("Person 1", detailsElements.get(1).getText());
    }

    @Test
    public void clickDetails_doesNotThrow() {
        grid.getRow(1).getDetails().click(0, 0);
        checkLogsForErrors();
    }

    @Test
    public void selectItem_onlyItsDetailsAreDisplayed() {
        grid.getCell(2, 0).click();

        List<WebElement> detailsElements = getDetailsElements();
        Assert.assertEquals(1, detailsElements.size());
        Assert.assertEquals("Person 2", detailsElements.get(0).getText());
    }

    @Test
    public void updateItem_detailsUpdated() {
        grid.getCell(2, 0).click();

        GridTHTDElement details = grid.getRow(2).getDetails();

        Assert.assertFalse(details.hasAttribute("hidden"));
        Assert.assertEquals("Person 2", details.getText());

        findElement(By.id("update-person-2")).click();

        Assert.assertFalse(details.hasAttribute("hidden"));
        Assert.assertEquals("Updated Person 2", details.getText());
    }

    @Test
    public void removeItem_detailsRemoved() {
        grid.getCell(2, 0).click();
        findElement(By.id("remove-person-2")).click();

        GridTHTDElement details = grid.getRow(1).getDetails();
        Assert.assertTrue(details.hasAttribute("hidden"));
    }

    private List<WebElement> getDetailsElements() {
        return grid.findElements(By.tagName("vaadin-button"));
    }
}
