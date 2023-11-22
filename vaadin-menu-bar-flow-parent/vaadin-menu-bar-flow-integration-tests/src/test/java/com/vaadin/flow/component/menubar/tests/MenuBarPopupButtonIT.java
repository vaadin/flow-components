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
package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-menu-bar/popup-button")
public class MenuBarPopupButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void clickingCloseButton_closesSubmenu() {
        var menuBar = $(MenuBarElement.class).first();
        menuBar.getButtons().get(0).click();
        verifyOpened();
        click("close-button");
        verifyClosed();
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(MenuBarPageIT.OVERLAY_TAG));
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(MenuBarPageIT.OVERLAY_TAG));
    }
}
