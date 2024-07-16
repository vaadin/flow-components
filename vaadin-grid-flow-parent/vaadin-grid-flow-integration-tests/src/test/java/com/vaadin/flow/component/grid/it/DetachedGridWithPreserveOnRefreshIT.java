/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
