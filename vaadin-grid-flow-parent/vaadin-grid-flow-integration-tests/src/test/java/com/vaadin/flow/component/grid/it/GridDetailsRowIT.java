/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
