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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/using-templates")
public class GridViewUsingTemplatesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridWithColumnTemplate() {
        GridElement grid = $(GridElement.class).id("template-renderer");
        scrollToElement(grid);

        Assert.assertEquals("0", grid.getCell(0, 0).getText());
        Assert.assertEquals(
                "<div title=\"Person 1\">Person 1<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
        Assert.assertEquals(
                "<div>Street S, number 49<br><small>10795</small></div>",
                grid.getCell(0, 2).getInnerHTML());
        Assert.assertEquals("<button>Update</button><button>Remove</button>",
                grid.getCell(0, 3).getInnerHTML());

        List<TestBenchElement> buttons = grid.getCell(0, 3).$("button").all();
        Assert.assertEquals(2, buttons.size());

        buttons.get(0).click();
        Assert.assertEquals(
                "<div title=\"Person 1 Updated\">Person 1 Updated<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
        buttons.get(0).click();
        Assert.assertEquals(
                "<div title=\"Person 1 Updated Updated\">Person 1 Updated Updated<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());

        buttons.get(1).click();
        Assert.assertEquals(
                "<div title=\"Person 2\">Person 2<br><small>61 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
    }

}
