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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-test")
public class MenuBarPageIT extends AbstractComponentIT {

    public static final String OVERLAY_TAG = "vaadin-menu-bar-overlay";

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void rootLevelItemsRendered() {
        assertButtonContents("item 1", "<p>item 2</p>");
    }

    @Test
    public void overflowButtonNotRendered() {
        Assert.assertNull("Overflow button was unexpectedly found",
                menuBar.getOverflowButton());
    }

    @Test
    public void clickRootButton_subMenuRenders() {
        menuBar.getButtons().get(0).click();
        verifyOpened();
        assertOverlayContents("sub item 1", "<p>sub item 2</p>",
                "<p>sub item 3</p>");
    }

    @Test
    public void clickRootItem_subMenuRenders() {
        menuBar.getButtons().get(0).$("vaadin-menu-bar-item").first().click();
        verifyOpened();
        assertOverlayContents("sub item 1", "<p>sub item 2</p>",
                "<p>sub item 3</p>");
    }

    @Test
    public void clickRootButton_hoverOnParentItem_subSubMenuRenders() {
        openSubSubMenu();

        String[] menuItemContents = getOverlayMenuItemContents(
                menuBar.getAllSubMenus().get(1));
        Assert.assertArrayEquals(
                new String[] { "<p>sub sub item 1</p>", "checkable" },
                menuItemContents);
    }

    @Test
    public void openSubMenu_clickItem_listenerCalled() {
        menuBar.getButtons().get(0).click();
        menuBar.getSubMenuItems().get(0).click();
        assertMessage("clicked sub item 1");
    }

    @Test
    public void openSubSubMenu_clickCheckableItem_checkableStateChanges() {
        openSubSubMenu();

        menuBar.getSubMenuItems(menuBar.getAllSubMenus().get(1)).get(1).click();
        verifyClosed();

        assertMessage("true");

        menuBar.$("vaadin-menu-bar-button").first().click();
        hoverOn(menuBar.getSubMenuItems().get(1));

        waitUntil(driver -> menuBar.getAllSubMenus().size() == 2);
        MenuBarItemElement checkableItem = menuBar
                .getSubMenuItems(menuBar.getAllSubMenus().get(1)).get(1);
        Assert.assertTrue(checkableItem.isChecked());

        checkableItem.click();
        verifyClosed();
        assertMessage("false");

        openSubSubMenu();
        checkableItem = menuBar.getSubMenuItems(menuBar.getAllSubMenus().get(1))
                .get(1);
        Assert.assertFalse(checkableItem.isChecked());
    }

    @Test
    public void setCheckedExternally_openSubMenu_itemChecked() {
        click("toggle-checked");
        openSubSubMenu();
        MenuBarItemElement checkableItem = menuBar
                .getSubMenuItems(menuBar.getAllSubMenus().get(1)).get(1);
        Assert.assertTrue(checkableItem.isChecked());
    }

    @Test
    public void addRootItem_newRootItemRendered() {
        click("add-root-item");
        assertButtonContents("item 1", "<p>item 2</p>", "added item");
    }

    @Test
    public void removeRootItem_itemNotRendered() {
        click("remove-item");
        assertButtonContents("item 1");
    }

    @Test
    public void addItemToSubMenu_openSubMenu_itemRendered() {
        click("add-sub-item");
        menuBar.$("vaadin-menu-bar-button").get(1).click();
        verifyNumOfOverlays(1);
        assertOverlayContents("added sub item");
    }

    @Test
    public void hoverOnRootItem_subMenuNotOpened() {
        hoverOn(menuBar.getButtons().get(0));
        verifyClosed();
    }

    @Test
    public void setOpenOnHover_hoverOnRootItem_subMenuOpens() {
        click("toggle-open-on-hover");
        hoverOn(menuBar.getButtons().get(0));
        verifyOpened();
    }

    @Test
    public void buttonsOverflow_itemsMovedToOverflowSubMenu() {
        click("set-width");
        waitForResizeObserver();
        click("add-root-item");
        MenuBarButtonElement overflowButton = menuBar.getOverflowButton();
        Assert.assertNotNull("Expected the overflow button to be rendered",
                overflowButton);
        assertButtonContents("item 1");

        overflowButton.click();
        verifyOpened();
        assertOverlayContents("<p>item 2</p>", "added item");
    }

    @Test
    public void clickRootButtonWithClickListener_listenerCalledOnce() {
        menuBar.getButtons().get(1).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void clickRootItemWithClickListener_listenerCalledOnce() {
        menuBar.getButtons().get(1).$("vaadin-menu-bar-item").first().click();
        assertMessage("clicked item 2");
    }

    @Test
    public void changeItems_clickRootButtonWithClickListener_clickListenerCalledOnce() {
        click("add-root-item");
        menuBar.getButtons().get(1).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void changeItems_clickRootItemWithClickListener_clickListenerCalledOnce() {
        click("add-root-item");
        menuBar.getButtons().get(1).$("vaadin-menu-bar-item").first().click();
        assertMessage("clicked item 2");
    }

    @Test
    public void buttonWithClickListenerOverflows_clickListenerWorksInSubMenu() {
        click("set-width");
        waitForResizeObserver();
        var overflowButton = menuBar.getOverflowButton();
        overflowButton.click();
        Assert.assertNotNull(menuBar.getSubMenu());
        menuBar.getSubMenuItems(overflowButton.getSubMenu()).get(0).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void overflow_openAndClose_unOverflow_clickButton_listenerCalledOnce() {
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        verifyOpened();
        clickBody();
        click("reset-width");
        waitForResizeObserver();
        menuBar.getButtons().get(1).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void overflow_openAndClose_unOverflow_clickItem_listenerCalledOnce() {
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        verifyOpened();
        clickBody();
        click("reset-width");
        waitForResizeObserver();
        menuBar.getButtons().get(1).$("vaadin-menu-bar-item").first().click();
        assertMessage("clicked item 2");
    }

    @Test
    public void buttonsReflectDisabledStateOfMenuItems() {
        assertButtonDisabled(0, false);
        click("toggle-disable");
        assertButtonDisabled(0, true);
        click("toggle-disable");
        assertButtonDisabled(0, false);
    }

    @Test
    public void disableButton_removeDisabledAttribute_click_listenerNotCalled() {
        click("toggle-disable");
        MenuBarButtonElement button2 = menuBar.getButtons().get(1);
        executeScript("arguments[0].disabled=false;"
                + "arguments[0].querySelector('vaadin-menu-bar-item').disabled=false;",
                button2);
        button2 = menuBar.getButtons().get(1);
        button2.click();
        assertMessage("");
    }

    @Test
    public void disableItems_addItem_oldItemStillDisabled() {
        click("toggle-disable");
        click("add-root-item");
        assertButtonDisabled(0, true);
        assertButtonDisabled(1, true);
        assertButtonDisabled(2, false);
    }

    @Test
    public void disableItem_overflow_itemDisabled() {
        click("toggle-disable");
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        verifyOpened();
        assertDisabled(menuBar.getSubMenuItems().get(0), true);
    }

    @Test
    public void overflow_disableItem_itemDisabled() {
        click("set-width");
        waitForResizeObserver();
        click("toggle-disable");
        menuBar.getOverflowButton().click();
        verifyOpened();
        assertDisabled(menuBar.getSubMenuItems().get(0), true);
        // repeat
        clickBody();
        menuBar.getOverflowButton().click();
        verifyOpened();
        assertDisabled(menuBar.getSubMenuItems().get(0), true);
    }

    @Test
    public void disable_overflow_openAndClose_unOverflow_buttonDisabled() {
        click("toggle-disable");
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().click();
        verifyOpened();
        clickBody();
        click("reset-width");
        waitForResizeObserver();
        assertButtonDisabled(1, true);
    }

    @Test
    public void toggleItemVisible_buttonRemovedAndAdded() {
        click("toggle-item-2-visibility");
        assertButtonContents("item 1");
        click("toggle-item-2-visibility");
        assertButtonContents("item 1", "<p>item 2</p>");
    }

    @Test
    public void hiddenItemOverflows_overflowButtonNotRendered() {
        click("toggle-item-2-visibility");
        click("set-width");
        waitForResizeObserver();
        Assert.assertNull(menuBar.getOverflowButton());
    }

    @Test
    public void itemsOverflow_toggleItemVisible_visibleStateCorrectInOverlay() {
        click("add-root-item");
        click("set-width");
        waitForResizeObserver();
        click("toggle-item-2-visibility");

        menuBar.getOverflowButton().click();
        assertOverlayContents("added item");

        clickBody();
        verifyClosed();
        click("toggle-item-2-visibility");

        menuBar.getOverflowButton().click();
        assertOverlayContents("<p>item 2</p>", "added item");
    }

    @Test
    public void hideParentButton_noClientError() {
        click("add-sub-item");
        click("toggle-item-2-visibility");
        checkLogsForErrors();
    }

    @Test
    public void hideParentButton_setVisible_subMenuRendered() {
        click("add-sub-item");
        click("toggle-item-2-visibility");
        click("toggle-item-2-visibility");
        menuBar.getButtons().get(1).click();
        verifyNumOfOverlays(1);
        assertOverlayContents("added sub item");
    }

    @Test
    public void addSubItem_clickMenuItem_clickButton_subMenuOpenedAndClosed() {
        click("add-sub-item");
        menuBar.getButtons().get(1).$("vaadin-menu-bar-item").first().click();
        verifyOpened();
        menuBar.getButtons().get(1).click();
        verifyClosed();
    }

    @After
    public void afterTest() {
        checkLogsForErrors();
    }

    private void assertButtonDisabled(int index, boolean expectDisabled) {
        assertDisabled(menuBar.getButtons().get(index), expectDisabled);
    }

    private void assertDisabled(TestBenchElement element,
            boolean expectDisabled) {
        Assert.assertEquals("Unexpected disabled state on element",
                expectDisabled, element.hasAttribute("disabled"));
    }

    private void assertButtonContents(String... expectedInnerHTML) {
        String[] contents = menuBar.getButtons().stream().map(button -> button
                .$("vaadin-menu-bar-item").first().getDomProperty("innerHTML"))
                .toArray(String[]::new);
        Assert.assertArrayEquals(expectedInnerHTML, contents);
    }

    private void openSubSubMenu() {
        menuBar.getButtons().get(0).click();
        verifyOpened();
        hoverOn(menuBar.getSubMenuItems().get(1));
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

    private void verifyNumOfOverlays(int expected) {
        waitUntil(driver -> menuBar.getAllSubMenus().size() == expected);
    }

    private void assertOverlayContents(String... expected) {
        Assert.assertArrayEquals(expected, getOverlayMenuItemContents());
    }

    private String[] getOverlayMenuItemContents() {
        return getOverlayMenuItemContents(menuBar.getSubMenuItems());
    }

    private String[] getOverlayMenuItemContents(TestBenchElement overlay) {
        return getOverlayMenuItemContents(menuBar.getSubMenuItems(overlay));
    }

    private String[] getOverlayMenuItemContents(
            List<MenuBarItemElement> menuItems) {
        return menuItems.stream().map(item -> item.getDomProperty("innerHTML"))
                .toArray(String[]::new);
    }

    private void hoverOn(TestBenchElement hoverTarget) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                hoverTarget);
    }

    private void waitForResizeObserver() {
        getCommandExecutor().getDriver().executeAsyncScript(
                "var callback = arguments[arguments.length - 1];"
                        + "requestAnimationFrame(callback)");

    }

    public void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    public void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }
}
