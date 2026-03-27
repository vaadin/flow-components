/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.shared.Registration;

class LoginFormTest {

    @Test
    void onForgotPasswordEvent() {
        LoginForm loginFormComponent = new LoginForm();

        AtomicInteger count = new AtomicInteger(0);
        loginFormComponent
                .addForgotPasswordListener(e -> count.incrementAndGet());

        ComponentUtil.fireEvent(loginFormComponent,
                new LoginForm.ForgotPasswordEvent(loginFormComponent, false));

        Assertions.assertEquals(1, count.get());
    }

    @Test
    void onLoginEvent() {
        LoginForm loginFormComponent = new LoginForm();

        AtomicInteger count = new AtomicInteger(0);
        loginFormComponent.addLoginListener(e -> {
            Assertions.assertEquals("username", e.getUsername());
            Assertions.assertEquals("password", e.getPassword());
            count.incrementAndGet();
        });

        Assertions.assertTrue(loginFormComponent.isEnabled());
        ComponentUtil.fireEvent(loginFormComponent, new LoginForm.LoginEvent(
                loginFormComponent, false, "username", "password"));

        Assertions.assertEquals(1, count.get());
        Assertions.assertFalse(loginFormComponent.isEnabled());
    }

    @Test
    void loginFormHasStyle() {
        LoginForm loginForm = new LoginForm();
        Assertions.assertTrue(loginForm instanceof HasStyle);
    }

    @Test
    void showErrorMessage_fromNullI18n() {
        final LoginForm form = new LoginForm(null);
        form.showErrorMessage("title", "message");

        Assertions.assertTrue(form.isError());
        Assertions.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assertions.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }

    @Test
    void showErrorMessage_fromDefaultI18n() {
        final LoginForm form = new LoginForm();
        form.showErrorMessage("title", "message");

        Assertions.assertTrue(form.isError());
        Assertions.assertEquals("title",
                form.getI18n().getErrorMessage().getTitle());
        Assertions.assertEquals("message",
                form.getI18n().getErrorMessage().getMessage());
    }

    @Test
    void showErrorMessage_preservesExistingI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Custom title");
        i18n.getForm().setUsername("Custom username");
        final LoginForm form = new LoginForm(i18n);
        form.showErrorMessage("title", "message");

        Assertions.assertEquals("Custom title",
                form.getI18n().getHeader().getTitle());
        Assertions.assertEquals("Custom username",
                form.getI18n().getForm().getUsername());
    }

    @Test
    void addLoginListeners_setAction_logsWarning() {
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
    void setAction_addLoginListener_logsWarning() {
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
    void setAction_unregisterAndRegisterDefaultLoginListener() {
        final LoginForm form = new LoginForm();
        form.setAction("login");
        form.setError(true);

        ComponentUtil.fireEvent(form, new AbstractLogin.LoginEvent(form, true,
                "username", "password"));
        Assertions.assertTrue(form.isEnabled(),
                "Expected form not being disabled by default listener");
        Assertions.assertTrue(form.isError(),
                "Expected error status not being reset by default listener");

        form.setAction(null);
        ComponentUtil.fireEvent(form, new AbstractLogin.LoginEvent(form, true,
                "username", "password"));
        Assertions.assertFalse(form.isEnabled(),
                "Expected form being disabled by default listener");
        Assertions.assertFalse(form.isError(),
                "Expected error status being reset by default listener");
    }
}
