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

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link ContextMenuView}.
 */
@TestPath("vaadin-context-menu")
public class ContextMenuDemoIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    @Before
    public void init() {
        open();
    }

    @Test
    public void openAndCloseBasicContextMenu_contentIsRendered() {
        verifyClosed();

        rightClickOn(By.id("basic-context-menu-target"));
        verifyOpened();

        Assert.assertArrayEquals(new String[] { "First menu item",
                "Second menu item", "Disabled menu item" },
                getMenuItemCaptions());

        Assert.assertEquals("The last item is supposed to be disabled", "true",
                getMenuItems().get(2).getAttribute("disabled"));

        $("body").first().click();
        verifyClosed();
    }

    @Test
    public void openAndCloseContextMenuWithComponents_contentIsRendered() {
        verifyClosed();

        rightClickOn(By.id("context-menu-with-components-target"));
        verifyOpened();

        Assert.assertArrayEquals(new String[] { "First menu item", "Checkbox" },
                getMenuItemCaptions());
        WebElement checkbox = getMenuItems().get(1)
                .findElement(By.tagName("vaadin-checkbox"));

        getOverlay().findElement(By.tagName("hr"));
        WebElement label = getOverlay().findElement(
                By.cssSelector("vaadin-context-menu-list-box > label"));
        Assert.assertEquals("This is not a menu item", label.getText());

        checkbox.click();
        $("body").first().click();
        verifyClosed();
        WebElement message = findElement(
                By.id("context-menu-with-components-message"));
        Assert.assertEquals("Clicked on checkbox with value: true",
                message.getText());
    }

    @Test
    public void hierarchicalContextMenu_openSubMenus() {
        verifyClosed();

        rightClickOn(By.id("hierarchical-menu-target"));
        verifyOpened();

        openSubMenu(getMenuItems().get(1));

        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 2);
        List<TestBenchElement> overlays = $(OVERLAY_TAG).all();

        openSubMenu(getMenuItems(overlays.get(1)).get(1));

        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 3);
        overlays = $(OVERLAY_TAG).all();

        getMenuItems(overlays.get(2)).get(0).click();

        Assert.assertEquals("Clicked on the third item",
                $("label").id("hierarchical-menu-message").getText());

        verifyClosed();
    }

    @Test
    public void checkableMenuItems() {
        verifyClosed();

        rightClickOn(By.id("checkable-menu-items-target"));
        verifyOpened();

        List<TestBenchElement> items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), false);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), true);

        items.get(1).click();

        Assert.assertEquals("Unselected option 2",
                $("label").id("checkable-menu-items-message").getText());
        verifyClosed();

        rightClickOn(By.id("checkable-menu-items-target"));
        verifyOpened();

        items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), false);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), false);

        items.get(0).click();

        Assert.assertEquals("Selected option 1",
                $("label").id("checkable-menu-items-message").getText());
        verifyClosed();

        rightClickOn(By.id("checkable-menu-items-target"));
        verifyOpened();

        items = getMenuItems();
        ContextMenuPageIT.assertCheckedInClientSide(items.get(0), true);
        ContextMenuPageIT.assertCheckedInClientSide(items.get(1), false);
    }

    @Test
    public void subMenuHasComponents_componentsAreNotItems() {
        verifyClosed();

        rightClickOn(By.id("context-menu-with-submenu-components-target"));
        verifyOpened();

        openSubMenu(getMenuItems().get(1));
        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 2);

        TestBenchElement subMenuOverlay = $(OVERLAY_TAG).all().get(1);

        TestBenchElement overlayContainer = subMenuOverlay
                .$("vaadin" + "-context-menu-list-box").first();
        List<WebElement> items = overlayContainer.findElements(By.xpath("./*"));
        Assert.assertEquals(4, items.size());
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("hr",
                items.get(1).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(2).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("label",
                items.get(3).getTagName().toLowerCase(Locale.ENGLISH));
    }

    private void rightClickOn(By by) {
        Actions action = new Actions(getDriver());
        WebElement element = findElement(by);
        action.contextClick(element).perform();
    }

    private TestBenchElement getOverlay() {
        return $(OVERLAY_TAG).first();
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    private String[] getMenuItemCaptions() {
        return getMenuItems().stream().map(WebElement::getText)
                .toArray(String[]::new);
    }

    private List<TestBenchElement> getMenuItems() {
        return getOverlay().$("vaadin-context-menu-item").all();
    }

    private List<TestBenchElement> getMenuItems(TestBenchElement overlay) {
        return overlay.$("vaadin-context-menu-item").all();
    }

    private void openSubMenu(WebElement parentItem) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                parentItem);
    }
}
