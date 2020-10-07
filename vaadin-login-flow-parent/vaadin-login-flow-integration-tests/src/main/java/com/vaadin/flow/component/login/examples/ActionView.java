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
