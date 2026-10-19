/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/toggle-visibility")
public class ToggleVisibilityIT extends AbstractComponentIT {

    @Test
    public void toggleVisibility_visibleGridIsRendered() {
        open();

        Assert.assertEquals("Grid1 Item 0", getGrid1().getCell(0, 0).getText());

        $("button").id("toggle-visibility-button").click();
        Assert.assertEquals("Grid2 Item 0", getGrid2().getCell(0, 0).getText());
        Assert.assertFalse(getGrid1().isDisplayed());

        $("button").id("toggle-visibility-button").click();
        Assert.assertEquals("Grid1 Item 0", getGrid1().getCell(0, 0).getText());
        checkLogsForErrors();
    }

    private GridElement getGrid1() {
        return $(GridElement.class).id("toggle-visibility-grid1");
    }

    private GridElement getGrid2() {
        return $(GridElement.class).id("toggle-visibility-grid2");
    }
}
