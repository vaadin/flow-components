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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-serialization-page")
public class GridSerializationIT extends AbstractComponentIT {

    @Test
    public void toStringIsUsedForObjectSerialization() {
        open();

        GridElement grid = $(GridElement.class).id("grid");

        Assert.assertEquals("2018-04-01", getCellText(grid, "Local Date"));
        Assert.assertEquals("69", getCellText(grid, "Integer"));
        Assert.assertEquals("1", getCellText(grid, "Id"));
        Assert.assertEquals("foo", getCellText(grid, "Value"));
        Assert.assertEquals("2018-04-01", getCellText(grid, "Date 2"));
        Assert.assertEquals("69", getCellText(grid, "Int 2"));
        Assert.assertEquals("Person with id 1", getCellText(grid, "Object"));
    }

    private String getCellText(GridElement grid, String columnHeader) {
        GridTRElement row = grid.getRow(0);
        return row.getCell(grid.getColumn(columnHeader)).getInnerHTML();
    }

}
