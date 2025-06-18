/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/overlay-remains-in-dom-after-detach-view")
public class OverlayForwardingIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void forwardPageInBeforeEnter_newPageDoesNotContainVaadinDialogOverlay() {
        waitForElementPresent(By.id("forwarded-view"));
        Assert.assertFalse(
                isElementPresent(By.tagName("vaadin-dialog-overlay")));
    }
}
