/*
 * Copyright 2000-2022 Vaadin Ltd.
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
