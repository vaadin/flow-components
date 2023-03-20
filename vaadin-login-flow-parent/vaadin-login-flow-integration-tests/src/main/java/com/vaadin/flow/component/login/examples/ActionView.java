/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-login/action")
public class ActionView extends Div {

    public ActionView() {
        this.setSizeFull();
        LoginForm loginForm = new LoginForm();
        loginForm.setAction("action/process-login-here");
        add(loginForm);
    }
}
