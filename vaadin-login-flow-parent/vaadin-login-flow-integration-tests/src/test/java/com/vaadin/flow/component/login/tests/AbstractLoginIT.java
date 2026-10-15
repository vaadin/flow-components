/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.login.tests;

import org.junit.Assert;
import org.junit.Ignore;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.tests.AbstractComponentIT;

@Ignore
public abstract class AbstractLoginIT extends AbstractComponentIT {

    protected void checkLoginFormDefaults(LoginFormElement login) {
        Assert.assertEquals("Log in", login.getFormTitle());
        Assert.assertEquals("", login.getErrorMessageTitle());
        Assert.assertEquals("", login.getErrorMessage());
        Assert.assertEquals("Forgot password",
                login.getForgotPasswordButton().getText().trim());
        Assert.assertFalse(
                login.getForgotPasswordButton().hasAttribute("hidden"));
        Assert.assertEquals("", login.getAdditionalInformation());
    }

    protected void checkLoginForm(TextFieldElement username,
            PasswordFieldElement password, ButtonElement submit) {
        Assert.assertEquals("Username", username.getLabel());
        Assert.assertEquals("Password", password.getLabel());
        Assert.assertEquals("Log in", submit.getText().trim());
    }

    protected void checkSuccessfulLogin(TextFieldElement usernameField,
            PasswordFieldElement passwordField, Runnable submit) {
        usernameField.setValue("username");
        passwordField.setValue("password");
        submit.run();
        Assert.assertEquals("Successful login", $("div").id("info").getText());
    }
}
