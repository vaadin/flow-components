/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Locale;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/item-details")
public class GridViewItemDetailsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridDetailsRowTests() {
        GridElement grid = $(GridElement.class).id("grid-with-details-row");
        scrollToElement(grid);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        WebElement detailsElement = grid
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

        Assert.assertTrue(hasCell(grid, "Person 1 Updated"));
    }

    @Test
    public void gridDetailsRowServerAPI() {
        GridElement grid = $(GridElement.class).id("grid-with-details-row-2");
        scrollToElement(grid);

        assertAmountOfOpenDetails(grid, 0);

        getCellContent(grid.getCell(1, 2)).click();
        assertAmountOfOpenDetails(grid, 1);
        assertThat(
                grid.findElement(By.className("custom-details"))
                        .getAttribute("innerHTML"),
                CoreMatchers.containsString("Hi! My name is <b>Person 2!</b>"));

        getCellContent(grid.getCell(3, 2)).click();
        assertAmountOfOpenDetails(grid, 2);

        getCellContent(grid.getCell(1, 2)).click();
        getCellContent(grid.getCell(3, 2)).click();
        assertThat("Details should be closed after clicking the button again",
                grid.findElement(By.className("custom-details"))
                        .getAttribute("innerHTML"),
                CoreMatchers.not(CoreMatchers
                        .containsString("Hi! My name is <b>Person 2!</b>")));
    }

    @Test
    public void openDetails_scrollToEnd_scrollToStart_detailsOpenedItemsCountDoesNotIncrease() {
        GridElement grid = $(GridElement.class).id("grid-with-details-row");

        // Open details
        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        // Check the detailsOpenedItems property (expected to be a list with one
        // item)
        Assert.assertEquals(1,
                ((List<?>) grid.getProperty("detailsOpenedItems")).size());

        // Scroll to end
        grid.scrollToRow(grid.getRowCount() - 1);

        // Scroll to start
        grid.scrollToRow(0);

        // Check that the detailsOpenedItems array does not increase / does not
        // contain duplicates
        Assert.assertEquals(1,
                ((List<?>) grid.getProperty("detailsOpenedItems")).size());
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }

    private WebElement getRow(TestBenchElement grid, int row) {
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
}
