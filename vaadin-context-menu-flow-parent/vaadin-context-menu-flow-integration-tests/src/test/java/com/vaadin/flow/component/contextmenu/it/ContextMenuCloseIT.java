/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
