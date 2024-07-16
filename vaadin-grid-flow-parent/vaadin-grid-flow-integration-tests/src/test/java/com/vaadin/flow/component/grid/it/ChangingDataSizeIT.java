/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/changing-data-size")
public class ChangingDataSizeIT extends AbstractComponentIT {

    @Test
    public void scrollDown_dataSizeChanges_sizeIsUpdated() {
        open();
        waitForElementPresent(By.id("grid"));

        GridElement grid = $(GridElement.class).id("grid");

        Assert.assertEquals(133, grid.getRowCount());
        Assert.assertEquals("Item 0", grid.getCell(0, 0).getText());

        // At this point, the 1st and 2nd pages are fetched (0-100), and Grid
        // reported the size of being 133. Clicking on the button affects the
        // DataProvider without refreshing anything.
        findElement(By.id("remove-items")).click();

        // Now if we scroll to the last presumed row, the items will be fetched,
        // and the size should be updated
        grid.scrollToRow(132);

        Assert.assertEquals(123, grid.getRowCount());
        Assert.assertEquals("Item 122", grid.getCell(122, 0).getText());
    }

}
