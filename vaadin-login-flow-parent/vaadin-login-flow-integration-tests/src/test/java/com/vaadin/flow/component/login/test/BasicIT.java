package com.vaadin.flow.component.login.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public abstract class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    public abstract LoginFormElement getLoginForm();

    @Test
    public void testDefaults() {
        LoginFormElement login = getLoginForm();

        Assert.assertEquals("Log in", login.getFormTitle());
        if (!BrowserUtil.isEdge(getDesiredCapabilities())
                && !BrowserUtil.isSafari(getDesiredCapabilities())) {
            // Error message should be hidden by default, however Safari and
            // Edge drivers return the innerHTML content
            Assert.assertEquals("", login.getErrorMessageTitle());
            Assert.assertEquals("", login.getErrorMessage());
        }
        Assert.assertEquals("Forgot password",
                login.getForgotPasswordButton().getText());
        Assert.assertFalse(getLoginForm().getForgotPasswordButton()
                .hasAttribute("hidden"));
        Assert.assertEquals("", login.getAdditionalInformation());
    }

    protected void checkLoginForm(TextFieldElement username,
            PasswordFieldElement password, ButtonElement submit) {
        Assert.assertEquals("Username", username.getLabel());
        Assert.assertEquals("Password", password.getLabel());
        Assert.assertEquals("Log in", submit.getText());
    }

    protected void checkSuccessfulLogin(TextFieldElement usernameField,
            PasswordFieldElement passwordField, Runnable submit) {
        usernameField.setValue("username");
        passwordField.setValue("password");
        submit.run();
        Assert.assertEquals("Successful login", $("div").id("info").getText());
    }

    @Test
    public void testNoForgotPasswordButton() {
        getDriver().get(getBaseURL() + "/no-forgot-password");

        Assert.assertTrue(getLoginForm().getForgotPasswordButton()
                .hasAttribute("hidden"));
    }

}
