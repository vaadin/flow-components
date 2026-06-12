/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.login;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;

public class LoginFormTest {

    private static MockedStatic<VaadinService> vaadinServiceMock;

    @BeforeClass
    public static void enableUrlSchemeValidation() {
        // URL scheme validation is disabled by default in this branch, so
        // configure a strict set of safe schemes to exercise the validation.
        DeploymentConfiguration config = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(config.getUrlSafeSchemes()).thenReturn(new HashSet<>(
                Arrays.asList("http", "https", "mailto", "tel", "ftp")));
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(service.getDeploymentConfiguration()).thenReturn(config);
        vaadinServiceMock = Mockito.mockStatic(VaadinService.class);
        vaadinServiceMock.when(VaadinService::getCurrent).thenReturn(service);
    }

    @AfterClass
    public static void cleanup() {
        vaadinServiceMock.close();
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void setActionWithUnsafeScheme_throws() {
        final LoginForm form = new LoginForm();
        form.setAction("javascript:alert(1)");
    }

    @Test
    public void setUnsafeActionWithUnsafeScheme_actionSet() {
        final LoginForm form = new LoginForm();
        form.setUnsafeAction("javascript:alert(1)");

        Assert.assertEquals("javascript:alert(1)", form.getAction());
    }
}
