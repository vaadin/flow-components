package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OverlayIT extends BasicIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/overlay");
    }

    @Override
    public LoginElement getLogin() {
        $("button").id("open").click();
        return $(LoginOverlayElement.class).waitForFirst().getLogin();
    }

    @Test
    public void testOverlaySelfAttached() {
        getDriver().get(getBaseURL() + "/overlayselfattached");

        Assert.assertFalse($(LoginOverlayElement.class).exists());
        $("button").id("open").click();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertTrue(loginOverlay.isOpened());

        loginOverlay.getUsernameField().setValue("value");
        loginOverlay.getPasswordField().setValue("value");
        loginOverlay.submit();

        Assert.assertFalse($(LoginOverlayElement.class).exists());
    }

}
