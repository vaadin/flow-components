/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.login.examples;

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
