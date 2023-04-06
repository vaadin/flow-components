
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("allow-nested-nulls")
public class GridAllowNestedNullsIT extends AbstractComponentIT {

    @Test
    public void addGridAllowNulls() {
        open();
        findElement(By.id("null-allowed")).click();
        GridElement grid = $(GridElement.class).first();
        Assert.assertNotNull(grid);
    }

    @Test(expected = NoSuchElementException.class)
    public void addGridThrowNulls() {
        open();
        findElement(By.id("null-thrown")).click();
        GridElement grid = $(GridElement.class).first();
    }

}
