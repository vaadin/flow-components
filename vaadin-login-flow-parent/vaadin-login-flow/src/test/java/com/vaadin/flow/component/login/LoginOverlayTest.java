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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.shared.Registration;

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

    @Test
    public void setAction_customLoginListeners_throws() {
        final LoginOverlay overlay = new LoginOverlay();
        Registration registration1 = overlay.addLoginListener(ev -> {
        });
        Registration registration2 = overlay.addLoginListener(ev -> {
        });
        Assert.assertThrows(IllegalStateException.class,
                () -> overlay.setAction("login"));

        registration1.remove();
        Assert.assertThrows(IllegalStateException.class,
                () -> overlay.setAction("login"));

        registration2.remove();
        overlay.setAction("login");
    }

    @Test
    public void setAction_unregisterAndRegisterDefaultLoginListener() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.setAction("login");
        overlay.setError(true);

        ComponentUtil.fireEvent(overlay, new AbstractLogin.LoginEvent(overlay,
                true, "username", "password"));
        Assert.assertTrue(
                "Expected form not being disabled by default listener",
                overlay.isEnabled());
        Assert.assertTrue(
                "Expected error status not being reset by default listener",
                overlay.isError());

        overlay.setAction(null);
        ComponentUtil.fireEvent(overlay, new AbstractLogin.LoginEvent(overlay,
                true, "username", "password"));
        Assert.assertFalse("Expected form being disabled by default listener",
                overlay.isEnabled());
        Assert.assertFalse(
                "Expected error status being reset by default listener",
                overlay.isError());
    }

    @Test
    public void addLoginListener_actionSet_throws() {
        final LoginOverlay overlay = new LoginOverlay();
        overlay.setAction("login");
        Assert.assertThrows(IllegalStateException.class,
                () -> overlay.addLoginListener(ev -> {
                }));

        overlay.setAction(null);
        overlay.addLoginListener(ev -> {
        });
    }

}
