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
package com.vaadin.flow.component.login.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.testbench.TestBenchElement;

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
        LoginOverlayElement overlay = $(LoginOverlayElement.class)
                .waitForFirst();
        checkSuccessfulLogin(overlay.getUsernameField(),
                overlay.getPasswordField(), () -> overlay.submit());
    }

    @Override
    public void testDefaults() {
        super.testDefaults();
        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();
        Assert.assertEquals("App name", loginOverlay.getTitle());
        Assert.assertEquals("Application description",
                loginOverlay.getDescription());
        checkLoginForm(loginOverlay.getUsernameField(),
                loginOverlay.getPasswordField(),
                loginOverlay.getSubmitButton());
    }

    @Test
    public void testOverlaySelfAttached() {
        getDriver()
                .get(super.getBaseURL() + "/vaadin-login/overlayselfattached");

        Assert.assertFalse($(LoginOverlayElement.class).exists());
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();
        Assert.assertTrue(loginOverlay.isOpened());

        loginOverlay.getUsernameField().setValue("value");
        loginOverlay.getPasswordField().setValue("value");
        loginOverlay.submit();

        Assert.assertFalse($(LoginOverlayElement.class).exists());
    }

    @Test
    public void testTitleComponent() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-login") + "/component-title";
        getDriver().get(url);
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();
        TestBenchElement title = loginOverlay.getTitleComponent();

        Assert.assertEquals("Component title", title.getText());

        checkSuccessfulLogin(loginOverlay.getUsernameField(),
                loginOverlay.getPasswordField(), () -> loginOverlay.submit());

        Assert.assertFalse(loginOverlay.isOpened());
        openOverlay();
        Assert.assertTrue(loginOverlay.isOpened());

        title = loginOverlay.getTitleComponent();
        Assert.assertEquals("vaadin:vaadin-h",
                title.$("vaadin-icon").first().getAttribute("icon"));

        Assert.assertEquals("Component title", title.$("h3").first().getText());

        checkSuccessfulLogin(loginOverlay.getUsernameField(),
                loginOverlay.getPasswordField(), () -> loginOverlay.submit());
        Assert.assertFalse(loginOverlay.isOpened());

        checkTitleComponentWasReset();
    }

    @Test
    public void testResetTitleComponent() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-login") + "/component-title";
        getDriver().get(url);
        checkTitleComponentWasReset();
    }

    private void checkTitleComponentWasReset() {
        // Setting title as String should detach the title component
        $("button").id("removeCustomTitle").click();
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();

        Assert.assertFalse(loginOverlay.hasTitleComponent());
        Assert.assertEquals("Make title string again", loginOverlay.getTitle());
    }

    public void testTitleAndDescriptionStrings() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-login")
                + "/property-title-description";
        getDriver().get(url);
        openOverlay();

        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();
        Assert.assertEquals("Property title", loginOverlay.getTitle());
        Assert.assertEquals("Property description",
                loginOverlay.getDescription());
    }

}
