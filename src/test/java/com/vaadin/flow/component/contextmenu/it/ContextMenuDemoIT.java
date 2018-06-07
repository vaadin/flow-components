/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.contextmenu.demo.ContextMenuView;
import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Integration tests for the {@link ContextMenuView}.
 */
public class ContextMenuDemoIT extends ComponentDemoTest {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    @Test
    public void openAndCloseBasicContextMenu_contentIsRendered() {
        verifyClosed();

        rightClickOn(By.id("basic-context-menu-target"));
        verifyOpened();

        Assert.assertEquals("Close the context menu by clicking anywhere",
                getOverlay().getText());

        getOverlay().click();
        verifyClosed();
    }

    private void rightClickOn(By by) {
        Actions action = new Actions(getDriver());
        WebElement element = findElement(by);
        action.contextClick(element).perform();
    }

    private WebElement getOverlay() {
        return findElement(By.tagName(OVERLAY_TAG));
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

    private void verifyOpened() {
        waitForElementPresent(By.tagName(OVERLAY_TAG));
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-context-menu";
    }
}
