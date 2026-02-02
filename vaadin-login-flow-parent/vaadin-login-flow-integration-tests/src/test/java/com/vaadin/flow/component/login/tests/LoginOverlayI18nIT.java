/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link LoginOverlayI18nPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-login/i18n")
public class LoginOverlayI18nIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setI18n_i18nIsApplied() {
        clickElementWithJs("set-i18n");
        clickElementWithJs("open");
        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();

        Assert.assertEquals("Custom title", loginOverlay.getTitle());
        Assert.assertEquals("Custom description",
                loginOverlay.getDescription());
        Assert.assertEquals("Custom form title", loginOverlay.getFormTitle());
        Assert.assertEquals("Custom username",
                loginOverlay.getUsernameField().getLabel());
        Assert.assertEquals("Custom password",
                loginOverlay.getPasswordField().getLabel());
        Assert.assertEquals("Custom submit",
                loginOverlay.getSubmitButton().getText().trim());
        Assert.assertEquals("Custom forgot password",
                loginOverlay.getForgotPasswordButton().getText().trim());
    }

    @Test
    public void setI18n_setEmptyI18n_defaultI18nIsRestored() {
        clickElementWithJs("set-i18n");
        clickElementWithJs("set-empty-i18n");
        clickElementWithJs("open");
        LoginOverlayElement loginOverlay = $(LoginOverlayElement.class)
                .waitForFirst();

        Assert.assertEquals("App name", loginOverlay.getTitle());
        Assert.assertEquals("Application description",
                loginOverlay.getDescription());
        Assert.assertEquals("Log in", loginOverlay.getFormTitle());
        Assert.assertEquals("Username",
                loginOverlay.getUsernameField().getLabel());
        Assert.assertEquals("Password",
                loginOverlay.getPasswordField().getLabel());
        Assert.assertEquals("Log in",
                loginOverlay.getSubmitButton().getText().trim());
        Assert.assertEquals("Forgot password",
                loginOverlay.getForgotPasswordButton().getText().trim());
    }
}
