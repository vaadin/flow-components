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

@TestPath("vaadin-grid/column-path")
public class ColumnPathIT extends AbstractComponentIT {

    @Test
    public void columnsUsePathPropertyWhenApplicable() {
        open();

        GridElement grid = $(GridElement.class).waitForFirst();

        Assert.assertEquals("Person 1", grid.getCell(0, 0).getText());
        Assert.assertEquals("Person 1", grid.getCell(0, 1).getText());

        // A column with an editor contains lot's of stuff, so let's just check
        // if the innerHTML contains Person 1
        Assert.assertTrue(grid.getCell(0, 2).getText().contains("Person 1"));

        Assert.assertEquals("Person 1", grid.getCell(0, 3).getText());

        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));
        Assert.assertNotNull(
                "The path property shouldn't be undefined for column 0",
                getPath(columns.get(0)));

        Assert.assertNull("The path property should be undefined for column 1",
                getPath(columns.get(1)));

        Assert.assertNotNull(
                "The path property shouldn't be undefined for column 2",
                getPath(columns.get(2)));

        Assert.assertNotNull(
                "The path property shouldn't be undefined for column 3",
                getPath(columns.get(3)));
    }

    private String getPath(WebElement col) {
        return (String) executeScript("return arguments[0].path", col);
    }

}
