/**
 * Copyright 2000-2024 Vaadin Ltd.
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

@TestPath("vaadin-grid/detailsGrid")
public class DetailsGridIT extends AbstractComponentIT {

    @Test
    public void openingMultipleDetails_navigatingAwayDoesntThrow() {
        open();

        GridElement grid = $(GridElement.class).first();

        assertAmountOfOpenDetails(grid, 0);

        grid.getCell(0, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Jorma", getDetailsElements(grid).get(0).getText());

        grid.getCell(1, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jarmo", getDetailsElements(grid).get(0).getText());

        grid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jethro", getDetailsElements(grid).get(0).getText());

        findElement(By.id("next")).click();

        Assert.assertEquals(
                "Expected to find one grid on new page. Encountered something unexpected.",
                1, $("vaadin-grid").all().size());
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }

    private List<WebElement> getDetailsElements(GridElement grid) {
        return grid.findElements(By.tagName("span"));
    }
}
