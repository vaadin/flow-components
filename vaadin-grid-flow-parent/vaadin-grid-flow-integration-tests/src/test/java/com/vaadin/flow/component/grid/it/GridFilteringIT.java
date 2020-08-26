/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-filtering")
public class GridFilteringIT extends AbstractComponentIT {

    @Test
    public void gridInNotLoadingState() {
        open();

        WebElement filter = $("vaadin-text-field").id("filter");
        WebElement input = getInShadowRoot(filter, By.cssSelector("input"));
        input.sendKeys("w");

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        WebElement grid = findElement(By.id("data-grid"));
        // empty Grid content
        Object size = executeScript("return arguments[0].size", grid);
        Assert.assertEquals("0", size.toString());

        input.sendKeys(Keys.BACK_SPACE);

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        waitUntil(driver -> executeScript("return arguments[0].size", grid)
                .toString().equals("3"));

        waitUntil(driver -> "false".equals(grid.getAttribute("loading")));
    }
}
