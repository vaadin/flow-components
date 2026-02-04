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
package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/i18n")
public class LoginOverlayI18nPage extends Div {

    public LoginOverlayI18nPage() {
        LoginOverlay loginOverlay = new LoginOverlay();

        NativeButton openButton = new NativeButton("Open",
                e -> loginOverlay.setOpened(true));
        openButton.setId("open");

        NativeButton setI18n = new NativeButton("Set I18N", e -> {
            LoginI18n i18n = LoginI18n.createDefault();
            i18n.setHeader(new LoginI18n.Header());
            i18n.getHeader().setTitle("Custom title");
            i18n.getHeader().setDescription("Custom description");
            i18n.getForm().setTitle("Custom form title");
            i18n.getForm().setUsername("Custom username");
            i18n.getForm().setPassword("Custom password");
            i18n.getForm().setSubmit("Custom submit");
            i18n.getForm().setForgotPassword("Custom forgot password");
            loginOverlay.setI18n(i18n);
        });
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty I18N",
                e -> loginOverlay.setI18n(new LoginI18n()));
        setEmptyI18n.setId("set-empty-i18n");

        add(openButton, setI18n, setEmptyI18n, loginOverlay);
    }
}
