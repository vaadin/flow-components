/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractContextMenuIT {

    @Test
    public void autoAttachedContextMenuWithPreserveOnRefresh_refresh_noClientErrors_menuRendered() {
        open();
        waitForElementPresent(By.id("target"));

        getDriver().navigate().refresh();
        waitForElementPresent(By.id("target"));

        checkLogsForErrors();

        rightClickOn("target");
        Assert.assertArrayEquals(new String[] { "foo" }, getMenuItemCaptions());
    }
}
