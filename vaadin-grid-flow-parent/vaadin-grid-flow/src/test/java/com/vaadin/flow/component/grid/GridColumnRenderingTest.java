/*
 * Copyright 2000-2024 Vaadin Ltd.
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

package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridColumnRenderingTest {

    @Test
    public void defaultColumnRendering() {
        var grid = new Grid<String>();
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
    }

    @Test
    public void setColumnRendering_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(ColumnRendering.LAZY);
        Assert.assertEquals(ColumnRendering.LAZY, grid.getColumnRendering());
        Assert.assertEquals("lazy",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingNull_defaultPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(null);
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
        Assert.assertEquals("eager",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingFromLazyToNull_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(ColumnRendering.LAZY);
        grid.setColumnRendering(null);
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
        Assert.assertEquals("eager",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingFromNullToLazy_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(null);
        grid.setColumnRendering(ColumnRendering.LAZY);
        Assert.assertEquals(ColumnRendering.LAZY, grid.getColumnRendering());
        Assert.assertEquals("lazy",
                grid.getElement().getProperty("columnRendering"));
    }
}
