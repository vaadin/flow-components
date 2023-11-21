package com.vaadin.flow.component.login;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;

public class LoginFormTest {

    @Test
    public void onForgotPasswordEvent() {
        LoginForm loginFormComponent = new LoginForm();

        AtomicInteger count = new AtomicInteger(0);
        loginFormComponent
                .addForgotPasswordListener(e -> count.incrementAndGet());

        ComponentUtil.fireEvent(loginFormComponent,
                new LoginForm.ForgotPasswordEvent(loginFormComponent, false));

        Assert.assertEquals(1, count.get());
    }

    @Test
    public void onLoginEvent() {
        LoginForm loginFormComponent = new LoginForm();

        AtomicInteger count = new AtomicInteger(0);
        loginFormComponent.addLoginListener(e -> {
            Assert.assertEquals("username", e.getUsername());
            Assert.assertEquals("password", e.getPassword());
            count.incrementAndGet();
        });

        Assert.assertTrue(loginFormComponent.isEnabled());
        ComponentUtil.fireEvent(loginFormComponent, new LoginForm.LoginEvent(
                loginFormComponent, false, "username", "password"));

        Assert.assertEquals(1, count.get());
        Assert.assertFalse(loginFormComponent.isEnabled());
    }

    @Test
    public void loginFormHasStyle() {
        LoginForm loginForm = new LoginForm();
        Assert.assertTrue(loginForm instanceof HasStyle);
    }

    @Test
    public void setErrorMessage_fromNullI18n() {
        final LoginForm form = new LoginForm(null);
        form.setErrorMessage("title", "message");
        Assert.assertTrue(form.isError());
        Assert.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }

    @Test
    public void setErrorMessage_fromDefaultI18n() {
        final LoginForm form = new LoginForm();
        form.setErrorMessage("title", "message");
        Assert.assertTrue(form.isError());
        Assert.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }
}
