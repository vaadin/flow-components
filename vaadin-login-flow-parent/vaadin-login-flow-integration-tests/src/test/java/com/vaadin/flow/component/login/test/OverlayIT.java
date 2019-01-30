package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

public class OverlayIT extends BasicIT {

    @Override
    protected String getBaseURL() {
        return super.getBaseURL() + "/overlay";
    }

    @Override
    public LoginFormElement getLoginForm() {
        openOverlay();
        return $(LoginOverlayElement.class).waitForFirst().getLoginForm();
    }

    private void openOverlay() {
        $("button").waitForFirst().click();
    }

    @Test
    public void login() {
        openOverlay();
        LoginOverlayElement overlay = $(LoginOverlayElement.class).waitForFirst();
        checkSuccessfulLogin(overlay.getUsernameField(), overlay.getPasswordField(), () -> overlay.submit());
    }

    @Override
    public void testDefaults() {
        super.testDefaults();
        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertEquals("App name", loginOverlay.getTitle());
        Assert.assertEquals("Application description", loginOverlay.getDescription());
        checkLoginForm(loginOverlay.getUsernameField(), loginOverlay.getPasswordField(), loginOverlay.getSubmitButton());
    }

    @Test
    public void testOverlaySelfAttached() {
        getDriver().get(super.getBaseURL() + "/overlayselfattached");

        Assert.assertFalse($(LoginOverlayElement.class).exists());
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertTrue(loginOverlay.isOpened());

        loginOverlay.getUsernameField().setValue("value");
        loginOverlay.getPasswordField().setValue("value");
        loginOverlay.submit();

        Assert.assertFalse($(LoginOverlayElement.class).exists());
    }

    @Test
    public void testTitleComponent() {
        getDriver().get(getBaseURL() + "/component-title");
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        TestBenchElement title = loginOverlay.getTitleComponent();

        Assert.assertEquals("Component title", title.getText());

        checkSuccessfulLogin(loginOverlay.getUsernameField(), loginOverlay.getPasswordField(),
                () -> loginOverlay.submit());

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
        getDriver().get(getBaseURL() + "/property-title-description");
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertEquals("Property title", loginOverlay.getTitle());
        Assert.assertEquals("Property description", loginOverlay.getDescription());
    }

}
