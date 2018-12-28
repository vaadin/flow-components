package com.vaadin.flow.component.login;

import org.junit.Assert;
import org.junit.Test;

public class LoginI18nTest {

    @Test
    public void createDefault() {
        LoginI18n i18n = LoginI18n.createDefault();

        Assert.assertEquals("Log in", i18n.getForm().getTitle());
        Assert.assertEquals("Username", i18n.getForm().getUsername());
        Assert.assertEquals("Password", i18n.getForm().getPassword());
        Assert.assertEquals("Forgot password", i18n.getForm().getForgotPassword());
        Assert.assertEquals("Log in", i18n.getForm().getSubmit());

        Assert.assertEquals("Incorrect username or password", i18n.getErrorMessage().getTitle());
        Assert.assertEquals("Check that you have entered the correct username and password and try again.",
                i18n.getErrorMessage().getMessage());

        Assert.assertEquals("In case you need to provide some additional info for the user.",
                i18n.getAdditionalInformation());
    }
}
