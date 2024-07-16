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

@TestPath("vaadin-grid/text-component-renderer")
public class TextInsideComponentRendererIT extends AbstractComponentIT {

    @Test
    public void renderGrid_noClientSideExceptions() {
        open();

        Assert.assertFalse(isElementPresent(By.className("v-system-error")));
        checkLogsForErrors();
    }
}
