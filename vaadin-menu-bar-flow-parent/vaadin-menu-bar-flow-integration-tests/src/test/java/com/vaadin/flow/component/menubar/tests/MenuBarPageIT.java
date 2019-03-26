/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("menu-bar-test")
public class MenuBarPageIT extends AbstractComponentIT {

    public static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void rootLevelItemsRendered() {
        Assert.assertEquals("Expected 2 buttons to be rendered", 2,
                menuBar.getButtons().size());
        assertItemContent(0, "item 1");
        assertItemContent(1, "<p>item 2</p>");
    }

    @Test
    public void ellipsisButtonNotRendered() {
        Assert.assertNull("Ellipsis button was unexpectedly found",
                menuBar.getEllipsisButton());
    }

    @Test
    public void clickRootButton_subMenuRenders() {
        menuBar.getButtons().get(0).click();
        verifyOpened();
        Assert.assertArrayEquals(
                new String[] { "sub item 1", "<p>sub item 2</p>" },
                getOverlayMenuItemContents());
    }

    @Test
    public void clickRootButtonWithClickListener_listenerCalled() {
        menuBar.getButtons().get(1).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void clickRootButton_hoverOnParentItem_subSubMenuRenders() {
        openSubSubMenu();

        String[] menuItemContents = getOverlayMenuItemContents(
                getAllOverlays().get(1));
        Assert.assertArrayEquals(
                new String[] { "<p>sub sub item 1</p>", "checkable" },
                menuItemContents);
    }

    @Test
    public void openSubSubMenu_clickCheckableItem_checkableStateChanges() {
        openSubSubMenu();

        getOverlayMenuItems(getAllOverlays().get(1)).get(1).click();
        verifyClosed();

        assertMessage("true");

        menuBar.$("vaadin-menu-bar-button").first().click();
        hoverOn(getOverlayMenuItems().get(1));

        waitUntil(driver -> getAllOverlays().size() == 2);
        TestBenchElement checkableItem = getOverlayMenuItems(
                getAllOverlays().get(1)).get(1);
        Assert.assertTrue(checkableItem.hasAttribute("menu-item-checked"));

        checkableItem.click();
        verifyClosed();
        assertMessage("false");

        openSubSubMenu();
        checkableItem = getOverlayMenuItems(getAllOverlays().get(1)).get(1);
        Assert.assertFalse(checkableItem.hasAttribute("menu-item-checked"));
    }

    @Test
    public void addRootItem_newRootItemRendered() {
        click("add-root-item");
    }

    @Test
    public void removeRootItem_itemNotRendered() {
        click("remove-item");
    }

    @Test
    public void addItemToSubMenu_openSubMenu_itemRendered() {
        click("add-sub-item");
        menuBar.$("vaadin-menu-bar-button").get(1).click();
        verifyNumOfOverlays(1);
        Assert.assertArrayEquals(new String[] { "added sub item" },
                getOverlayMenuItemContents());
    }

    @After
    public void afterTest() {
        checkLogsForErrors();
    }

    private void assertItemContent(int index, String expectedInnerHTML) {
        String itemContent = menuBar.getButtons().get(index)
                .$("vaadin-context-menu-item").first()
                .getAttribute("innerHTML");
        Assert.assertEquals(
                "Unexpected content in the button at index " + index,
                expectedInnerHTML, itemContent);
    }

    private void openSubSubMenu() {
        menuBar.$("vaadin-menu-bar-button").first().click();
        verifyOpened();
        hoverOn(getOverlayMenuItems().get(1));
        verifyNumOfOverlays(2);
    }

    private void assertMessage(String expected) {
        Assert.assertEquals(expected, $("p").id("message").getText());
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    private void clickBody() {
        $("body").first().click();
    }

    private TestBenchElement getOverlay() {
        return $(OVERLAY_TAG).first();
    }

    private List<TestBenchElement> getAllOverlays() {
        return $(OVERLAY_TAG).all();
    }

    private void verifyNumOfOverlays(int expected) {
        waitUntil(driver -> getAllOverlays().size() == expected);
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    private String[] getOverlayMenuItemContents() {
        return getOverlayMenuItemContents(getOverlayMenuItems());
    }

    private String[] getOverlayMenuItemContents(TestBenchElement overlay) {
        return getOverlayMenuItemContents(getOverlayMenuItems(overlay));
    }

    private String[] getOverlayMenuItemContents(
            List<TestBenchElement> menuItems) {
        return menuItems.stream().map(item -> item.getAttribute("innerHTML"))
                .toArray(String[]::new);
    }

    private List<TestBenchElement> getOverlayMenuItems() {
        return getOverlayMenuItems(getOverlay());
    }

    private List<TestBenchElement> getOverlayMenuItems(
            TestBenchElement overlay) {
        return overlay.$("vaadin-context-menu-item").all();
    }

    private void hoverOn(TestBenchElement hoverTarget) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                hoverTarget);
    }
}