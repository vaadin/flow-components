package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void testDefaultStrings() {
        LoginElement login = $(LoginElement.class).waitForFirst();

        Assert.assertEquals("App name", login.getTitle());
        Assert.assertEquals("Inspiring application description", login.getMessage());
        Assert.assertEquals("Log in", login.getFormTitle());
        if (!BrowserUtil.isEdge(getDesiredCapabilities()) && !BrowserUtil.isSafari(getDesiredCapabilities())) {
            // Error message should be hidden by default, however Safari and Edge drivers return the innerHTML content
            Assert.assertEquals("", login.getErrorMessageTitle());
            Assert.assertEquals("", login.getErrorMessage());
        }
        Assert.assertEquals("Username", login.getUsernameField().getLabel());
        Assert.assertEquals("Password", login.getPasswordField().getLabel());
        Assert.assertEquals("Log in", login.getSubmitButton().getText());
        Assert.assertEquals("Forgot password", login.getForgotPasswordButton().getText());
        Assert.assertEquals("In case you need to provide some additional info for the user.",
                login.getAdditionalInformation());
    }

    @Test
    public void forgotPassword() {
        LoginElement login = $(LoginElement.class).waitForFirst();
        login.forgotPassword();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Forgot password button pressed",
                notification);
    }

    @Test
    public void login() {
        LoginElement login = $(LoginElement.class).waitForFirst();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("password");
        login.submit();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Successful login", notification);
    }

    @Test
    public void failedLogin() {
        LoginElement login = $(LoginElement.class).waitForFirst();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("wrongPassword");
        login.submit();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Login failed", notification);
    }

    @Test
    public void actionLogin() {
        getDriver().get(getBaseURL() + "/action");
        LoginElement login = $(LoginElement.class).waitForFirst();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("wrongPassword");
        login.submit();
        Assert.assertTrue("Redirect didn't happened on login",
                getDriver().getCurrentUrl().endsWith("process-login-here"));
    }
}
