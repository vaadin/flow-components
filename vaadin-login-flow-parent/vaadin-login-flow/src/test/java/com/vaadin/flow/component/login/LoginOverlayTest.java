package com.vaadin.flow.component.login;

import org.junit.Assert;
import org.junit.Test;

public class LoginOverlayTest {
    @Test
    public void showErrorMessage_fromNullI18n() {
        final LoginOverlay overlay = new LoginOverlay(null);
        overlay.showErrorMessage("title", "message");

        Assert.assertTrue(overlay.isError());
        Assert.assertEquals("title",
                overlay.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                overlay.getI18n().getErrorMessage().getMessage());
    }

    @Test
    public void showErrorMessage_fromDefaultI18n() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.showErrorMessage("title", "message");

        Assert.assertTrue(overlay.isError());
        Assert.assertEquals("title",
                overlay.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                overlay.getI18n().getErrorMessage().getMessage());
    }

    @Test
    public void showErrorMessage_preservesExistingI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Custom title");
        i18n.getForm().setUsername("Custom username");
        final LoginOverlay overlay = new LoginOverlay(i18n);
        overlay.showErrorMessage("title", "message");

        Assert.assertEquals("Custom title",
                overlay.getI18n().getHeader().getTitle());
        Assert.assertEquals("Custom username",
                overlay.getI18n().getForm().getUsername());
    }
}
