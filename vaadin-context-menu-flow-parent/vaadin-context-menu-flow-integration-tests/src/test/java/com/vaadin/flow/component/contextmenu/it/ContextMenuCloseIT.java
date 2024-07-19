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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-context-menu/close")
public class ContextMenuCloseIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void closeOnClick_openedChangeListener_isFromClientTrue() {
        rightClickOn("context-menu-target");
        verifyOpened();

        clickBody();
        verifyClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: true", message.getText());
    }

    @Test
    public void closeProgrammatically_openedChangeListener_isFromClientFalse() {
        rightClickOn("context-menu-target");
        verifyOpened();

        leftClickOn("close-menu");
        verifyClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: false", message.getText());
    }

    @Test
    public void reopen_closeOnClick_openedChangeListener_isFromClientTrue() {
        rightClickOn("context-menu-target");
        verifyOpened();

        leftClickOn("close-menu");
        verifyClosed();

        rightClickOn("context-menu-target");
        verifyOpened();

        clickBody();
        verifyClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: true", message.getText());
    }
}
