/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/select-invisible-grid")
public class SelectAndMakeVisibleIT extends AbstractComponentIT {

    @Test
    public void selectRowAndMakeGridVisible() throws InterruptedException {
        open();

        $("button").id("select").click();
        checkLogsForErrors();

        Long selectedLength = (Long) getCommandExecutor().executeScript(
                "return arguments[0].selectedItems.length;",
                findElement(By.tagName("vaadin-grid")));
        Assert.assertEquals("Unexpected number of selected items", 1l,
                selectedLength.longValue());
    }
}
