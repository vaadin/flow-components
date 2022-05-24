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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/component-column-height")
public class ComponentColumnWithHeightIT extends AbstractComponentIT {

    private GridElement grid;
    private WebElement add;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        add = $("button").first();
    }

    @Test
    public void shouldPositionItemsCorrectlyAfterUpdatingComponentRenderers() {
        add.click();
        // Expect the y position of the second row to equal the y position + the
        // height of the first row
        Assert.assertEquals(
                grid.getRow(0).getLocation().y
                        + grid.getRow(0).getSize().height,
                grid.getRow(1).getLocation().y);
    }
}
