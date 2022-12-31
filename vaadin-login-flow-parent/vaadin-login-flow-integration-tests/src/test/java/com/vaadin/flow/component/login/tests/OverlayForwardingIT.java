package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-login/overlay-remains-in-dom-after-detach-view")
public class OverlayForwardingIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void forwardPageInBeforeEnter_newPageDoesNotContainVaadinLoginOverlay() {
        waitForElementPresent(By.id("forwarded-view"));
        Assert.assertFalse(
                isElementPresent(By.tagName("vaadin-login-overlay")));
    }
}
