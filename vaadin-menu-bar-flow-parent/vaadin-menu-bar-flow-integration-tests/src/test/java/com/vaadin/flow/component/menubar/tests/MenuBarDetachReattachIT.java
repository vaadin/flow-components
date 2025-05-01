/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/detach-reattach")
public class MenuBarDetachReattachIT extends AbstractComponentIT {

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).waitForFirst();
    }

    @Test
    public void detach_reattach_noClientErrors_clientCodeFunctional() {
        click("toggle-attached");
        click("toggle-attached");
        waitForElementPresent(By.tagName("vaadin-menu-bar"));
        checkLogsForErrors();

        // Verify client-code with setVisible functionality:
        menuBar = $(MenuBarElement.class).first();
        click("toggle-item-2-visibility");
        assertButtonContents("item 1");
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        click("set-width");

        waitForResizeObserver();

        MenuBarButtonElement overflowButton = menuBar.getOverflowButton();

        Assert.assertEquals("More options",
                overflowButton.getDomAttribute("aria-label"));

        click("set-i18n");

        Assert.assertEquals("more-options",
                overflowButton.getDomAttribute("aria-label"));
    }

    @Test
    public void setI18n_detach_attach_i18nIsPersisted() {
        click("set-width");

        waitForResizeObserver();

        click("set-i18n");

        MenuBarButtonElement overflowButton = menuBar.getOverflowButton();

        Assert.assertEquals("more-options",
                overflowButton.getDomAttribute("aria-label"));

        click("toggle-attached");
        click("toggle-attached");

        menuBar = $(MenuBarElement.class).first();
        overflowButton = menuBar.getOverflowButton();

        Assert.assertEquals("more-options",
                overflowButton.getDomAttribute("aria-label"));
    }

    private void assertButtonContents(String... expectedInnerHTML) {
        String[] contents = menuBar.getButtons().stream().map(button -> button
                .$("vaadin-menu-bar-item").first().getDomProperty("innerHTML"))
                .toArray(String[]::new);
        Assert.assertArrayEquals(expectedInnerHTML, contents);
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    private void waitForResizeObserver() {
        getCommandExecutor().getDriver()
                .executeAsyncScript("requestAnimationFrame(arguments[0])");
    }
}
