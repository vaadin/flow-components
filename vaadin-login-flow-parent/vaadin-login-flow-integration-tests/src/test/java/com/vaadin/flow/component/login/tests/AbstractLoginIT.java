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
        Assert.assertFalse(login.getForgotPasswordButton()
                .hasAttribute("hidden"));
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
