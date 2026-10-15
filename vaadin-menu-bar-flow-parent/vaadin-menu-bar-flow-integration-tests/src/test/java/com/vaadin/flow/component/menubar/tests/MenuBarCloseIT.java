/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/close")
public class MenuBarCloseIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void clickingCloseButton_closesSubmenu() {
        var menuBar = $(MenuBarElement.class).first();
        menuBar.getButtons().get(0).click();
        verifyOpened();
        clickElementWithJs("close-button");
        verifyClosed();
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(MenuBarPageIT.OVERLAY_TAG));
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(MenuBarPageIT.OVERLAY_TAG));
    }
}
