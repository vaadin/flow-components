/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import static com.vaadin.flow.component.contextmenu.it.MenuItemDisableOnClickView.getEnabledStateChangeMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-context-menu/disable-on-click")
public class MenuItemDisableOnClickIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("target-div"));
        rightClickOn("target-div");
    }

    @Test
    public void clickDisableOnClickMenuItem_newClickNotRegistered() {
        var menuItem = findElement(By.id("disable-on-click-menu-item"));
        Assert.assertTrue(menuItem.isEnabled());

        scrollToElement(menuItem);
        executeScript(
                "arguments[0].click();arguments[0].click();arguments[0].click();arguments[0].click();",
                menuItem);
        Assert.assertFalse(menuItem.isEnabled());

        var disabledWithSingleClickMessage = getEnabledStateChangeMessage(
                "Disabled on click", false, 1);
        Assert.assertEquals(disabledWithSingleClickMessage,
                findElement(By.id("disabled-message")).getText());

        executeScript(
                "arguments[0].removeAttribute(\"disabled\");"
                        + "arguments[0].click();",
                findElement(By.id("disable-on-click-menu-item")));
        Assert.assertEquals(disabledWithSingleClickMessage,
                findElement(By.id("disabled-message")).getText());

        // Test that enabling after disable on click works more than once
        for (int i = 0; i < 3; i++) {
            findElement(By.id("enable-menu-item")).click();

            menuItem = findElement(By.id("disable-on-click-menu-item"));
            Assert.assertTrue(menuItem.isEnabled());
            Assert.assertEquals("Re-enabled item from server.",
                    findElement(By.id("disabled-message")).getText());

            menuItem.click();
            Assert.assertFalse(menuItem.isEnabled());
            Assert.assertEquals(
                    "Item should have gotten 1 click and become disabled.",
                    disabledWithSingleClickMessage,
                    findElement(By.id("disabled-message")).getText());
        }
    }

    @Test
    public void disableMenuItemOnClick_canBeEnabled() {
        getCommandExecutor().disableWaitForVaadin();
        var menuItem = $(TestBenchElement.class)
                .id("temporarily-disabled-menu-item");

        for (int i = 0; i < 3; i++) {
            menuItem.click();

            Assert.assertFalse(menuItem.isEnabled());
            waitUntil(
                    ExpectedConditions
                            .elementToBeClickable($(TestBenchElement.class)
                                    .id("temporarily-disabled-menu-item")),
                    2000);

            Assert.assertTrue("item should be enabled again",
                    menuItem.isEnabled());
        }

        getCommandExecutor().enableWaitForVaadin();
    }

    @Test
    public void removeDisabled_itemWorksNormally() {
        var menuItem = findElement(By.id("disable-on-click-menu-item"));
        Assert.assertTrue(menuItem.isEnabled());

        scrollIntoViewAndClick(menuItem);
        Assert.assertFalse(menuItem.isEnabled());

        findElement(By.id("enable-menu-item")).click();
        findElement(By.id("toggle-menu-item")).click();
        menuItem = findElement(By.id("disable-on-click-menu-item"));
        menuItem.click();
        menuItem.click();
        Assert.assertTrue(menuItem.isEnabled());

        var enabledStateChangeMessage = getEnabledStateChangeMessage(
                "Disabled on click", true, 2);
        Assert.assertEquals(enabledStateChangeMessage,
                findElement(By.id("disabled-message")).getText());
    }

    @Test
    public void disableOnClick_enableInSameRoundTrip_clientSideMenuItemIsEnabled() {
        var menuItem = findElement(
                By.id("disable-on-click-re-enable-menu-item"));
        for (int i = 0; i < 3; i++) {
            var disabled = (Boolean) executeScript(
                    "arguments[0].click(); return arguments[0].disabled",
                    menuItem);
            Assert.assertTrue(disabled);

            waitUntil(ExpectedConditions.elementToBeClickable(menuItem));
        }
    }

    @Test
    public void disableOnClick_hideWhenDisabled_showWhenEnabled_clientSideMenuItemIsEnabled() {
        var menuItem = findElement(By.id("disable-on-click-hidden-menu-item"));
        for (int i = 0; i < 3; i++) {
            menuItem.click();

            waitUntil(ExpectedConditions.invisibilityOf(menuItem));
            waitUntil(ExpectedConditions
                    .not(ExpectedConditions.elementToBeClickable(menuItem)));

            findElement(By.id("enable-hidden-menu-item")).click();
            waitUntil(ExpectedConditions.visibilityOf(menuItem));
            waitUntil(ExpectedConditions.elementToBeClickable(menuItem));
        }
    }

    @Test
    public void disabledAndPointerEventsAuto_disableOnClick_clientSideMenuItemIsDisabled() {
        var menuItem = findElement(
                By.id("disable-on-click-pointer-events-auto"));

        scrollToElement(menuItem);
        executeScript("arguments[0].dispatchEvent(new MouseEvent(\"click\"));",
                menuItem);

        Assert.assertFalse(menuItem.isEnabled());
    }
}
