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
package com.vaadin.flow.component.contextmenu.it;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractContextMenuIT extends AbstractComponentIT {

    protected void rightClickOn(String id) {
        Actions action = new Actions(getDriver());
        WebElement element = findElement(By.id(id));
        action.contextClick(element).perform();
    }

    protected void leftClickOn(String id) {
        findElement(By.id(id)).click();
    }

    protected void clickBody() {
        $("body").first().click();
    }

    protected ContextMenuElement getMenu() {
        return $(ContextMenuElement.class).withAttribute("opened").first();
    }

    protected List<ContextMenuElement> getAllMenus() {
        return $(ContextMenuElement.class).withAttribute("opened").all();
    }

    protected void verifyNumberOfMenus(int expected) {
        try {
            waitUntil(driver -> getAllMenus().size() == expected);
        } catch (TimeoutException e) {
            Assert.assertEquals("Unexpected number of menus opened at a time.",
                    expected, getAllMenus().size());
        }
    }

    protected void verifyClosed() {
        waitForElementNotPresent(By.cssSelector(
                "vaadin-context-menu[opened], vaadin-context-menu[closing]"));
    }

    protected void verifyClosedAndRemoved() {
        waitForElementNotPresent(By.cssSelector("vaadin-context-menu"));
    }

    protected void verifyOpened() {
        waitForElementPresent(By.cssSelector("vaadin-context-menu[opened]"));
    }

    protected String[] getMenuItemCaptions() {
        return getMenuItemCaptions(getMenuItems());
    }

    protected String[] getMenuItemCaptions(
            List<ContextMenuItemElement> menuItems) {
        return menuItems.stream().map(ContextMenuItemElement::getText)
                .toArray(String[]::new);
    }

    protected List<ContextMenuItemElement> getMenuItems() {
        return getMenuItems(getMenu());
    }

    protected List<ContextMenuItemElement> getMenuItems(
            ContextMenuElement menu) {
        TestBenchElement content = getMenuContent(menu);
        return content.$(ContextMenuItemElement.class).all();
    }

    protected TestBenchElement getMenuContent() {
        return getMenuContent(getMenu());
    }

    protected TestBenchElement getMenuContent(ContextMenuElement menu) {
        return wrap(TestBenchElement.class,
                menu.findElement(By.cssSelector(":scope > [slot='overlay']")));
    }

    protected void openSubMenu(ContextMenuItemElement parentItem) {
        parentItem.openSubMenu();
    }
}
