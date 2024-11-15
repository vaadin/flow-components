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

@TestPath("vaadin-context-menu/disable-on-click")
public class MenuItemDisableOnClickIT extends AbstractContextMenuIT {

    private static final String TARGET_ID = "target-div";

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(TARGET_ID));
    }

    @Test
    public void clickDisableOnClickMenuItem_newClickNotRegistered() {
        var itemId = "disable-on-click-menu-item";
        var messageId = "disabled-message";
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));

        var menuItem = findElement(By.id(itemId));
        Assert.assertTrue(menuItem.isEnabled());

        executeScript(
                "arguments[0].click();arguments[0].click();arguments[0].click();arguments[0].click();",
                menuItem);
        Assert.assertFalse(findElement(By.id(itemId)).isEnabled());

        var disabledWithSingleClickMessage = getEnabledStateChangeMessage(
                "Disabled on click", false, 1);
        Assert.assertEquals(disabledWithSingleClickMessage,
                findElement(By.id(messageId)).getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");"
                + "arguments[0].click();", findElement(By.id(itemId)));
        Assert.assertEquals(disabledWithSingleClickMessage,
                findElement(By.id(messageId)).getText());

        // Test that enabling after disable on click works more than once
        for (int i = 0; i < 3; i++) {
            clickElementWithJs("enable-menu-item");

            Assert.assertTrue(findElement(By.id(itemId)).isEnabled());
            Assert.assertEquals("Re-enabled item from server.",
                    findElement(By.id(messageId)).getText());

            clickElementWithJs(itemId);
            Assert.assertFalse(findElement(By.id(itemId)).isEnabled());
            Assert.assertEquals(disabledWithSingleClickMessage,
                    findElement(By.id(messageId)).getText());
        }
    }

    @Test
    public void setItemKeepOpenFalse_clickDisableOnClickMenuItem_menuClosed() {
        var itemId = "disable-on-click-menu-item";
        clickElementWithJs("toggle-keep-open");
        rightClickOn(TARGET_ID);
        verifyOpened();
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(itemId)), 2);
        clickElementWithJs(itemId);
        verifyClosed();
    }

    @Test
    public void disableOnClick_enableInSameRoundTrip_clientSideMenuItemIsEnabled() {
        var itemId = "disable-on-click-re-enable-menu-item";
        rightClickOn(TARGET_ID);
        waitUntil(ExpectedConditions
                .elementToBeClickable(findElement(By.id(itemId))), 2);
        for (int i = 0; i < 3; i++) {
            var menuItem = findElement(By.id(itemId));
            var disabled = (Boolean) getCommandExecutor().getDriver()
                    .executeAsyncScript("""
                            var callback = arguments[arguments.length - 1];
                            var element = arguments[0];
                            element.click();
                            requestAnimationFrame(function() {
                              callback(element.disabled);
                            });""", menuItem);
            Assert.assertTrue(disabled);
            waitUntil(driver -> findElement(By.id(itemId)).isEnabled());
        }
    }

    @Test
    public void removeDisableOnClick_itemWorksNormally() {
        var itemId = "disable-on-click-menu-item";
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());

        clickElementWithJs(itemId);
        Assert.assertFalse(findElement(By.id(itemId)).isEnabled());

        clickElementWithJs("enable-menu-item");
        clickElementWithJs("toggle-menu-item");
        clickElementWithJs(itemId);
        clickElementWithJs(itemId);
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());

        var enabledStateChangeMessage = getEnabledStateChangeMessage(
                "Disabled on click", true, 2);
        Assert.assertEquals(enabledStateChangeMessage,
                findElement(By.id("disabled-message")).getText());
    }

    @Test
    public void disableOnClick_hideWhenDisabled_showWhenEnabled_clientSideMenuItemIsEnabled() {
        var itemId = "disable-on-click-hidden-menu-item";
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));
        for (int i = 0; i < 3; i++) {
            clickElementWithJs(itemId);

            var menuItem = findElement(By.id(itemId));
            waitUntil(ExpectedConditions.invisibilityOf(menuItem), 2);
            waitUntil(
                    ExpectedConditions.not(
                            ExpectedConditions.elementToBeClickable(menuItem)),
                    2);

            clickElementWithJs("enable-hidden-menu-item");

            menuItem = findElement(By.id(itemId));
            waitUntil(ExpectedConditions.visibilityOf(menuItem), 2);
            waitUntil(ExpectedConditions.elementToBeClickable(menuItem), 2);
        }
    }

    @Test
    public void disabledAndPointerEventsAuto_disableOnClick_clientSideMenuItemIsDisabled() {
        var itemId = "disable-on-click-pointer-events-auto";
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));
        var menuItem = findElement(By.id(itemId));
        executeScript("arguments[0].dispatchEvent(new MouseEvent(\"click\"));",
                menuItem);
        Assert.assertFalse(findElement(By.id(itemId)).isEnabled());
    }

    @Test
    public void itemDisabledOnClick_detachAndReattach_itemStillDisableOnClick() {
        var itemId = "disable-on-click-re-enable-menu-item";
        clickElementWithJs("toggle-keep-open");
        rightClickOn(TARGET_ID);
        waitUntil(ExpectedConditions
                .elementToBeClickable(findElement(By.id(itemId))), 2);
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());

        // Detach and reattach
        clickElementWithJs("remove-re-enable-in-same-round-trip-menu-item");
        rightClickOn(TARGET_ID);
        waitForElementPresent(
                By.id("add-re-enable-in-same-round-trip-menu-item"));
        waitForElementNotPresent(By.id(itemId));
        clickElementWithJs("add-re-enable-in-same-round-trip-menu-item");
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());

        clickBody();
        verifyClosed();
        clickElementWithJs("toggle-keep-open");
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(itemId));

        // Test whether the item is still disable on click
        getCommandExecutor().disableWaitForVaadin();
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());
        clickElementWithJs(itemId);
        waitUntil(driver -> !findElement(By.id(itemId)).isEnabled(), 2);
        waitUntil(ExpectedConditions
                .elementToBeClickable(findElement(By.id(itemId))), 2);
        Assert.assertTrue(findElement(By.id(itemId)).isEnabled());
        getCommandExecutor().enableWaitForVaadin();
    }
}
