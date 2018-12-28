package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;
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
        openOverlay();
        return $(LoginOverlayElement.class).waitForFirst().getLogin();
    }

    private void openOverlay() {
        $("button").waitForFirst().click();
    }

    @Override
    public void testDefaultStrings() {
        super.testDefaultStrings();
        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertEquals("App name", loginOverlay.getTitle());
        Assert.assertEquals("Application description", loginOverlay.getDescription());
    }

    @Test
    public void testOverlaySelfAttached() {
        getDriver().get(getBaseURL() + "/overlayselfattached");

        Assert.assertFalse($(LoginOverlayElement.class).exists());
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertTrue(loginOverlay.isOpened());

        loginOverlay.getUsernameField().setValue("value");
        loginOverlay.getPasswordField().setValue("value");
        loginOverlay.submit();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            // https://github.com/vaadin/vaadin-login-flow/issues/27
            skipTest("Skip IE since the overlay is not self detached from the page");
        }
        Assert.assertFalse($(LoginOverlayElement.class).exists());
    }

    @Test
    public void testTitleComponent() {
        getDriver().get(getBaseURL() + "/overlay/component-title");
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        TestBenchElement title = loginOverlay.getTitleComponent();
        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            // https://github.com/vaadin/vaadin-login-flow/issues/27
            skipTest("Skip IE since the teleport doesn't work there");
        }
        Assert.assertEquals("Component title", title.getText());

        checkSuccessfulLogin(loginOverlay.getLogin(), () -> loginOverlay.submit());

        Assert.assertFalse(loginOverlay.isOpened());
        openOverlay();
        Assert.assertTrue(loginOverlay.isOpened());

        title = loginOverlay.getTitleComponent();
        Assert.assertEquals("vaadin:vaadin-h",
                title.$("iron-icon").first().getAttribute("icon"));

        Assert.assertEquals("Component title",
                title.$("h3").first().getText());
    }

    @Test
    public void testTitleAndDescriptionStrings() {
        getDriver().get(getBaseURL() + "/overlay/property-title-description");
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertEquals("Property title", loginOverlay.getTitle());
        Assert.assertEquals("Property description", loginOverlay.getDescription());
    }

}
