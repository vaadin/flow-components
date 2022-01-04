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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/toggle-visibility")
public class ToggleVisibilityIT extends AbstractComponentIT {

    @Test
    public void toggleVisibility_secondGridIsVisible() {
        open();

        waitForElementPresent(By.id("toggle-visibility-grid1"));
        checkLogsForErrors();

        GridElement grid1 = $(GridElement.class).id("toggle-visibility-grid1");
        Assert.assertEquals("Grid1 Item 0", grid1.getCell(0, 0).getText());

        WebElement toggle = findElement(By.id("toggle-visibility-button"));
        toggle.click();

        waitForElementPresent(By.id("toggle-visibility-grid2"));
        checkLogsForErrors();

        GridElement grid2 = $(GridElement.class).id("toggle-visibility-grid2");
        Assert.assertEquals("Grid2 Item 0", grid2.getCell(0, 0).getText());

        toggle.click();
        waitForElementPresent(By.id("toggle-visibility-grid1"));
        checkLogsForErrors();
    }

}
