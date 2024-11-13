/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.login;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.shared.Registration;

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
    public void showErrorMessage_fromNullI18n() {
        final LoginForm form = new LoginForm(null);
        form.showErrorMessage("title", "message");

        Assert.assertTrue(form.isError());
        Assert.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }

    @Test
    public void showErrorMessage_fromDefaultI18n() {
        final LoginForm form = new LoginForm();
        form.showErrorMessage("title", "message");

        Assert.assertTrue(form.isError());
        Assert.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assert.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }

    @Test
    public void showErrorMessage_preservesExistingI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Custom title");
        i18n.getForm().setUsername("Custom username");
        final LoginForm form = new LoginForm(i18n);
        form.showErrorMessage("title", "message");

        Assert.assertEquals("Custom title",
                form.getI18n().getHeader().getTitle());
        Assert.assertEquals("Custom username",
                form.getI18n().getForm().getUsername());
    }

    @Test
    public void addLoginListeners_setAction_logsWarning() {
        final LoginForm form = new LoginForm();
        Registration registration1 = form.addLoginListener(ev -> {
        });
        Registration registration2 = form.addLoginListener(ev -> {
        });

        Logger mockedLogger = Mockito.mock(Logger.class);
        try (MockedStatic<LoggerFactory> context = Mockito
                .mockStatic(LoggerFactory.class)) {
            context.when(() -> LoggerFactory.getLogger(LoginForm.class))
                    .thenReturn(mockedLogger);

            form.setAction("login1");
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            registration1.remove();
            form.setAction("login2");
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            registration2.remove();
            form.setAction("login3");
            Mockito.verify(mockedLogger, Mockito.never())
                    .warn(Mockito.anyString());
        }
    }

    @Test
    public void setAction_addLoginListener_logsWarning() {
        final LoginForm form = new LoginForm();
        form.setAction("login");

        Logger mockedLogger = Mockito.mock(Logger.class);
        try (MockedStatic<LoggerFactory> context = Mockito
                .mockStatic(LoggerFactory.class)) {
            context.when(() -> LoggerFactory.getLogger(LoginForm.class))
                    .thenReturn(mockedLogger);

            form.addLoginListener(ev -> {
            });
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            form.setAction(null);
            form.addLoginListener(ev -> {
            });
            Mockito.verify(mockedLogger, Mockito.never())
                    .warn(Mockito.anyString());
        }
    }

    @Test
    public void setAction_unregisterAndRegisterDefaultLoginListener() {
        final LoginForm form = new LoginForm();
        form.setAction("login");
        form.setError(true);

        ComponentUtil.fireEvent(form, new AbstractLogin.LoginEvent(form, true,
                "username", "password"));
        Assert.assertTrue(
                "Expected form not being disabled by default listener",
                form.isEnabled());
        Assert.assertTrue(
                "Expected error status not being reset by default listener",
                form.isError());

        form.setAction(null);
        ComponentUtil.fireEvent(form, new AbstractLogin.LoginEvent(form, true,
                "username", "password"));
        Assert.assertFalse("Expected form being disabled by default listener",
                form.isEnabled());
        Assert.assertFalse(
                "Expected error status being reset by default listener",
                form.isError());
    }
}
