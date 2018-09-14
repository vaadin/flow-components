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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("context-menu-test")
public class ContextMenuPageIT extends AbstractContextMenuIT {

    static final String CONTEXT_MENU_OVERLAY_TAG = "vaadin-context-menu-overlay";

    @Before
    public void init() {
        open();
    }

    @Test
    public void openedChangeListenerIsCalled_isOpenedReturnsCorrectValue() {
        verifyClosed();
        String string = "The open state of the context menu is ";
        String messageId = "message";

        Assert.assertEquals("No OpenedChangeEvents should be fired initially",
                "", findElement(By.id(messageId)).getText());

        rightClickOn("context-menu-test");
        verifyOpened();
        Assert.assertEquals("Context menu test.", getOverlay().getText());
        assertMessage(string, true, messageId);

        getOverlay().click();
        verifyClosed();
        assertMessage(string, false, messageId);
    }

    @Test
    public void setOpenOnClick_opensCorrectlyWithEitherLeftOrRightClick_getterReturnsCorrect() {
        String string = "Current state is ";
        String messageId = "message-on-click";
        verifyClosed();
        assertMessage(string, false, messageId);

        rightClickOn("context-menu-with-controls");
        verifyOpened();
        getOverlay().click();
        verifyClosed();
        leftClickOn("context-menu-with-controls");
        verifyClosed();

        findElement(By.id("on")).click();
        assertMessage(string, true, messageId);
        leftClickOn("context-menu-with-controls");
        verifyOpened();
        getOverlay().click();
        verifyClosed();
        rightClickOn("context-menu-with-controls");
        verifyClosed();

        findElement(By.id("off")).click();
        assertMessage(string, false, messageId);
        rightClickOn("context-menu-with-controls");
        verifyOpened();
        getOverlay().click();
        verifyClosed();
        leftClickOn("context-menu-with-controls");
        verifyClosed();
    }

    @Test
    public void changeTarget_opensByRightClickingNewTarget() {
        leftClickOn("change-target");
        verifyClosed();

        rightClickOn("context-menu-with-controls");
        verifyClosed();

        rightClickOn("alt-target");
        verifyOpened();
    }

    @Test
    public void setOpenOnClick_changeTarget_opensByClickingNewTarget() {
        leftClickOn("on");
        leftClickOn("change-target");
        verifyClosed();

        rightClickOn("context-menu-with-controls");
        verifyClosed();
        leftClickOn("context-menu-with-controls");
        verifyClosed();

        rightClickOn("alt-target");
        verifyClosed();
        leftClickOn("alt-target");
        verifyOpened();
    }

    @Test
    public void setOpenOnClick_setTargetNull_doesntOpen_changeTarget_opensByClickingNewTarget() {
        leftClickOn("on");
        leftClickOn("remove-target");
        verifyClosed();

        rightClickOn("context-menu-with-controls");
        verifyClosed();
        leftClickOn("context-menu-with-controls");
        verifyClosed();
        rightClickOn("alt-target");
        verifyClosed();
        leftClickOn("alt-target");
        verifyClosed();

        leftClickOn("change-target");
        leftClickOn("alt-target");
        verifyOpened();
    }

    @Test
    public void openMenuAddComponentAtFirst() {
        verifyInitialMenu(3);
        findElement(By.id("button-to-first")).click();
        rightClickOn("context-menu-add-component-target");
        assertButtonNumberInMenu(4);
        assertButtonText(0);
    }

    @Test
    public void openMenuAddComponentAtIndex() {
        verifyInitialMenu(3);
        findElement(By.id("button-to-second")).click();
        rightClickOn("context-menu-add-component-target");
        assertButtonNumberInMenu(4);
        assertButtonText(1);
    }

    private void assertButtonText(int index) {
        Assert.assertEquals("Button Text is not correct", "Added Button",
                findElements(By.tagName(CONTEXT_MENU_OVERLAY_TAG)).get(0)
                        .findElements(By.tagName("button")).get(index)
                        .getText());
    }

    private void verifyInitialMenu(int initialNumber) {
        waitForElementNotPresent(By.id("menu-add-component-at-index"));
        rightClickOn("context-menu-add-component-target");
        waitForElementPresent(By.id("menu-add-component-at-index"));
        assertButtonNumberInMenu(initialNumber);
        leftClickOn("context-menu-add-component-target");
        waitForElementNotPresent(By.id("menu-add-component-at-index"));
    }

    private void assertButtonNumberInMenu(int expectedButtonNumber) {
        Assert.assertEquals(
                "Number of buttons in the menu overlay is not correct.",
                expectedButtonNumber,
                findElement(By.tagName(CONTEXT_MENU_OVERLAY_TAG))
                        .findElements(By.tagName("button")).size());
    }

    private void assertMessage(String string, Boolean state, String id) {
        Assert.assertEquals(string + state, findElement(By.id(id)).getText());
    }
}
