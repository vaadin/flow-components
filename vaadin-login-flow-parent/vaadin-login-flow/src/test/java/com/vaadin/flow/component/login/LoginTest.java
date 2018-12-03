package com.vaadin.flow.component.login;

import com.vaadin.flow.component.ComponentUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class LoginTest {

    @Test
    public void onForgotPasswordEvent() {
        Login loginComponent = new Login();

        AtomicInteger count = new AtomicInteger(0);
        loginComponent.addForgotPasswordListener(e -> count.incrementAndGet());

        ComponentUtil.fireEvent(loginComponent, new Login.ForgotPasswordEvent(loginComponent, false));

        Assert.assertEquals(1, count.get());
    }

    @Test
    public void onLoginEvent() {
        Login loginComponent = new Login();

        AtomicInteger count = new AtomicInteger(0);
        loginComponent.addLoginListener(e -> {
            Assert.assertEquals("username", e.getUsername());
            Assert.assertEquals("password", e.getPassword());
            count.incrementAndGet();
        });

        Assert.assertTrue(loginComponent.isEnabled());
        ComponentUtil.fireEvent(loginComponent, new Login.LoginEvent(loginComponent, false,
                "username", "password"));

        Assert.assertEquals(1, count.get());
        Assert.assertFalse(loginComponent.isEnabled());
    }
}
