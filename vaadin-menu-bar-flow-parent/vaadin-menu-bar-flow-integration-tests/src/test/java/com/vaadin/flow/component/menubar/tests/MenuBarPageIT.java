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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarItemElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarSubMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/menu-bar-test")
public class MenuBarPageIT extends AbstractComponentIT {

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
        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();

        assertOverlayContents(subMenu, "sub item 1", "<p>sub item 2</p>",
                "<p>sub item 3</p>");
    }

    @Test
    public void clickRootButton_hoverOnParentItem_subSubMenuRenders() {
        MenuBarSubMenuElement subSubMenu = openSubSubMenu();

        String[] menuItemContents = getOverlayMenuItemContents(subSubMenu);
        Assert.assertArrayEquals(
                new String[] { "<p>sub sub item 1</p>", "checkable" },
                menuItemContents);
    }

    @Test
    public void openSubMenu_clickItem_listenerCalled() {
        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();
        subMenu.getMenuItem("sub item 1").orElseThrow().click();
        assertMessage("clicked sub item 1");
    }

    @Test
    public void openSubSubMenu_clickCheckableItem_checkableStateChanges() {
        MenuBarSubMenuElement subSubMenu = openSubSubMenu();

        MenuBarItemElement checkableItem = subSubMenu.getMenuItem("checkable")
                .orElseThrow();
        checkableItem.click();
        subSubMenu.waitUntilClosed();

        assertMessage("true");

        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();
        subSubMenu = subMenu.getMenuItems().get(1).openSubMenu();

        checkableItem = subSubMenu.getMenuItem("checkable").orElseThrow();
        Assert.assertTrue(checkableItem.isChecked());

        checkableItem.click();
        subMenu.waitUntilClosed();
        assertMessage("false");

        subSubMenu = openSubSubMenu();
        checkableItem = subSubMenu.getMenuItem("checkable").orElseThrow();
        Assert.assertFalse(checkableItem.isChecked());
    }

    @Test
    public void setCheckedExternally_openSubMenu_itemChecked() {
        click("toggle-checked");
        MenuBarSubMenuElement subSubMenu = openSubSubMenu();
        MenuBarItemElement checkableItem = subSubMenu.getMenuItem("checkable")
                .orElseThrow();
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
        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(1)
                .openSubMenu();
        assertOverlayContents(subMenu, "added sub item");
    }

    @Test
    public void hoverOnRootItem_subMenuNotOpened() {
        menuBar.getButtons().get(0).hover();
        Assert.assertFalse(menuBar.$(MenuBarSubMenuElement.class)
                .withAttribute("opened").exists());
    }

    @Test
    public void setOpenOnHover_hoverOnRootItem_subMenuOpens() {
        click("toggle-open-on-hover");
        menuBar.getButtons().get(0).hover();
        Assert.assertTrue(menuBar.$(MenuBarSubMenuElement.class)
                .withAttribute("opened").exists());
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

        MenuBarSubMenuElement subMenu = overflowButton.openSubMenu();
        assertOverlayContents(subMenu, "<p>item 2</p>", "added item");
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
        MenuBarSubMenuElement subMenu = menuBar.getOverflowButton()
                .openSubMenu();
        subMenu.getMenuItems().get(0).click();
        assertMessage("clicked item 2");
    }

    @Test
    public void overflow_openAndClose_unOverflow_clickButton_listenerCalledOnce() {
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().openSubMenu();
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
        menuBar.getOverflowButton().openSubMenu();
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
        MenuBarSubMenuElement subMenu = menuBar.getOverflowButton()
                .openSubMenu();
        assertDisabled(subMenu.getMenuItems().get(0), true);
    }

    @Test
    public void overflow_disableItem_itemDisabled() {
        click("set-width");
        waitForResizeObserver();
        click("toggle-disable");
        MenuBarSubMenuElement subMenu = menuBar.getOverflowButton()
                .openSubMenu();
        assertDisabled(subMenu.getMenuItems().get(0), true);
        // repeat
        clickBody();
        subMenu = menuBar.getOverflowButton().openSubMenu();
        assertDisabled(subMenu.getMenuItems().get(0), true);
    }

    @Test
    public void disable_overflow_openAndClose_unOverflow_buttonDisabled() {
        click("toggle-disable");
        click("set-width");
        waitForResizeObserver();
        menuBar.getOverflowButton().openSubMenu();
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

        MenuBarSubMenuElement subMenu = menuBar.getOverflowButton()
                .openSubMenu();
        assertOverlayContents(subMenu, "added item");

        clickBody();
        subMenu.waitUntilClosed();
        click("toggle-item-2-visibility");

        subMenu = menuBar.getOverflowButton().openSubMenu();
        assertOverlayContents(subMenu, "<p>item 2</p>", "added item");
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
        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(1)
                .openSubMenu();
        assertOverlayContents(subMenu, "added sub item");
    }

    @Test
    public void addSubItem_clickMenuItem_clickButton_subMenuOpenedAndClosed() {
        click("add-sub-item");
        menuBar.getButtons().get(1).$("vaadin-menu-bar-item").first().click();
        Assert.assertTrue(menuBar.$(MenuBarSubMenuElement.class)
                .withAttribute("opened").exists());
        menuBar.getButtons().get(1).click();
        Assert.assertFalse(menuBar.$(MenuBarSubMenuElement.class)
                .withAttribute("opened").exists());
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

    private MenuBarSubMenuElement openSubSubMenu() {
        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();

        return subMenu.getMenuItems().get(1).openSubMenu();
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

    private void assertOverlayContents(MenuBarSubMenuElement subMenu,
            String... expected) {
        Assert.assertArrayEquals(expected, getOverlayMenuItemContents(subMenu));
    }

    private String[] getOverlayMenuItemContents(MenuBarSubMenuElement subMenu) {
        return subMenu.getMenuItems().stream()
                .map(item -> item.getDomProperty("innerHTML"))
                .toArray(String[]::new);
    }

    private void waitForResizeObserver() {
        getCommandExecutor().getDriver().executeAsyncScript(
                "var callback = arguments[arguments.length - 1];"
                        + "requestAnimationFrame(callback)");

    }
}
