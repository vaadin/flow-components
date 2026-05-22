/*
 * Copyright 2000-2026 Vaadin Ltd.
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

@TestPath("vaadin-context-menu/menu-item-click-trigger")
public class MenuItemClickTriggerIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(MenuItemClickTriggerView.TARGET_ID));
    }

    @Test
    public void clickMenuItem_actionSetsResultText() {
        rightClickOn(MenuItemClickTriggerView.TARGET_ID);
        waitForElementPresent(By.id(MenuItemClickTriggerView.SET_ITEM_ID));

        findElement(By.id(MenuItemClickTriggerView.SET_ITEM_ID)).click();

        Assert.assertEquals(MenuItemClickTriggerView.SET_MESSAGE,
                findElement(By.id(MenuItemClickTriggerView.RESULT_ID))
                        .getText());
    }

    @Test
    public void clickClearItem_actionClearsResultText() {
        rightClickOn(MenuItemClickTriggerView.TARGET_ID);
        waitForElementPresent(By.id(MenuItemClickTriggerView.SET_ITEM_ID));
        findElement(By.id(MenuItemClickTriggerView.SET_ITEM_ID)).click();

        rightClickOn(MenuItemClickTriggerView.TARGET_ID);
        waitForElementPresent(By.id(MenuItemClickTriggerView.CLEAR_ITEM_ID));
        findElement(By.id(MenuItemClickTriggerView.CLEAR_ITEM_ID)).click();

        Assert.assertEquals("",
                findElement(By.id(MenuItemClickTriggerView.RESULT_ID))
                        .getText());
    }
}
