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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-context-menu/close")
public class ContextMenuCloseIT extends AbstractContextMenuIT {

    private TestBenchElement target;

    @Before
    public void init() {
        open();
        target = $(TestBenchElement.class).id("context-menu-target");
    }

    @Test
    public void closeOnClick_openedChangeListener_isFromClientTrue() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick(target);

        clickBody();
        contextMenu.waitUntilClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: true", message.getText());
    }

    @Test
    public void closeProgrammatically_openedChangeListener_isFromClientFalse() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick(target);

        leftClickOn("close-menu");
        contextMenu.waitUntilClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: false", message.getText());
    }

    @Test
    public void reopen_closeOnClick_openedChangeListener_isFromClientTrue() {
        ContextMenuElement contextMenu = ContextMenuElement
                .openByRightClick(target);

        leftClickOn("close-menu");
        contextMenu.waitUntilClosed();

        contextMenu = ContextMenuElement.openByRightClick(target);

        clickBody();
        contextMenu.waitUntilClosed();

        WebElement message = findElement(By.id("closed-message"));
        Assert.assertEquals("Closed from client: true", message.getText());
    }
}
