package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.login.testbench.LoginElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    public abstract LoginElement getLogin();

    @Test
    public void testDefaults() {
        LoginElement login = getLogin();

        Assert.assertEquals("Log in", login.getFormTitle());
        if (!BrowserUtil.isEdge(getDesiredCapabilities()) && !BrowserUtil.isSafari(getDesiredCapabilities())) {
            // Error message should be hidden by default, however Safari and Edge drivers return the innerHTML content
            Assert.assertEquals("", login.getErrorMessageTitle());
            Assert.assertEquals("", login.getErrorMessage());
        }
        Assert.assertEquals("Forgot password", login.getForgotPasswordButton().getText());
        Assert.assertFalse(getLogin().getForgotPasswordButton().hasAttribute("hidden"));
        Assert.assertEquals("", login.getAdditionalInformation());
    }

    protected void checkLoginForm(TextFieldElement username, PasswordFieldElement password, ButtonElement submit) {
        Assert.assertEquals("Username", username.getLabel());
        Assert.assertEquals("Password", password.getLabel());
        Assert.assertEquals("Log in", submit.getText());
    }

    protected void checkSuccessfulLogin(TextFieldElement usernameField, PasswordFieldElement passwordField,
                                        Runnable submit) {
        usernameField.setValue("username");
        passwordField.setValue("password");
        submit.run();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Successful login", notification);
    }

    @Test
    public void testNoForgotPasswordButton() {
        getDriver().get(getBaseURL() + "/no-forgot-password");

        Assert.assertTrue(getLogin().getForgotPasswordButton().hasAttribute("hidden"));
    }

}
