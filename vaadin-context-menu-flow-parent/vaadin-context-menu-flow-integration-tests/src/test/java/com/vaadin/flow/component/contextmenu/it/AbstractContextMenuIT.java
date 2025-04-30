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

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuOverlayElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractContextMenuIT extends AbstractComponentIT {

    public static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

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

    protected ContextMenuOverlayElement getOverlay() {
        return $(ContextMenuOverlayElement.class).first();
    }

    protected List<ContextMenuOverlayElement> getAllOverlays() {
        return $(ContextMenuOverlayElement.class).all();
    }

    protected void verifyNumOfOverlays(int expected) {
        try {
            waitUntil(driver -> getAllOverlays().size() == expected);
        } catch (TimeoutException e) {
            Assert.assertEquals(
                    "Unexpected number of overlays opened at a time.", expected,
                    getAllOverlays().size());
        }
    }

    protected void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    protected void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
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
        return getMenuItems(getOverlay());
    }

    protected List<ContextMenuItemElement> getMenuItems(
            ContextMenuOverlayElement overlay) {
        return overlay.getMenuItems();
    }

    protected void openSubMenu(ContextMenuItemElement parentItem) {
        parentItem.openSubMenu();
    }
}
