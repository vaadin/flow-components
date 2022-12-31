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

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.grid.it.DetachedGridWithPreserveOnRefreshPage.ADD;
import static com.vaadin.flow.component.grid.it.DetachedGridWithPreserveOnRefreshPage.GRID;
import static com.vaadin.flow.component.grid.it.DetachedGridWithPreserveOnRefreshPage.REMOVE;

// Regression test for https://github.com/vaadin/flow/issues/14435
@TestPath("vaadin-grid/detached-grid-with-preserve-on-refresh")
public class DetachedGridWithPreserveOnRefreshIT extends AbstractComponentIT {

    @Test
    public void requestFlush_gridDetached_pageRefreshed_gridAttached_shouldRequestFlushWithoutInfiniteLoop() {
        open();

        // Add Grid to the page
        waitForElementPresent(By.id(ADD));
        findElement(By.id(ADD)).click();

        waitForElementPresent(By.id(GRID));

        // Detach Grid
        findElement(By.id(REMOVE)).click();

        waitForElementNotPresent(By.id(GRID));

        // Refresh the page to make Grid create and store a new flushRequest
        open();

        // Add the same Grid again
        waitForElementPresent(By.id(ADD));
        findElement(By.id(ADD)).click();

        // Wait for Grid to be rendered without loops in requestFlush
        waitForElementPresent(By.id(GRID));
    }
}
