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
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/auto-attached-context-menu")
public class AutoAttachedContextMenuIT extends AbstractContextMenuIT {

    public static final String TARGET_ID = "target-for-not-attached-context-menu";
    private final String MENU_ID = "not-attached-context-menu";

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("label"));
        checkLogsForErrors();
    }

    @Test
    public void contextMenuNotAttachedToThePage_openAndClose_contextMenuIsAttachedAndRemoved() {
        waitForElementNotPresent(By.id(MENU_ID));

        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(MENU_ID));
        verifyOpened();

        clickBody();
        waitForElementNotPresent(By.id(MENU_ID));
        verifyClosed();
    }

    @Test
    public void autoAttachedContextMenu_openMultipleTimes() {
        open();

        rightClickOn(TARGET_ID);
        verifyOpened();
        clickBody();
        verifyClosed();
        rightClickOn(TARGET_ID);

        verifyOpened();
        Assert.assertEquals("Auto-attached context menu",
                getOverlay().getAttribute("innerText"));

        checkLogsForErrors();
    }
}
