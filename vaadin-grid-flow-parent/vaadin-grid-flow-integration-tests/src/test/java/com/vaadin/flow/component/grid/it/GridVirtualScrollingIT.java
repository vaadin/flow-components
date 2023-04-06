
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-virtual-scrolling")
public class GridVirtualScrollingIT extends AbstractComponentIT {

    @Test
    public void gridScrolling() {
        open();

        waitForElementPresent(By.tagName("vaadin-grid"));
        WebElement grid = findElement(By.tagName("vaadin-grid"));

        executeScript("arguments[0].scrollToIndex(1000000);", grid);

        long firstVisible = (long) executeScript(
                "return arguments[0]._firstVisibleIndex", grid);
        Assert.assertEquals(1000000L, firstVisible);
    }

}
