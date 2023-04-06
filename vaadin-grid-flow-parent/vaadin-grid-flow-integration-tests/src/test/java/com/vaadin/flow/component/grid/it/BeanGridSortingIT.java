
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/bean-grid-sorting")
public class BeanGridSortingIT extends AbstractComponentIT {

    @Test
    public void sortBornColumn_valuesAreSortedAsIntegers() {
        open();
        GridElement grid = $(GridElement.class).first();

        // Original values
        Assert.assertEquals("99", grid.getCell(0, 1).getText());
        Assert.assertEquals("1111", grid.getCell(1, 1).getText());
        Assert.assertEquals("1", grid.getCell(2, 1).getText());

        // Sort by ascending order
        grid.getHeaderCell(1).$("vaadin-grid-sorter").first().click();

        Assert.assertEquals("1", grid.getCell(0, 1).getText());
        Assert.assertEquals("99", grid.getCell(1, 1).getText());
        Assert.assertEquals("1111", grid.getCell(2, 1).getText());

        // Sort by descending order
        grid.getHeaderCell(1).$("vaadin-grid-sorter").first().click();

        Assert.assertEquals("1111", grid.getCell(0, 1).getText());
        Assert.assertEquals("99", grid.getCell(1, 1).getText());
        Assert.assertEquals("1", grid.getCell(2, 1).getText());
    }

}
