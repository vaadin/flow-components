/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-empty")
public class GridEmptyIT extends AbstractComponentIT {

    @Test
    public void emptyGrid_clearCache_loadingStateCleared() {
        open();

        // Force data provider request by clearing the grid's cache
        ButtonElement clearCache = $(ButtonElement.class)
                .id("clear-cache-button");
        clearCache.click();

        WebElement grid = findElement(By.id("empty-grid"));
        waitUntil(driver -> "false".equals(grid.getAttribute("loading")));
    }

}
