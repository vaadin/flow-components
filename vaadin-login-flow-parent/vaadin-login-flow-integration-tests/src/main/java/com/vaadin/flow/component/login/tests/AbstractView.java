/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;

public abstract class AbstractView extends Div
        implements HasUrlParameter<String> {

    private AbstractLogin login;

    public AbstractView() {
        this.setSizeFull();
    }

    public void init(AbstractLogin login) {
        this.login = login;
        Div info = new Div();
        info.setId("info");

        login.addForgotPasswordListener(e -> {
            info.setText("Forgot password button pressed");
        });

        AtomicInteger failCounter = new AtomicInteger(0);
        login.addLoginListener(e -> {
            if ("username".equals(e.getUsername())
                    && "password".equals(e.getPassword())) {
                failCounter.set(0);
                info.setText("Successful login");
                return;
            }

            login.setError(true);
            if (failCounter.incrementAndGet() > 2) {
                LoginI18n i18n = LoginI18n.createDefault();
                i18n.getErrorMessage().setTitle("You made too many attempts");
                i18n.getErrorMessage()
                        .setMessage("Your account was suspended for a while");
                login.setI18n(i18n);
                login.setEnabled(false);
            }
        });

        add(login, info);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,
            @OptionalParameter String s) {
        login.setEnabled(!"disable-login".equals(s));

        if ("no-forgot-password".equals(s)) {
            login.setForgotPasswordButtonVisible(false);
        }
    }
}
