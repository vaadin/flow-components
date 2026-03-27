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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.shared.Registration;

class LoginOverlayTest {
    @Test
    void showErrorMessage_fromNullI18n() {
        final LoginOverlay overlay = new LoginOverlay(null);
        overlay.showErrorMessage("title", "message");

        Assertions.assertTrue(overlay.isError());
        Assertions.assertEquals("title",
                overlay.getI18n().getErrorMessage().getTitle());
        Assertions.assertEquals("message",
                overlay.getI18n().getErrorMessage().getMessage());
    }

    @Test
    void showErrorMessage_fromDefaultI18n() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.showErrorMessage("title", "message");

        Assertions.assertTrue(overlay.isError());
        Assertions.assertEquals("title",
                overlay.getI18n().getErrorMessage().getTitle());
        Assertions.assertEquals("message",
                overlay.getI18n().getErrorMessage().getMessage());
    }

    @Test
    void showErrorMessage_preservesExistingI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Custom title");
        i18n.getForm().setUsername("Custom username");
        final LoginOverlay overlay = new LoginOverlay(i18n);
        overlay.showErrorMessage("title", "message");

        Assertions.assertEquals("Custom title",
                overlay.getI18n().getHeader().getTitle());
        Assertions.assertEquals("Custom username",
                overlay.getI18n().getForm().getUsername());
    }

    @Test
    void addLoginListeners_setAction_logsWarning() {
        final LoginOverlay overlay = new LoginOverlay();
        Registration registration1 = overlay.addLoginListener(ev -> {
        });
        Registration registration2 = overlay.addLoginListener(ev -> {
        });

        Logger mockedLogger = Mockito.mock(Logger.class);
        try (MockedStatic<LoggerFactory> context = Mockito
                .mockStatic(LoggerFactory.class)) {
            context.when(() -> LoggerFactory.getLogger(LoginOverlay.class))
                    .thenReturn(mockedLogger);

            overlay.setAction("login1");
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            registration1.remove();
            overlay.setAction("login2");
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            registration2.remove();
            overlay.setAction("login3");
            Mockito.verify(mockedLogger, Mockito.never())
                    .warn(Mockito.anyString());
        }
    }

    @Test
    void setAction_addLoginListener_logsWarning() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.setAction("login");

        Logger mockedLogger = Mockito.mock(Logger.class);
        try (MockedStatic<LoggerFactory> context = Mockito
                .mockStatic(LoggerFactory.class)) {
            context.when(() -> LoggerFactory.getLogger(LoginOverlay.class))
                    .thenReturn(mockedLogger);

            overlay.addLoginListener(ev -> {
            });
            Mockito.verify(mockedLogger, Mockito.times(1))
                    .warn(Mockito.anyString());
            Mockito.reset(mockedLogger);

            overlay.setAction(null);
            overlay.addLoginListener(ev -> {
            });
            Mockito.verify(mockedLogger, Mockito.never())
                    .warn(Mockito.anyString());
        }
    }

    @Test
    void setAction_unregisterAndRegisterDefaultLoginListener() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.setAction("login");
        overlay.setError(true);

        ComponentUtil.fireEvent(overlay, new AbstractLogin.LoginEvent(overlay,
                true, "username", "password"));
        Assertions.assertTrue(overlay.isEnabled(),
                "Expected form not being disabled by default listener");
        Assertions.assertTrue(overlay.isError(),
                "Expected error status not being reset by default listener");

        overlay.setAction(null);
        ComponentUtil.fireEvent(overlay, new AbstractLogin.LoginEvent(overlay,
                true, "username", "password"));
        Assertions.assertFalse(overlay.isEnabled(),
                "Expected form being disabled by default listener");
        Assertions.assertFalse(overlay.isError(),
                "Expected error status being reset by default listener");
    }
}
