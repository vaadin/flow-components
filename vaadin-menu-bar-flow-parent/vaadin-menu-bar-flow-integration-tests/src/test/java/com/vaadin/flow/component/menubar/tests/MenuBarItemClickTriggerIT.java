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
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-item-click-trigger")
public class MenuBarItemClickTriggerIT extends AbstractComponentIT {

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void clickRootItem_actionSetsResultText() {
        menuBar.getButtons().get(0).click();

        Assert.assertEquals(MenuBarItemClickTriggerPage.ROOT_MESSAGE,
                findElement(By.id(MenuBarItemClickTriggerPage.RESULT_ID))
                        .getText());
    }

    @Test
    public void clickSubMenuItem_actionSetsResultText() {
        menuBar.getButtons().get(1).openSubMenu().getMenuItems().get(0).click();

        Assert.assertEquals(MenuBarItemClickTriggerPage.SUB_MESSAGE,
                findElement(By.id(MenuBarItemClickTriggerPage.RESULT_ID))
                        .getText());
    }
}
