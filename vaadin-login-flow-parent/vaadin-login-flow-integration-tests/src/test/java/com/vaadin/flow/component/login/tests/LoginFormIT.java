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
package com.vaadin.flow.component.login.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-login")
public class LoginFormIT extends AbstractLoginIT {

    @Before
    public void init() {
        open();
    }

    private LoginFormElement getLoginForm() {
        return $(LoginFormElement.class).waitForFirst();
    }

    @Test
    public void login() {
        LoginFormElement login = getLoginForm();
        checkSuccessfulLogin(login.getUsernameField(), login.getPasswordField(),
                () -> login.submit());
    }

    @Test
    public void testDefaults() {
        LoginFormElement login = getLoginForm();
        checkLoginFormDefaults(login);
        checkLoginForm(login.getUsernameField(), login.getPasswordField(),
                login.getSubmitButton());
    }

    @Test
    public void forgotPassword() {
        checkForgotPassword(getLoginForm());
    }

    private void checkForgotPassword(LoginFormElement login) {
        login.forgotPassword();
        Assert.assertEquals("Forgot password button pressed",
                $("div").id("info").getText());
    }

    @Test
    public void disabledLogin() {
        String url = getRootURL() + getTestPath() + "/disable-login";
        getDriver().get(url);
        LoginFormElement login = getLoginForm();
        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("password");
        login.submit();

        Assert.assertTrue("Login notification was shown",
                $("div").id("info").getText().isEmpty());

        login.getPasswordField().sendKeys(Keys.ENTER);
        Assert.assertTrue("Login notification was shown",
                $("div").id("info").getText().isEmpty());

        Assert.assertFalse("Disabled property should not reflect to attribute",
                login.hasAttribute("disabled"));
        // Forgot password event should be processed anyway
        checkForgotPassword(login);
    }

    @Test
    public void passwordEnterKeyLogin() {
        LoginFormElement login = getLoginForm();
        checkSuccessfulLogin(login.getUsernameField(), login.getPasswordField(),
                () -> {
                    login.getPasswordField().sendKeys(Keys.ENTER);
                });
    }

    @Test
    public void usernameEnterKeyLogin() {
        LoginFormElement login = getLoginForm();
        checkSuccessfulLogin(login.getUsernameField(), login.getPasswordField(),
                () -> {
                    login.getUsernameField().sendKeys(Keys.ENTER);
                });
    }

    @Test
    public void failedLogin() {
        LoginFormElement login = getLoginForm();

        TestBenchElement errorMessage = login.getErrorComponent();
        // TODO #isDisplayed() should be used when safari 12 is in use
        Assert.assertTrue(errorMessage.hasAttribute("hidden"));

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("wrongPassword");
        login.submit();

        // TODO #isDisplayed() should be used when safari 12 is in use
        Assert.assertFalse(errorMessage.hasAttribute("hidden"));
        Assert.assertEquals("Incorrect username or password",
                login.getErrorMessageTitle());
        Assert.assertEquals(
                "Check that you have entered the correct username and password and try again.",
                login.getErrorMessage());

        Assert.assertTrue(login.isEnabled());
        login.submit();
        Assert.assertTrue(login.isEnabled());
        login.submit();

        // Should be disabled after 3rd attempt and change the error message
        Assert.assertFalse(login.isEnabled());
        Assert.assertEquals("You made too many attempts",
                login.getErrorMessageTitle());
    }

    @Test
    public void actionLogin() {
        String url = getRootURL() + getTestPath() + "/action";
        getDriver().get(url);
        LoginFormElement login = getLoginForm();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("password");
        login.submit();
        Assert.assertTrue("Redirect didn't happened on login",
                getDriver().getCurrentUrl().endsWith("process-login-here"));
    }

    @Test
    public void testNoForgotPasswordButton() {
        String url = getRootURL() + getTestPath() + "/no-forgot-password";
        getDriver().get(url);

        LoginFormElement login = getLoginForm();
        Assert.assertTrue(login.getForgotPasswordButton()
                .hasAttribute("hidden"));
    }
}
