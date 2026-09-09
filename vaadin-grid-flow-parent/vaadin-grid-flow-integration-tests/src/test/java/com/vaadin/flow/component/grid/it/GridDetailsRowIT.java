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
import java.util.Locale;

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

        // Clicking a row closes the initially-opened details asynchronously
        waitUntil(driver -> getDetailsElements().size() == 1);
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

    @Test
    public void litRendererDetails_clickRow_detailsRenderedAndUpdatable() {
        GridElement detailsGrid = $(GridElement.class)
                .id("grid-with-details-row");
        scrollToElement(detailsGrid);

        clickElementWithJs(
                getInternalRow(detailsGrid, 0).findElement(By.tagName("td")));

        WebElement detailsElement = detailsGrid
                .findElement(By.className("custom-details"));

        List<WebElement> children = detailsElement
                .findElements(By.tagName("div"));
        Assert.assertEquals(2, children.size());

        Assert.assertEquals("div",
                children.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Hi! My name is Person 1!",
                children.get(0).getText());

        Assert.assertEquals("div",
                children.get(1).getTagName().toLowerCase(Locale.ENGLISH));

        WebElement button = children.get(1).findElement(By.tagName("button"));
        Assert.assertEquals("Update Person", button.getText());

        clickElementWithJs(detailsElement.findElement(By.tagName("button")));

        Assert.assertTrue(hasCell(detailsGrid, "Person 1 Updated"));
    }

    @Test
    public void detailsVisibleOnClickDisabled_multipleDetailsOpenedAndClosedViaApi() {
        GridElement detailsGrid = $(GridElement.class)
                .id("grid-with-details-row-2");
        scrollToElement(detailsGrid);

        assertAmountOfOpenDetails(detailsGrid, 0);

        getCellContent(detailsGrid.getCell(1, 2)).click();
        assertAmountOfOpenDetails(detailsGrid, 1);
        Assert.assertTrue(
                detailsGrid.findElement(By.className("custom-details"))
                        .getText().contains("Hi! My name is Person 2!"));

        getCellContent(detailsGrid.getCell(3, 2)).click();
        assertAmountOfOpenDetails(detailsGrid, 2);

        getCellContent(detailsGrid.getCell(1, 2)).click();
        getCellContent(detailsGrid.getCell(3, 2)).click();
        Assert.assertFalse(
                "Details should be closed after clicking the button again",
                detailsGrid.findElement(By.className("custom-details"))
                        .getText().contains("Hi! My name is Person 2!"));
    }

    private WebElement getInternalRow(GridElement grid, int row) {
        return grid.$("*").id("items").findElements(By.cssSelector("tr"))
                .get(row);
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

    private boolean hasCell(GridElement grid, String text) {
        return grid.getCell(text) != null;
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }
}
