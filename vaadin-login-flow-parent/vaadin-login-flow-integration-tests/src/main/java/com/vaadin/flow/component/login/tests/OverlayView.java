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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-login/overlay")
public class OverlayView extends AbstractView {

    private final LoginOverlay login = new LoginOverlay();

    public OverlayView() {
        init(login);
        login.addLoginListener(e -> login.close());
        NativeButton button = new NativeButton("open");
        button.setId("open");
        button.addClickListener(e -> login.setOpened(true));
        add(button);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,
            @OptionalParameter String s) {
        super.setParameter(beforeEvent, s);
        if ("component-title".equals(s)) {
            Div div = new Div(VaadinIcon.VAADIN_H.create(),
                    new H3("Component title"));
            div.setId("componentTitle");
            login.setTitle(div);

            NativeButton removeCustomTitle = new NativeButton("Remove title");
            removeCustomTitle.setId("removeCustomTitle");
            removeCustomTitle.addClickListener(
                    e -> login.setTitle("Make title string again"));
            add(removeCustomTitle);

        }
        if ("property-title-description".equals(s)) {
            login.setTitle("Property title");
            login.setDescription("Property description");
        }
        if ("ptbr".equals(s)) {
            login.setI18n(getPtBrI18n());
        }
    }

    private LoginI18n getPtBrI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Nome do aplicativo");
        i18n.getHeader().setDescription("Descrição do aplicativo");
        i18n.getForm().setUsername("Usuário");
        i18n.getForm().setTitle("Acesse a sua conta");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setPassword("Senha");
        i18n.getForm().setForgotPassword("Esqueci minha senha");
        i18n.getErrorMessage().setTitle("Usuário/senha inválidos");
        i18n.getErrorMessage()
                .setMessage("Confira seu usuário e senha e tente novamente.");
        i18n.setAdditionalInformation(
                "Caso necessite apresentar alguma informação extra para o usuário (como credenciais padrão), este é o lugar.");

        return i18n;
    }
}
